import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.MediaTracker;
import java.io.*;
import javax.swing.JOptionPane;
import javax.sound.sampled.*;
import java.io.File;




interface Renderable {
    int getRenderX();
    int getRenderY();
    int getRenderPriority();
    int getDepthX(); // Bottom-right X for depth calculation
    int getDepthY(); // Bottom-right Y for depth calculation
    void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game);
}

public class CodeBlue extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    public static final int TILE_WIDTH = 16;
    public static final int TILE_HEIGHT = 8;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;
    
    // Player positions (in grid coordinates)
    private Point player1Pos = new Point(5, 5);
    private Point player2Pos = new Point(7, 7);
    
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 50; // milliseconds between moves
    
    private boolean showGrid = true;
    public boolean showTileCoordinates = false;
    
    // Input handling
    private Set<Integer> pressedKeys = new HashSet<>();
    private boolean isEraseMode = false;
    private Point mouseGridPos = null;
    private boolean showPreview = false;
    
    // Camera system
    private Point2D.Double cameraPos = new Point2D.Double(0, 0);
    private static final double CAMERA_LERP_SPEED = 0.15;
    private boolean cameraLerp = true;
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 16.0;
    private static final double ZOOM_STEP = 1.0;
    
    private Color floorColor = new Color(240, 240, 240);
    private Color wallColor = new Color(139, 69, 19);
    private Color player1Color = new Color(255, 100, 100);
    private Color player2Color = new Color(100, 100, 255);
    private Color gridColor = new Color(200, 200, 200);
    
    private java.util.List<Bed> beds = new ArrayList<>();

    public Image bedSprite;
    public Image floorSprite;
    public Image wallNESWSprite;  
    public Image wallNWSESprite;   
    public Image wallNWSEShortSprite;  
    public Image wallCornerSprite; 
    public Image wallCornerNorthSprite;
    public Image wallCornerSouthSprite;
    
    public Image wheelchairNorthSprite;
    public Image wheelchairEastSprite;
    public Image wheelchairSouthSprite;
    public Image wheelchairWestSprite;
    
    public boolean showSprites = true;
    private boolean showDepthDebug = false;
    
    private java.util.List<WallSegment> walls = new ArrayList<>();
    private boolean isThinWallMode = false;
    private WallSegment.Type currentThinWallType = WallSegment.Type.DIAGONAL_NW;
    
    private java.util.List<FloorTile> placedFloorTiles = new ArrayList<>();
    private boolean isFloorMode = false;
    
    private java.util.List<Wheelchair> wheelchairs = new ArrayList<>();
    
    private boolean isPushingWheelchair = false;
    private Wheelchair pushedWheelchair = null;
    
    private Clip backgroundMusic;
    private boolean musicEnabled = true;
    
public enum PlaceableType {
    FLOOR_TILE,
    THIN_WALL_NE,
    THIN_WALL_NW, 
    THIN_WALL_NW_SHORT,
    THIN_WALL_CORNER_NORTH,
    THIN_WALL_CORNER_SOUTH,
    BED,
    WHEELCHAIR,
}
private PlaceableType currentPlaceableType = PlaceableType.FLOOR_TILE;
private boolean isPlacementMode = false;
    
    
    public CodeBlue() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        loadSprites();
        loadBackgroundMusic();
    }

    
private void loadSprites() {
    try {
        bedSprite = Toolkit.getDefaultToolkit().getImage("bed.png");
        floorSprite = Toolkit.getDefaultToolkit().getImage("floor.png");
        wallNESWSprite = Toolkit.getDefaultToolkit().getImage("wall_NE-SW.png");
        wallNWSESprite = Toolkit.getDefaultToolkit().getImage("wall_NW-SE.png");
         wallNWSEShortSprite = Toolkit.getDefaultToolkit().getImage("wall_NW-SE_short.png");
        wallCornerNorthSprite = Toolkit.getDefaultToolkit().getImage("wall_corner_north.png");
        wallCornerSouthSprite = Toolkit.getDefaultToolkit().getImage("wall_corner_south.png");
        
        wheelchairNorthSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_north.png");
        wheelchairEastSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_east.png");
        wheelchairSouthSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_south.png");
        wheelchairWestSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_west.png");
        
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(bedSprite, 0);
        tracker.addImage(floorSprite, 1);
        tracker.addImage(wallNESWSprite, 2);
        tracker.addImage(wallNWSESprite, 3);
        tracker.addImage(wallNWSEShortSprite, 4);
        tracker.addImage(wallCornerNorthSprite, 5);
        tracker.addImage(wallCornerSouthSprite, 6);
        tracker.addImage(wheelchairNorthSprite, 7);
        tracker.addImage(wheelchairEastSprite, 8);
        tracker.addImage(wheelchairSouthSprite, 9);
        tracker.addImage(wheelchairWestSprite, 10);
        tracker.waitForAll();
    } catch (Exception e) {
        bedSprite = createPlaceholderBed();
        floorSprite = createPlaceholderFloor();
        wallNESWSprite = createPlaceholderWall();
        wallNWSESprite = createPlaceholderWall();
        wallCornerSprite = createPlaceholderWall();
    }
}
    
private Image createPlaceholderBed() {
    BufferedImage placeholder = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = placeholder.createGraphics();
    g.setColor(new Color(139, 69, 19));
    g.fillRect(0, 0, 50, 50);
    g.setColor(Color.WHITE);
    g.drawString("BED", 15, 28);
    g.dispose();
    return placeholder;
}
    
private Image createPlaceholderFloor() {
    BufferedImage placeholder = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = placeholder.createGraphics();
    g.setColor(floorColor);
    g.fillRect(0, 0, 50, 50);
    g.setColor(Color.GRAY);
    g.drawRect(0, 0, 49, 49);
    g.dispose();
    return placeholder;
}
    
private Image createPlaceholderWall() {
    BufferedImage placeholder = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = placeholder.createGraphics();
    g.setColor(wallColor);
    g.fillRect(0, 0, 50, 50);
    g.setColor(Color.GRAY);
    g.drawRect(0, 0, 49, 49);
    g.dispose();
    return placeholder;
}
    
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // Apply zoom transformation
    g2d.scale(zoomLevel, zoomLevel);
    
    // Calculate offset to center the map with camera
    int offsetX = (int)(WINDOW_WIDTH / 2 / zoomLevel - cameraPos.x);
    int offsetY = (int)(WINDOW_HEIGHT / 2 / zoomLevel - cameraPos.y);
    
    drawFloor(g2d, offsetX, offsetY);
    
    
    // Collect all renderable objects INCLUDING thin walls
    List<Renderable> renderables = new ArrayList<>();
    renderables.addAll(placedFloorTiles);
    renderables.addAll(beds);
    renderables.addAll(walls); // Add thin walls to depth sorting
    renderables.add(new Player(player1Pos, player1Color, "P1"));
    renderables.add(new Player(player2Pos, player2Color, "P2"));
    renderables.addAll(wheelchairs);

    // Sort by depth (back to front) using bottom-right corner
    renderables.sort((a, b) -> {
        int depthA = a.getDepthX() + a.getDepthY();
        int depthB = b.getDepthX() + b.getDepthY();
        if (depthA != depthB) {
            return Integer.compare(depthA, depthB);
        }
        return Integer.compare(a.getRenderPriority(), b.getRenderPriority());
    });
    
    // Render all objects in depth order
    for (Renderable obj : renderables) {
        obj.render(g2d, offsetX, offsetY, this);
    }
    
    // Draw wall preview AFTER all other objects so it's always visible
if (showPreview && mouseGridPos != null) {
    switch (currentPlaceableType) {
        case FLOOR_TILE:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == mouseGridPos.x && floor.y == mouseGridPos.y)) {
                // Render floor preview
                Point isoPos = gridToIso(mouseGridPos.x, mouseGridPos.y, offsetX, offsetY);
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                
                int floorDisplayWidth = TILE_WIDTH;
                int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
                int floorX = isoPos.x - floorDisplayWidth / 2;
                int floorY = isoPos.y - floorDisplayHeight + TILE_HEIGHT / 2;
                g2d.drawImage(floorSprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
                
                g2d.setComposite(originalComposite);
            }
            break;
            
        case THIN_WALL_NE:
        case THIN_WALL_NW:
        case THIN_WALL_NW_SHORT:
        case THIN_WALL_CORNER_NORTH:
        case THIN_WALL_CORNER_SOUTH:
            WallSegment.Type wallType = getWallTypeFromPlaceable(currentPlaceableType);
            if (!walls.stream().anyMatch(wall -> 
                wall.gridX == mouseGridPos.x && wall.gridY == mouseGridPos.y && wall.type == wallType)) {
                WallPreview preview = new WallPreview(mouseGridPos, wallType);
                preview.render(g2d, offsetX, offsetY, this);
            }
            break;
            
        // Add other preview cases as needed
    }
}
    

    
    if (showDepthDebug) {
        g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("Arial", Font.BOLD, 12));
    FontMetrics fm = g2d.getFontMetrics();
    
    for (Renderable obj : renderables) {
        Point isoPos = gridToIso(obj.getRenderX(), obj.getRenderY(), offsetX, offsetY);
        int depthX = obj.getDepthX();
        int depthY = obj.getDepthY();
        int totalDepth = depthX + depthY;
        
        String depthText = "(" + depthX + "," + depthY + ")=" + totalDepth;
        int textWidth = fm.stringWidth(depthText);
        
        // Draw background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(isoPos.x - textWidth/2 - 2, isoPos.y - 30 - fm.getAscent(), 
                    textWidth + 4, fm.getHeight());
        
        // Draw text
        g2d.setColor(Color.CYAN);
        g2d.drawString(depthText, isoPos.x - textWidth/2, isoPos.y - 30);
    }
    }
    
    // Reset transformation for UI
    g2d.scale(1.0/zoomLevel, 1.0/zoomLevel);
    
    // Draw UI
    drawUI(g2d);
}
    
private WallSegment.Type getWallTypeFromPlaceable(PlaceableType type) {
    switch (type) {
        case THIN_WALL_NE: return WallSegment.Type.DIAGONAL_NE;
        case THIN_WALL_NW: return WallSegment.Type.DIAGONAL_NW;
        case THIN_WALL_NW_SHORT: return WallSegment.Type.DIAGONAL_NW_short;
        case THIN_WALL_CORNER_NORTH: return WallSegment.Type.CORNER_NORTH;
        case THIN_WALL_CORNER_SOUTH: return WallSegment.Type.CORNER_SOUTH;
        default: return WallSegment.Type.DIAGONAL_NE;
    }
}
  
    
private void drawFloor(Graphics2D g2d, int offsetX, int offsetY) {
    for (int x = 0; x < MAP_WIDTH; x++) {
        for (int y = 0; y < MAP_HEIGHT; y++) {
            Point isoPos = gridToIso(x, y, offsetX, offsetY);
            
            // Draw floor sprite first
            if (showSprites) {
                int floorX = isoPos.x - 25; // Center 50x50 sprite
                int floorY = isoPos.y - 12;
              //  g2d.drawImage(floorSprite, floorX, floorY, null);
            }
            
            // Draw colored overlay for grid mode
            Color tileColor = null;
            if (showGrid) {
                if (isPlayerTile(x, y)) {
                    tileColor = new Color(150, 200, 150, 100); // Semi-transparent green
                } else if (isBedTile(x, y)) {
                    tileColor = new Color(200, 150, 150, 100); // Semi-transparent red
                }
            }
            
            // Draw tile overlay or grid
            if (tileColor != null) {
            //    drawIsometricTile(g2d, isoPos.x, isoPos.y, tileColor, false);
            } else if (showGrid && !showSprites) {
              //  drawIsometricTile(g2d, isoPos.x, isoPos.y, floorColor, true);
            } else if (showGrid) {
                // Just draw grid lines over sprites with constant thickness
                g2d.setColor(gridColor);
                setConstantThicknessStroke(g2d, 1.0f); // Maintains 1-pixel visual thickness
                int[] xPoints = {isoPos.x, isoPos.x + TILE_WIDTH/2, isoPos.x, isoPos.x - TILE_WIDTH/2};
                int[] yPoints = {isoPos.y - TILE_HEIGHT/2, isoPos.y, isoPos.y + TILE_HEIGHT/2, isoPos.y};
                g2d.drawPolygon(xPoints, yPoints, 4);
                g2d.setStroke(new BasicStroke(1.0f)); // Reset stroke
            }
        }
    }
}
    
private boolean isBedTile(int x, int y) {
    for (Bed bed : beds) {
        if (bed.occupiesTile(x, y)) {
            return true;
        }
    }
    return false;
}
    
private boolean isPlayerTile(int x, int y) {
    return (x == player1Pos.x && y == player1Pos.y) ||
           (x == player2Pos.x && y == player2Pos.y);
}
    
public void setConstantThicknessStroke(Graphics2D g2d, float pixelWidth) {
    // Divide by zoom level to maintain constant visual thickness
    float adjustedWidth = (float)(pixelWidth / zoomLevel);
    g2d.setStroke(new BasicStroke(adjustedWidth));
}    
    
private void drawIsometricTile(Graphics2D g2d, int x, int y, Color color, boolean drawBorder) {
    int[] xPoints = {
        x, x + TILE_WIDTH/2, x, x - TILE_WIDTH/2
    };
    int[] yPoints = {
        y - TILE_HEIGHT/2, y, y + TILE_HEIGHT/2, y
    };
    
    Polygon diamond = new Polygon(xPoints, yPoints, 4);
    g2d.setColor(color);
    g2d.fillPolygon(diamond);
    
    if (drawBorder) {
        g2d.setColor(gridColor);
        // Set constant visual thickness (1 pixel on screen regardless of zoom)
        setConstantThicknessStroke(g2d, 1.0f);
        g2d.drawPolygon(diamond);
        // Reset to default stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }
}
    
    public static Point gridToIso(int gridX, int gridY, int offsetX, int offsetY) {
        int isoX = (gridX - gridY) * TILE_WIDTH / 2 + offsetX;
        int isoY = (gridX + gridY) * TILE_HEIGHT / 2 + offsetY;
        return new Point(isoX, isoY);
    }
    
    private Point isoToGrid(int isoX, int isoY, int offsetX, int offsetY) {
        // Convert screen coordinates back to grid coordinates
        isoX -= offsetX;
        isoY -= offsetY;
        
        int gridX = (isoX / (TILE_WIDTH/2) + isoY / (TILE_HEIGHT/2)) / 2;
        int gridY = (isoY / (TILE_HEIGHT/2) - isoX / (TILE_WIDTH/2)) / 2;
        
        return new Point(gridX, gridY);
    }
    
    private Point worldToGrid(double worldX, double worldY) {
        // Improved isometric to grid conversion for accurate tile detection
        double gridX = (worldX / (TILE_WIDTH / 2.0) + worldY / (TILE_HEIGHT / 2.0)) / 2.0;
        double gridY = (worldY / (TILE_HEIGHT / 2.0) - worldX / (TILE_WIDTH / 2.0)) / 2.0;

        // Round to nearest grid position
        return new Point((int)Math.round(gridX), (int)Math.round(gridY));
    }
    
private void drawUI(Graphics2D g2d) {
    g2d.setColor(Color.BLACK);
    g2d.setFont(new Font("Arial", Font.PLAIN, 12));
    
    String[] instructions = {
        "Player 1 (Red): WASD to move",
        "Player 2 (Blue): Arrow keys to move",
        "T: Toggle placement mode " + (isPlacementMode ? "[ON]" : "[OFF]"),
        "R: Cycle object type - Current: " + currentPlaceableType.toString(),
        "E: Toggle erase mode " + (isEraseMode ? "[ON]" : "[OFF]"),
        "C: Toggle coordinates " + (showTileCoordinates ? "[ON]" : "[OFF]"),
        "Mouse Click: Place/erase object",
        "H: Toggle sprites " + (showSprites ? "[ON]" : "[OFF]"),
        "Ctrl+S: Save map",
        "Ctrl+O: Load map", 
        "Ctrl+N: New map",
        "+ / =: Zoom in, - : Zoom out",
        "Zoom: " + String.format("%.1f", zoomLevel) + "x"
    };
    
    for (int i = 0; i < instructions.length; i++) {
        g2d.drawString(instructions[i], 10, 20 + i * 15);
    }
}
    
private void updateGame() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastMoveTime < MOVE_DELAY) {
        return; // Too soon to move again
    }        
    
     boolean moved = false;
    
    // Player 1 movement with wheelchair pushing
    if (isPushingWheelchair && pushedWheelchair != null) {
        Point newPos1 = new Point(player1Pos);
        boolean player1Wants2Move = false;
        Point movementDirection = new Point(0, 0);
        
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            newPos1.y--;
            movementDirection = new Point(0, -1); // North
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_S)) {
            newPos1.y++;
            movementDirection = new Point(0, 1); // South
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_A)) {
            newPos1.x--;
            movementDirection = new Point(-1, 0); // West
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_D)) {
            newPos1.x++;
            movementDirection = new Point(1, 0); // East
            player1Wants2Move = true;
        }
        
        if (player1Wants2Move) {
            // Set wheelchair direction based on movement
            if (movementDirection.equals(new Point(0, -1))) {
                pushedWheelchair.direction = 0; // North
            } else if (movementDirection.equals(new Point(1, 0))) {
                pushedWheelchair.direction = 1; // East
            } else if (movementDirection.equals(new Point(0, 1))) {
                pushedWheelchair.direction = 2; // South
            } else if (movementDirection.equals(new Point(-1, 0))) {
                pushedWheelchair.direction = 3; // West
            }
            // Calculate new wheelchair position (in front of player's new position)
            Point newChairPos = new Point(newPos1.x + movementDirection.x, 
                                         newPos1.y + movementDirection.y);
            
            // Check if both player and wheelchair can move to their new positions
            if (isValidMove(player1Pos, newPos1) && 
                isValidWheelchairMove(pushedWheelchair, newChairPos) &&
                !newPos1.equals(player2Pos) && !newChairPos.equals(player2Pos)) {
                
                // Move both player and wheelchair
                player1Pos.x = newPos1.x;
                player1Pos.y = newPos1.y;
                pushedWheelchair.x = newChairPos.x;
                pushedWheelchair.y = newChairPos.y;
                moved = true;
            }
        }
    } else {
        // Normal player 1 movement
        Point newPos1 = new Point(player1Pos);
        boolean player1Moved = false;
        
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            newPos1.y--;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_S)) {
            newPos1.y++;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_A)) {
            newPos1.x--;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_D)) {
            newPos1.x++;
            player1Moved = true;
        }
        
        if (player1Moved && isValidMove(player1Pos, newPos1) && !newPos1.equals(player2Pos)) {
            player1Pos.x = newPos1.x;
            player1Pos.y = newPos1.y;
            moved = true;
        }
    }
    
    // Player 2 movement (normal movement only)
    Point newPos2 = new Point(player2Pos);
    boolean player2Moved = false;
    
    if (pressedKeys.contains(KeyEvent.VK_UP)) {
        newPos2.y--;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
        newPos2.y++;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
        newPos2.x--;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
        newPos2.x++;
        player2Moved = true;
    }
    
    if (player2Moved && isValidMove(player2Pos, newPos2) && !newPos2.equals(player1Pos)) {
        player2Pos.x = newPos2.x;
        player2Pos.y = newPos2.y;
        moved = true;
    }
    
    if (moved) {
        lastMoveTime = currentTime;
        updateCamera();
        repaint();
    }
    
     checkMusicStatus(); 
    
}
    
private void updateCamera() {
    // Calculate the midpoint between the two players in grid coordinates
    double targetGridX = (player1Pos.x + player2Pos.x) / 2.0;
    double targetGridY = (player1Pos.y + player2Pos.y) / 2.0;
    
    // Convert to isometric screen coordinates
    double isoTargetX = (targetGridX - targetGridY) * TILE_WIDTH / 2;
    double isoTargetY = (targetGridX + targetGridY) * TILE_HEIGHT / 2;
    
    if (cameraLerp) {
        // Lerp camera position towards target
        cameraPos.x += (isoTargetX - cameraPos.x) * CAMERA_LERP_SPEED;
        cameraPos.y += (isoTargetY - cameraPos.y) * CAMERA_LERP_SPEED;
    } else {
        // Instant camera movement
        cameraPos.x = isoTargetX;
        cameraPos.y = isoTargetY;
    }
    
    // Always repaint when camera moves
    repaint();
}
    
private boolean isValidMove(Point from, Point to) {
    if (to.x < 0 || to.x >= MAP_WIDTH || to.y < 0 || to.y >= MAP_HEIGHT) {
        return false;
    }
    
    // Check thin walls
    for (WallSegment wall : walls) {
       // System.out.println(wall);
        if (wall.blocksMovement(from, to)) {
            return false;
        }
    }
    
    // Check beds
    for (Bed bed : beds) {
        if (bed.occupiesTile(to.x, to.y)) {
            return false;
        }
    }
    
    for (Wheelchair chair : wheelchairs) {
        if (isPushingWheelchair && chair == pushedWheelchair) {
            continue; // Skip this wheelchair check
        }
        if (chair.x == to.x && chair.y == to.y) {
            return false;
        }
    }
    
    return true;
}
    
    
    private void placeObject(Point gridPos) {
    if (isEraseMode) {
        eraseObject(gridPos);
        return;
    }
    
    switch (currentPlaceableType) {
        case FLOOR_TILE:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == gridPos.x && floor.y == gridPos.y)) {
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y));
            }
            break;
            
        case THIN_WALL_NE:
            placeThinWall(gridPos, WallSegment.Type.DIAGONAL_NE);
            break;
            
        case THIN_WALL_NW:
            placeThinWall(gridPos, WallSegment.Type.DIAGONAL_NW);
            break;
            
        case THIN_WALL_NW_SHORT:
            placeThinWall(gridPos, WallSegment.Type.DIAGONAL_NW_short);
            break;
            
        case THIN_WALL_CORNER_NORTH:
            placeThinWall(gridPos, WallSegment.Type.CORNER_NORTH);
            break;
            
        case THIN_WALL_CORNER_SOUTH:
            placeThinWall(gridPos, WallSegment.Type.CORNER_SOUTH);
            break;
            
        case BED:
            placeBed(gridPos);
            break;
            
        case WHEELCHAIR:
            if (!wheelchairs.stream().anyMatch(chair -> 
                chair.x == gridPos.x && chair.y == gridPos.y)) {
                wheelchairs.add(new Wheelchair(gridPos.x, gridPos.y));
            }
            break;
            
    }
}
    
    
private void placeThinWall(Point gridPos, WallSegment.Type type) {
    if (!walls.stream().anyMatch(wall -> 
        wall.gridX == gridPos.x && wall.gridY == gridPos.y && wall.type == type)) {
        walls.add(new WallSegment(gridPos.x, gridPos.y, type));
    }
}

private void placeBed(Point gridPos) {
    // Check if 4x2 area is clear
    boolean canPlace = true;
    for (int dx = 0; dx < 4 && canPlace; dx++) {
        for (int dy = 0; dy < 2 && canPlace; dy++) {
            int checkX = gridPos.x + dx;
            int checkY = gridPos.y + dy;
            
            if (checkX >= MAP_WIDTH || checkY >= MAP_HEIGHT ||
                (checkX == player1Pos.x && checkY == player1Pos.y) ||
                (checkX == player2Pos.x && checkY == player2Pos.y)) {
                canPlace = false;
            }
            
            for (Bed bed : beds) {
                if (bed.occupiesTile(checkX, checkY)) {
                    canPlace = false;
                    break;
                }
            }
        }
    }
    
    if (canPlace) {
        beds.add(new Bed(gridPos.x, gridPos.y));
    }
}


// Unified erase method
private void eraseObject(Point gridPos) {
    // Remove floor tiles
    placedFloorTiles.removeIf(floor -> 
        floor.x == gridPos.x && floor.y == gridPos.y);
    
    // Remove thin walls
    walls.removeIf(wall -> 
        wall.gridX == gridPos.x && wall.gridY == gridPos.y);
    
    // Remove beds
    beds.removeIf(bed -> bed.occupiesTile(gridPos.x, gridPos.y));
    
wheelchairs.removeIf(chair -> 
    chair.x == gridPos.x && chair.y == gridPos.y);
    

}
    
    
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        
if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
            // Zoom in
            if (zoomLevel < MAX_ZOOM) {
                zoomLevel = Math.min(MAX_ZOOM, zoomLevel + ZOOM_STEP);
                repaint();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            // Zoom out
            if (zoomLevel > MIN_ZOOM) {
                zoomLevel = Math.max(MIN_ZOOM, zoomLevel - ZOOM_STEP);
                repaint();
            }
        }  else if (e.getKeyCode() == KeyEvent.VK_G) {
            showGrid = !showGrid;
            repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_L) {
            cameraLerp = !cameraLerp;
            repaint();
        }  else if (e.getKeyCode() == KeyEvent.VK_H) {
            showSprites = !showSprites;
            repaint();
        }
    else if (e.getKeyCode() == KeyEvent.VK_T) {
        // Toggle placement mode
        isPlacementMode = !isPlacementMode;
        repaint();
    } else if (e.getKeyCode() == KeyEvent.VK_R) {
        // Cycle through placeable objects
        PlaceableType[] types = PlaceableType.values();
        int currentIndex = 0;
        for (int i = 0; i < types.length; i++) {
            if (types[i] == currentPlaceableType) {
                currentIndex = i;
                break;
            }
        }
        currentPlaceableType = types[(currentIndex + 1) % types.length];
        repaint();
    } else if (e.getKeyCode() == KeyEvent.VK_E) {
        isEraseMode = !isEraseMode;
        repaint();
    }


else if (e.getKeyCode() == KeyEvent.VK_F) {
    showDepthDebug = !showDepthDebug;
    repaint();
}
        
else if (e.getKeyCode() == KeyEvent.VK_C) {
    showTileCoordinates = !showTileCoordinates;
    repaint();
}
        
else if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
    pressedKeys.remove(KeyEvent.VK_S); // Remove S from pressed keys
    saveMap();
} else if (e.getKeyCode() == KeyEvent.VK_O && e.isControlDown()) {
    pressedKeys.remove(KeyEvent.VK_O); // Remove O from pressed keys  
    loadMap();
} else if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
    pressedKeys.remove(KeyEvent.VK_N); // Remove N from pressed keys
    newMap();
}
        
if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
    if (!isPushingWheelchair) {
        // Try to start pushing - look for wheelchair near player
        Wheelchair chair1 = getWheelchairNearPlayer(player1Pos);
        Wheelchair chair2 = getWheelchairNearPlayer(player2Pos);
        
        if (chair1 != null) {
            isPushingWheelchair = true;
            pushedWheelchair = chair1;
        } else if (chair2 != null) {
            isPushingWheelchair = true;
            pushedWheelchair = chair2;
        }
    } else {
        // Stop pushing
        isPushingWheelchair = false;
        pushedWheelchair = null;
    }
}
        
        updateGame();
    }
    
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
@Override
public void mouseClicked(MouseEvent e) {
    double worldX = (e.getX() / zoomLevel) + cameraPos.x - (WINDOW_WIDTH / 2.0 / zoomLevel);
    double worldY = (e.getY() / zoomLevel) + cameraPos.y - (WINDOW_HEIGHT / 2.0 / zoomLevel);
    Point gridPos = worldToGrid(worldX, worldY);
    
    if (gridPos.x >= 0 && gridPos.x < MAP_WIDTH && 
        gridPos.y >= 0 && gridPos.y < MAP_HEIGHT) {
        
        if (isPlacementMode) {
            placeObject(gridPos);
        }
        
        repaint();
    }
}
    
    
@Override
public void mouseMoved(MouseEvent e) {
    double worldX = (e.getX() / zoomLevel) + cameraPos.x - (WINDOW_WIDTH / 2.0 / zoomLevel);
    double worldY = (e.getY() / zoomLevel) + cameraPos.y - (WINDOW_HEIGHT / 2.0 / zoomLevel);
    Point newGridPos = worldToGrid(worldX, worldY);
    
    if (!newGridPos.equals(mouseGridPos)) {
        mouseGridPos = newGridPos;
        showPreview = isPlacementMode && 
                     mouseGridPos.x >= 0 && mouseGridPos.x < MAP_WIDTH && 
                     mouseGridPos.y >= 0 && mouseGridPos.y < MAP_HEIGHT;
        repaint();
    }
}

@Override
public void mouseDragged(MouseEvent e) {
    mouseMoved(e); // Handle dragging same as moving
}    
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Code Blue - Isometric Hospital Game");
        CodeBlue game = new CodeBlue();
        
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setVisible(true);
        
        // Game loop for smooth movement
        javax.swing.Timer gameTimer = new javax.swing.Timer(50, e -> game.updateGame());
        gameTimer.start();
    }
    
    
    
    
public boolean shouldWallBeTransparent(int wallX, int wallY, WallSegment.Type wallType) {
    // Check both players
    for (Point playerPos : new Point[]{player1Pos, player2Pos}) {
        int dx = wallX - playerPos.x;
        int dy = wallY - playerPos.y;
        
        // Define exactly which relative positions make walls transparent
        // Wall becomes transparent if it's at these specific offsets from player
        boolean shouldBeTransparent = 
            (dx == 1 && dy == 0 && wallType == WallSegment.Type.DIAGONAL_NE) ||  // Wall directly east
            (dx == 0 && dy == 1 && wallType == WallSegment.Type.DIAGONAL_NW) ||  // Wall directly south  
            (dx == 1 && dy == 1) ||  // Wall southeast (diagonal)
            (dx == 2 && dy == 1) ||  // Two east, one south
            (dx == 1 && dy == 2);    // One east, two south
            // Add more specific positions as needed
        
        if (shouldBeTransparent) {
            return true;
        }
    }
    
    return false;
}
    
    
    
    
    
// Save map to file
private void saveMap() {
    File file = new File("map.map");
     
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write map header
            writer.println("# Hospital Map File");
            writer.println("VERSION=1.0");
            writer.println("MAP_WIDTH=" + MAP_WIDTH);
            writer.println("MAP_HEIGHT=" + MAP_HEIGHT);
            writer.println();
            
            // Write player positions
            writer.println("# Player Positions");
            writer.println("PLAYER1=" + player1Pos.x + "," + player1Pos.y);
            writer.println("PLAYER2=" + player2Pos.x + "," + player2Pos.y);
            writer.println();
            
            
            // Write thin walls
            writer.println("# Thin Walls (x,y,type)");
            for (WallSegment wall : walls) {
                writer.println("THIN_WALL=" + wall.gridX + "," + wall.gridY + "," + wall.type.toString());
            }
            writer.println();
            
            // Write floor tiles
            writer.println("# Floor Tiles (x,y)");
            for (FloorTile floor : placedFloorTiles) {
                writer.println("FLOOR=" + floor.x + "," + floor.y);
            }
            writer.println();
            
            // Write beds
            writer.println("# Beds (x,y)");
            for (Bed bed : beds) {
                writer.println("BED=" + bed.x + "," + bed.y);
            }
            
            writer.println("# Wheelchairs (x,y,direction)");
            for (Wheelchair chair : wheelchairs) {
                writer.println("WHEELCHAIR=" + chair.x + "," + chair.y + "," + chair.direction);
            }
            writer.println();

            
            JOptionPane.showMessageDialog(this, "Map saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving map: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    
}

// Load map from file
private void loadMap() {
    File file = new File("map.map");
    
    if (!file.exists()) {
        JOptionPane.showMessageDialog(this, "No map.map file found in current directory", "Load Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Clear existing map data
            clearMap();
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                
                // Parse different data types
                if (line.startsWith("PLAYER1=")) {
                    String[] coords = line.substring(8).split(",");
                    player1Pos = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                    
                } else if (line.startsWith("PLAYER2=")) {
                    String[] coords = line.substring(8).split(",");
                    player2Pos = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
                    
                } else if (line.startsWith("THIN_WALL=")) {
                    String[] parts = line.substring(10).split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    WallSegment.Type type = WallSegment.Type.valueOf(parts[2]);
                    walls.add(new WallSegment(x, y, type));
                    
                } else if (line.startsWith("FLOOR=")) {
                    String[] coords = line.substring(6).split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    placedFloorTiles.add(new FloorTile(x, y));
                    
                } else if (line.startsWith("BED=")) {
                    String[] coords = line.substring(4).split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    beds.add(new Bed(x, y));
                }
                else if (line.startsWith("WHEELCHAIR=")) {
                    String[] parts = line.substring(11).split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int dir = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                    Wheelchair chair = new Wheelchair(x, y);
                    chair.direction = dir;
                    wheelchairs.add(chair);
                }
            }
            
            updateCamera(); // Center camera on players
            repaint();
            
            JOptionPane.showMessageDialog(this, "Map loaded successfully!", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading map: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error parsing map file: " + e.getMessage(), "Parse Error", JOptionPane.ERROR_MESSAGE);
        }
    
}

// Clear all map data
private void clearMap() {
    // Clear  objects
    walls.clear();
    placedFloorTiles.clear();
    beds.clear();
    wheelchairs.clear();
    
    // Reset player positions
    player1Pos = new Point(5, 5);
    player2Pos = new Point(7, 7);
}

// Create new empty map
private void newMap() {
    int result = JOptionPane.showConfirmDialog(this, 
        "Create a new map? This will clear all current data.", 
        "New Map", 
        JOptionPane.YES_NO_OPTION);
    
    if (result == JOptionPane.YES_OPTION) {
        clearMap();
        updateCamera();
        repaint();
    }
}    
    
    
private Wheelchair getWheelchairBehindPlayer(Point playerPos) {
    for (Wheelchair chair : wheelchairs) {
        // Check if wheelchair is directly behind player (one tile back)
        if ((chair.x == playerPos.x && chair.y == playerPos.y + 1) ||  // South
            (chair.x == playerPos.x + 1 && chair.y == playerPos.y) ||  // East  
            (chair.x == playerPos.x && chair.y == playerPos.y - 1) ||  // North
            (chair.x == playerPos.x - 1 && chair.y == playerPos.y)) {  // West
            return chair;
        }
    }
    return null;
}

private Wheelchair getWheelchairNearPlayer(Point playerPos) {
    for (Wheelchair chair : wheelchairs) {
        // Check if wheelchair is adjacent to player (any direction)
        int dx = Math.abs(chair.x - playerPos.x);
        int dy = Math.abs(chair.y - playerPos.y);
        
        // Adjacent means exactly 1 tile away in one direction, 0 in the other
        if ((dx == 1 && dy == 0) || (dx == 0 && dy == 1)) {
            return chair;
        }
    }
    return null;
}
    
private Point calculatePushedPosition(Point oldPlayerPos, Point newPlayerPos, Wheelchair chair) {
    int dx = newPlayerPos.x - oldPlayerPos.x;
    int dy = newPlayerPos.y - oldPlayerPos.y;
    
    return new Point(chair.x + dx, chair.y + dy);
}
    
private boolean isValidWheelchairMove(Wheelchair chair, Point newPos) {
    if (newPos.x < 0 || newPos.x >= MAP_WIDTH || newPos.y < 0 || newPos.y >= MAP_HEIGHT) {
        return false;
    }

    
    // Check for thin walls
    for (WallSegment wall : walls) {
        if (wall.blocksMovement(new Point(chair.x, chair.y), newPos)) {
            return false;
        }
    }
    
    // Check for beds
    for (Bed bed : beds) {
        if (bed.occupiesTile(newPos.x, newPos.y)) {
            return false;
        }
    }
    
    // Check for other wheelchairs
    for (Wheelchair otherChair : wheelchairs) {
        if (otherChair != chair && otherChair.x == newPos.x && otherChair.y == newPos.y) {
            return false;
        }
    }
    
    return true;
}
    
    
    
    
private void loadBackgroundMusic() {
    try {
        File wavFile = new File("background.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
        
        backgroundMusic = AudioSystem.getClip();
        backgroundMusic.open(audioStream);
        
        // Add a line listener to detect when playback stops unexpectedly
        backgroundMusic.addLineListener(new LineListener() {
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    System.out.println("Music stopped: " + event.toString());
                }
            }
        });
        
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        
    } catch (Exception e) {
        System.out.println("Could not load background music: " + e.getMessage());
        backgroundMusic = null;
    }
}
    
private void checkMusicStatus() {
    if (backgroundMusic != null && musicEnabled && !backgroundMusic.isRunning()) {
        System.out.println("Music stopped unexpectedly, restarting...");
        backgroundMusic.setFramePosition(0);
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
    }
}
    
    
}


class Bed implements Renderable {
    int x, y;
    
    public Bed(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean occupiesTile(int tileX, int tileY) {
        return tileX >= x && tileX < x + 4 && 
               tileY >= y && tileY < y + 2;
    }
    
    @Override
    public int getRenderX() { return x; }
    
    @Override
    public int getRenderY() { return y; }
    
    @Override
    public int getDepthX() { return x + 1; } // Use bottom-right corner for depth (2x4 bed)
    
    @Override
    public int getDepthY() { return y + 3; } // Use bottom-right corner for depth
    
    @Override
    public int getRenderPriority() { return 1; } // Beds render before players
    
    @Override
public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
        if (game.showSprites) {
        Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
        int bedX = isoPos.x - 18;
        int bedY = isoPos.y - 18;
        g2d.drawImage(game.bedSprite, bedX, bedY, 50, 50, null);
    }
}

    
}

class Wheelchair implements Renderable {
    int x, y;
    int direction; // 0=North, 1=East, 2=South, 3=West
    
    
    public Wheelchair(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = 0; // Default facing north
    }
    
    @Override
    public int getRenderX() { return x; }
    
    @Override
    public int getRenderY() { return y; }
    
    @Override
    public int getDepthX() { return x; }
    
    @Override
    public int getDepthY() { return y + 1; }
    
    @Override
    public int getRenderPriority() { return 2; } 
    
@Override
public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
    if (game.showSprites) {
        Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
        
        int floorDisplayWidth = CodeBlue.TILE_WIDTH;
        int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
        
        int floorX = isoPos.x - floorDisplayWidth / 2;
        int floorY = isoPos.y - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        // Select sprite based on direction
        Image sprite;
        switch (direction) {
            case 0: sprite = game.wheelchairNorthSprite; break;
            case 1: sprite = game.wheelchairEastSprite; break;
            case 2: sprite = game.wheelchairSouthSprite; break;
            case 3: sprite = game.wheelchairWestSprite; break;
            default: sprite = game.wheelchairNorthSprite; break;
        }
        
        g2d.drawImage(sprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
    }
}
}


class Player implements Renderable {
    Point pos;
    Color color;
    String label;
    
    public Player(Point pos, Color color, String label) {
        this.pos = pos;
        this.color = color;
        this.label = label;
    }
    
    @Override
    public int getRenderX() { return pos.x; }
    
    @Override
    public int getRenderY() { return pos.y; }
    
    @Override
    public int getDepthX() { return pos.x; } // Players are 1x1, same as render position
    
    @Override
    public int getDepthY() { return pos.y; }
    
    @Override
    public int getRenderPriority() { return 2; } // Players render after beds
    
    @Override
    public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
        if (game.showSprites) {
            Point isoPos = game.gridToIso(pos.x, pos.y, offsetX, offsetY);

            g2d.setColor(color);
            int playerSize = 6;
            g2d.fillOval(isoPos.x - playerSize/2, isoPos.y - playerSize/2, playerSize, playerSize);
        }
    }
}

class WallSegment implements Renderable {
    public enum Type {
        DIAGONAL_NE, 
        DIAGONAL_NW,
        DIAGONAL_NW_short,
        CORNER_NORTH,
        CORNER_SOUTH,
    }
    
    int gridX, gridY;
    Type type;
    
    public WallSegment(int gridX, int gridY, Type type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.type = type;
    }
    
    // Implement Renderable interface
    @Override
    public int getRenderX() { return gridX; }
    
    @Override
    public int getRenderY() { return gridY; }
    
@Override
public int getDepthX() { 
    if (type == Type.CORNER_SOUTH) {
        return gridX + 1; // Move forward in depth
    }
    return gridX; 
}

@Override
public int getDepthY() { 
    if (type == Type.CORNER_SOUTH) {
        return gridY + 1; // Move forward in depth
    }
    return gridY; 
}
    
    @Override
    public int getRenderPriority() { return 0; } // Walls render before beds and players
    
    // Your existing blocksMovement method stays the same
    public boolean blocksMovement(Point from, Point to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        
        if (type == Type.DIAGONAL_NE) {
            if (from.x == gridX && from.y == gridY && to.x == gridX - 1 && to.y == gridY) {
                return true;
            }
            if (from.x == gridX - 1 && from.y == gridY && to.x == gridX && to.y == gridY) {
                return true;
            }
        } else if (type == Type.DIAGONAL_NW || type == Type.DIAGONAL_NW_short) {
            if (from.x == gridX && from.y == gridY && to.x == gridX && to.y == gridY - 1) {
                return true;
            }
            if (from.x == gridX && from.y == gridY - 1 && to.x == gridX && to.y == gridY) {
                return true;
            }
        } else if (type == Type.CORNER_NORTH) {
            if (from.x == gridX && from.y == gridY && to.x == gridX - 1 && to.y == gridY) {
                return true;
            }
            if (from.x == gridX - 1 && from.y == gridY && to.x == gridX && to.y == gridY) {
                return true;
            }
            if (from.x == gridX && from.y == gridY && to.x == gridX && to.y == gridY - 1) {
                return true;
            }
            if (from.x == gridX && from.y == gridY - 1 && to.x == gridX && to.y == gridY) {
                return true;
            }
        } else if (type == Type.CORNER_SOUTH) {
    // Block east/west movement (moved 1 tile east from the corner position)
    if (from.x == gridX + 1 && from.y == gridY && to.x == gridX && to.y == gridY) {
        return true;
    }
    if (from.x == gridX && from.y == gridY && to.x == gridX + 1 && to.y == gridY) {
        return true;
    }
    // Block north/south movement (at the corner position)
    if (from.x == gridX && from.y == gridY && to.x == gridX && to.y == gridY + 1) {
        return true;
    }
    if (from.x == gridX && from.y == gridY + 1 && to.x == gridX && to.y == gridY) {
        return true;
    }
}
        
        return false;
    }
    
    // Your existing render method becomes the Renderable render method
    @Override
    public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
        Point isoPos = CodeBlue.gridToIso(gridX, gridY, offsetX, offsetY);
        
        int wallDisplayWidth = CodeBlue.TILE_WIDTH;
        int wallDisplayHeight = (int)(wallDisplayWidth * (501.0 / 320.0));
        
        int wallX = isoPos.x - wallDisplayWidth / 2;
        int wallY = isoPos.y - wallDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        Image currentWallSprite;
        
        if (type == Type.DIAGONAL_NE) {
            currentWallSprite = game.wallNESWSprite;
        } else if (type == Type.DIAGONAL_NW) {
            currentWallSprite = game.wallNWSESprite;
        } else if (type == Type.DIAGONAL_NW_short) {
            currentWallSprite = game.wallNWSEShortSprite;  
        } else if (type == Type.CORNER_NORTH) {
            currentWallSprite = game.wallCornerNorthSprite;
        } else if (type == Type.CORNER_SOUTH) {
            currentWallSprite = game.wallCornerSouthSprite;
        } else {
            return;
        }
        
boolean shouldBeTransparent = game.shouldWallBeTransparent(gridX, gridY, type);
        
    if (shouldBeTransparent) {
        // Save original composite
        Composite originalComposite = g2d.getComposite();
        
        // Set transparency (0.3f = 30% opacity, adjust as needed)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        
        g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
        
        // Restore original composite
        g2d.setComposite(originalComposite);
    } else {
        // Draw normally
        g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
    }
        if (!game.showTileCoordinates) return;
        g2d.setFont(new Font("Arial", Font.BOLD, 2));
        FontMetrics fm = g2d.getFontMetrics();
        String coordText = "(" + this.gridX + "," + this.gridY + ")";
        int textWidth = fm.stringWidth(coordText);
        int textHeight = fm.getAscent();
        g2d.drawString(coordText, isoPos.x - textWidth/2, isoPos.y + textHeight/2 - 2);
    }
}

class WallPreview {
    private Point gridPos;
    private WallSegment.Type type;
    
    public WallPreview(Point gridPos, WallSegment.Type type) {
        this.gridPos = gridPos;
        this.type = type;
    }
    
    public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
        Point isoPos = CodeBlue.gridToIso(gridPos.x, gridPos.y, offsetX, offsetY);
        
        // Save original composite
        Composite originalComposite = g2d.getComposite();
        
        // Set semi-transparent
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        int wallDisplayWidth = CodeBlue.TILE_WIDTH;
        int wallDisplayHeight = (int)(wallDisplayWidth * (524.0 / 304.0));
        
        int wallX = isoPos.x - wallDisplayWidth / 2;
        int wallY = isoPos.y - wallDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        Image currentWallSprite;
        
        if (type == WallSegment.Type.DIAGONAL_NE) {
            currentWallSprite = game.wallNESWSprite;
        } else if (type == WallSegment.Type.DIAGONAL_NW) {
            currentWallSprite = game.wallNWSESprite;
        }  else if (type == WallSegment.Type.DIAGONAL_NW_short) {
            currentWallSprite = game.wallNWSEShortSprite;  
        }  else if (type == WallSegment.Type.CORNER_NORTH) {
            currentWallSprite = game.wallCornerNorthSprite;
        }   else if (type == WallSegment.Type.CORNER_SOUTH) {
            currentWallSprite = game.wallCornerSouthSprite;
        }   else {
            g2d.setComposite(originalComposite);
            return;
        }
        
        g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
        
        // Optional: Draw a colored border to indicate it's a preview
        g2d.setComposite(originalComposite);
        g2d.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
        game.setConstantThicknessStroke(g2d, 3.0f);
        g2d.drawRect(wallX, wallY, wallDisplayWidth, wallDisplayHeight);
        g2d.setStroke(new BasicStroke(1.0f));
    }
}

class FloorTile implements Renderable {
    int x, y;
    
    public FloorTile(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public int getRenderX() { return x; }
    
    @Override
    public int getRenderY() { return y; }
    
    @Override
    public int getDepthX() { return x; }
    
    @Override
    public int getDepthY() { return y; }
    
    @Override
    public int getRenderPriority() { return -1; } // Render before walls (under everything)
    
@Override
public void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game) {
    if (game.showSprites) {
        Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
        
        // Use the same dimensions as thin walls
        int floorDisplayWidth = CodeBlue.TILE_WIDTH;
        int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0)); // Same ratio as walls
        
        // Use the same positioning as thin walls
        int floorX = isoPos.x - floorDisplayWidth / 2;
        int floorY = isoPos.y - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        g2d.drawImage(game.floorSprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
        
        if (!game.showTileCoordinates) return;
        g2d.setFont(new Font("Arial", Font.BOLD, 2));
        FontMetrics fm = g2d.getFontMetrics();
        String coordText = "(" + this.x + "," + this.y + ")";
        int textWidth = fm.stringWidth(coordText);
        int textHeight = fm.getAscent();
        g2d.drawString(coordText, isoPos.x - textWidth/2, isoPos.y + textHeight/2 - 2);
    }
}
}                