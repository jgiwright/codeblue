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
    void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game);
}

public class CodeBlue extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    public static final int TILE_WIDTH = 16;
    public static final int TILE_HEIGHT = 8;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;
    
    Player player1;
    Player player2;
    
    // Player positions (in grid coordinates)
    private Point player1Pos = new Point(5, 5);
    private Point player2Pos = new Point(7, 7);
    
private double player1X = 5.0, player1Y = 5.0;
private double player2X = 7.0, player2Y = 7.0;
private static final double MOVE_SPEED = 0.4; // Tiles per frame

// Keep grid positions for collision detection
private Point player1GridPos = new Point(5, 5);
private Point player2GridPos = new Point(7, 7);
    
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 50; // milliseconds between moves
    
private long lastUpdateTime = System.nanoTime();
private static final double TILES_PER_SECOND = 10.0; // Target speed
    
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
public Image wallNESWSpriteFloor, wallNESWSpriteWall;
public Image wallNWSESpriteFloor, wallNWSESpriteWall;   
public Image wallNWSEShortSpriteFloor, wallNWSEShortSpriteWall;  
public Image wallCornerNorthSpriteFloor, wallCornerNorthSpriteWall;
public Image wallCornerSouthSpriteFloor, wallCornerSouthSpriteWall;
    
    public Image wheelchairNorthSprite;
    public Image wheelchairEastSprite;
    public Image wheelchairSouthSprite;
    public Image wheelchairWestSprite;
    
    public Image drawerSWSprite;
    
    public Image playerNorthSprite;
    Image[] playerCprSprites;
    
    public boolean showSprites = true;
    private boolean showDepthDebug = false;
    
    private java.util.List<WallSegment> walls = new ArrayList<>();
    private boolean isThinWallMode = false;
    private WallSegment.Type currentThinWallType = WallSegment.Type.DIAGONAL_NW;
    
    private java.util.List<FloorTile> placedFloorTiles = new ArrayList<>();
    private boolean isFloorMode = false;
    
    private java.util.List<Wheelchair> wheelchairs = new ArrayList<>();
    
    private java.util.List<Drawer> drawers = new ArrayList<>();
    
    private boolean isPushingWheelchair = false;
    private Wheelchair pushedWheelchair = null;
    
    private Clip backgroundMusic;
    private boolean musicEnabled = true;
    
private double lastPlayer1X = 0;
private double lastPlayer1Y = 0;
private Point lastPlayerScreenPos = new Point(0, 0);
private int logCounter = 0;
    public double player1ImgPosX;
    public double player1ImgPosY;
    
    private Patient patient01 = new Patient(10,10,"crocodile","john","MI");
    
public enum PlaceableType {
    FLOOR_TILE,
    FLOOR_WALL_NE_SW,
    FLOOR_WALL_NW_SE,
    FLOOR_WALL_NW_SE_SHORT,
    FLOOR_WALL_CORNER_NORTH,
    FLOOR_WALL_CORNER_SOUTH,
    THIN_WALL_NE,
    THIN_WALL_NW, 
    THIN_WALL_NW_SHORT,
    THIN_WALL_CORNER_NORTH,
    THIN_WALL_CORNER_SOUTH,
    BED,
    WHEELCHAIR,
    DRAWER_SW,
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
        
        player1 = new Player(5, 5, Color.BLUE, "P1");
        player2 = new Player(10, 10, Color.RED, "P2");
        
        
    }

    
private void loadSprites() {
    try {
        bedSprite = Toolkit.getDefaultToolkit().getImage("bed.png");
        floorSprite = Toolkit.getDefaultToolkit().getImage("floor.png");
        
        wallNESWSpriteFloor = Toolkit.getDefaultToolkit().getImage("wall_NE-SW_floor.png");
        wallNESWSpriteWall = Toolkit.getDefaultToolkit().getImage("wall_NE-SW_wall.png");
        wallNWSESpriteFloor = Toolkit.getDefaultToolkit().getImage("wall_NW-SE_floor.png");
        wallNWSESpriteWall = Toolkit.getDefaultToolkit().getImage("wall_NW-SE_wall.png");
        wallNWSEShortSpriteFloor = Toolkit.getDefaultToolkit().getImage("wall_NW-SE_short_floor.png");
        wallNWSEShortSpriteWall = Toolkit.getDefaultToolkit().getImage("wall_NW-SE_short_wall.png");
        wallCornerNorthSpriteFloor = Toolkit.getDefaultToolkit().getImage("wall_corner_north_floor.png");
        wallCornerNorthSpriteWall = Toolkit.getDefaultToolkit().getImage("wall_corner_north_wall.png");
        wallCornerSouthSpriteFloor = Toolkit.getDefaultToolkit().getImage("wall_corner_south_floor.png");
        wallCornerSouthSpriteWall = Toolkit.getDefaultToolkit().getImage("wall_corner_south_wall.png");
        
        wheelchairNorthSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_north.png");
        wheelchairEastSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_east.png");
        wheelchairSouthSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_south.png");
        wheelchairWestSprite = Toolkit.getDefaultToolkit().getImage("wheelchair_west.png");
        
        drawerSWSprite = Toolkit.getDefaultToolkit().getImage("drawerSW.png");
        
        playerNorthSprite = Toolkit.getDefaultToolkit().getImage("player_north.png");
        
        playerCprSprites = new Image[] {
            Toolkit.getDefaultToolkit().getImage("sprites/0001.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0002.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0003.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0004.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0005.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0006.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0007.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0008.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0009.png"),
             Toolkit.getDefaultToolkit().getImage("sprites/0010.png"),
            // Add as many frames as you have
        };
        
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(bedSprite, 0);
        tracker.addImage(floorSprite, 1);
        
        // Add floor sprites to tracker
        tracker.addImage(wallNESWSpriteFloor, 2);
        tracker.addImage(wallNWSESpriteFloor, 3);
        tracker.addImage(wallNWSEShortSpriteFloor, 4);
        tracker.addImage(wallCornerNorthSpriteFloor, 5);
        tracker.addImage(wallCornerSouthSpriteFloor, 6);
        
        // Add wall sprites to tracker
        tracker.addImage(wallNESWSpriteWall, 7);
        tracker.addImage(wallNWSESpriteWall, 8);
        tracker.addImage(wallNWSEShortSpriteWall, 9);
        tracker.addImage(wallCornerNorthSpriteWall, 10);
        tracker.addImage(wallCornerSouthSpriteWall, 11);
        
        tracker.addImage(wheelchairNorthSprite, 12);
        tracker.addImage(wheelchairEastSprite, 13);
        tracker.addImage(wheelchairSouthSprite, 14);
        tracker.addImage(wheelchairWestSprite, 15);
        
        tracker.addImage(drawerSWSprite, 16);
        
        tracker.addImage(playerNorthSprite, 17);
        tracker.waitForAll();
    } catch (Exception e) {
        bedSprite = createPlaceholderBed();
        floorSprite = createPlaceholderFloor();
        wallNESWSpriteFloor = createPlaceholderFloor();
        wallNESWSpriteWall = createPlaceholderWall();
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
    double offsetX = WINDOW_WIDTH / 2.0 / zoomLevel - cameraPos.x;
    double offsetY = WINDOW_HEIGHT / 2.0 / zoomLevel - cameraPos.y;
    
    drawFloor(g2d, offsetX, offsetY);
    
    // Collect all renderable objects INCLUDING thin walls
    List<Renderable> renderables = new ArrayList<>();
    renderables.addAll(placedFloorTiles);
    renderables.addAll(beds);
    renderables.addAll(walls); // Add thin walls to depth sorting
    renderables.add(player1);
    renderables.add(player2);
    renderables.addAll(wheelchairs);
    renderables.addAll(drawers);
    renderables.add(patient01);
    

    // Enhanced isometric depth sorting
    renderables.sort((a, b) -> {
        // Floor tiles always render first
        if (a instanceof FloorTile && !(b instanceof FloorTile)) {
            return -1; // a renders before b
        }
        if (b instanceof FloorTile && !(a instanceof FloorTile)) {
            return 1; // b renders before a
        }
        
        // Both are floor tiles or both are non-floor tiles - use depth sorting
        double depthA = calculateIsometricDepth(a);
        double depthB = calculateIsometricDepth(b);
        
        // If depths are very close, use render priority
        if (Math.abs(depthA - depthB) < 0.001) {
            return Integer.compare(a.getRenderPriority(), b.getRenderPriority());
        }
        
        return Double.compare(depthA, depthB);
    });
    
    // Render all objects in depth order
    for (Renderable obj : renderables) {
        obj.render(g2d, offsetX, offsetY, this);
    }
    
    // Draw wall preview AFTER all other objects so it's always visible
    if (showPreview && mouseGridPos != null) {
        switch (currentPlaceableType) {
      case FLOOR_TILE:
        case FLOOR_WALL_NE_SW:
        case FLOOR_WALL_NW_SE:
        case FLOOR_WALL_NW_SE_SHORT:
        case FLOOR_WALL_CORNER_NORTH:
        case FLOOR_WALL_CORNER_SOUTH:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == mouseGridPos.x && floor.y == mouseGridPos.y)) {
                
                // Get the appropriate floor sprite based on type
                Image previewFloorSprite = null;
                
                switch (currentPlaceableType) {
                    case FLOOR_TILE:
                        previewFloorSprite = floorSprite;
                        break;
                    case FLOOR_WALL_NE_SW:
                        previewFloorSprite = wallNESWSpriteFloor;
                        break;
                    case FLOOR_WALL_NW_SE:
                        previewFloorSprite = wallNWSESpriteFloor;
                        break;
                    case FLOOR_WALL_NW_SE_SHORT:
                        previewFloorSprite = wallNWSEShortSpriteFloor;
                        break;
                    case FLOOR_WALL_CORNER_NORTH:
                        previewFloorSprite = wallCornerNorthSpriteFloor;
                        break;
                    case FLOOR_WALL_CORNER_SOUTH:
                        previewFloorSprite = wallCornerSouthSpriteFloor;
                        break;
                }
                
                // Render floor preview with appropriate sprite
                if (previewFloorSprite != null) {
                    Point isoPos = gridToIso(mouseGridPos.x, mouseGridPos.y, offsetX, offsetY);
                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    
                    int floorDisplayWidth = TILE_WIDTH;
                    int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
                    int floorX = isoPos.x - floorDisplayWidth / 2;
                    int floorY = isoPos.y - floorDisplayHeight + TILE_HEIGHT / 2;
                    g2d.drawImage(previewFloorSprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
                    
                    g2d.setComposite(originalComposite);
                }
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
    

private double calculateIsometricDepth(Renderable obj) {
    if (obj instanceof Player) {
        Player p = (Player) obj;
        // For players, use their exact position without artificial offset
        return p.x + p.y + 1.5;
    } else if (obj instanceof FloorTile) {
        // Floor tiles should always be at the back
        return obj.getDepthX() + obj.getDepthY() - 0.5;
    } else if (obj instanceof WallSegment) {
        WallSegment wall = (WallSegment) obj;
        // Walls need special handling based on their type
        double baseDepth = wall.getDepthX() + wall.getDepthY();
        
        switch (wall.type) {
            case DIAGONAL_NE:
                return baseDepth + 0.1;
            case DIAGONAL_NW:
            case DIAGONAL_NW_short:
                return baseDepth + 0.1;
            case CORNER_NORTH:
                return baseDepth;
            case CORNER_SOUTH:
                return baseDepth + 0.2;
            default:
                return baseDepth;
        }
    } else {
        // Default for beds, wheelchairs, etc.
        return obj.getDepthX() + obj.getDepthY();
    }
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
  
    
private void drawFloor(Graphics2D g2d, double offsetX, double offsetY) {
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
    
public static Point gridToIso(int gridX, int gridY, double offsetX, double offsetY) {
    // Keep full precision until the final rounding
    double isoX = (gridX - gridY) * TILE_WIDTH / 2.0 + offsetX;
    double isoY = (gridX + gridY) * TILE_HEIGHT / 2.0 + offsetY;
    
    // Round to nearest pixel for final display
    return new Point((int)Math.round(isoX), (int)Math.round(isoY));
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
    
    // Calculate the same offset values used in rendering
    double offsetX = WINDOW_WIDTH / 2.0 / zoomLevel - cameraPos.x;
    double offsetY = WINDOW_HEIGHT / 2.0 / zoomLevel - cameraPos.y;

    
    String[] instructions = {
        "Player 1 (Red): WASD to move",
        "Player 2 (Blue): Arrow keys to move", 
        "Player 1 Position: (" + String.format("%.4f", player1.x) + ", " + String.format("%.4f", player1.y) + ")",
        "Player 1 Img Position: (" + String.format("%.4f", player1ImgPosX) + ", " + String.format("%.4f", player1ImgPosY) + ")",
        "Camera Position: (" + String.format("%.4f", cameraPos.x) + ", " + String.format("%.4f", cameraPos.y) + ")",
        "Offset: (" + String.format("%.4f", offsetX) + ", " + String.format("%.4f", offsetY) + ")",
        "Camera Lerp: " + (cameraLerp ? "[ON]" : "[OFF]") + " (Press L to toggle)",
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
    long currentTime = System.nanoTime();
    double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // Convert to seconds
    lastUpdateTime = currentTime;
    
    player1.update(deltaTime);
    
    double moveDistance = TILES_PER_SECOND * deltaTime;
    
    boolean moved = false;
    
    // Player 1 movement with wheelchair pushing
    if (isPushingWheelchair && pushedWheelchair != null) {
        double newX1 = player1.x, newY1 = player1.y; 
        boolean player1Wants2Move = false;
        Point movementDirection = new Point(0, 0);
        
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            newY1 -= moveDistance;
            movementDirection = new Point(0, -1);
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_S)) {
            newY1 += moveDistance;
            movementDirection = new Point(0, 1);
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_A)) {
            newX1 -= moveDistance;
            movementDirection = new Point(-1, 0);
            player1Wants2Move = true;
        } else if (pressedKeys.contains(KeyEvent.VK_D)) {
            newX1 += moveDistance;
            movementDirection = new Point(1, 0);
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
            
    double newChairX = newX1 + movementDirection.x;
    double newChairY = newY1 + movementDirection.y;
    
    // Use grid positions for collision detection
    Point newGridPos1 = new Point((int)Math.round(newX1), (int)Math.round(newY1));
    Point newChairGridPos = new Point((int)Math.round(newChairX), (int)Math.round(newChairY));
    
    Point currentGridPos1 = new Point((int)Math.round(player1.x), (int)Math.round(player1.y));
    if (isValidMove(currentGridPos1, newGridPos1) && 
        isValidWheelchairMove(pushedWheelchair, newChairGridPos) &&
        !newGridPos1.equals(new Point((int)Math.round(player2.x), (int)Math.round(player2.y))) &&
        !newChairGridPos.equals(new Point((int)Math.round(player2.x), (int)Math.round(player2.y)))) {
        
        // Apply snapping to both player and wheelchair
        player1.x = snapToGrid(newX1, 0.25);
        player1.y = snapToGrid(newY1, 0.25);
        player1GridPos = newGridPos1;
        
        // Keep wheelchair at integer grid positions (they don't need subtile movement)
        pushedWheelchair.x = newChairGridPos.x;
        pushedWheelchair.y = newChairGridPos.y;
        moved = true;
    }
        }
    } else {
        // Normal player 1 movement
        double newX1 = player1.x, newY1 = player1.y;
        boolean player1Moved = false;
        
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            newY1 -= moveDistance;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_S)) {
            newY1 += moveDistance;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_A)) {
            newX1 -= moveDistance;
            player1Moved = true;
        } else if (pressedKeys.contains(KeyEvent.VK_D)) {
            newX1 += moveDistance;
            player1Moved = true;
        }
        
  if (player1Moved) {
            // Use current actual position for collision detection
            Point currentGridPos1 = new Point((int)Math.round(player1.x), (int)Math.round(player1.y));
            Point newGridPos1 = new Point((int)Math.round(newX1), (int)Math.round(newY1));
            
            // Only check collision if actually moving to a different grid tile
            if (currentGridPos1.equals(newGridPos1) || 
                (isValidMove(currentGridPos1, newGridPos1) && 
                 !newGridPos1.equals(new Point((int)Math.round(player2.x), (int)Math.round(player2.y))))) {
                
                player1.x = newX1;
                player1.y = newY1;
                player1GridPos = newGridPos1;
                moved = true;
            }
            // If move is invalid, don't update position at all (no bouncing back)
        }
    }
    
    // Player 2 movement (normal movement only)
    double newX2 = player2.x, newY2 = player2.y; 
    boolean player2Moved = false;
    
    if (pressedKeys.contains(KeyEvent.VK_UP)) {
        newY2 -= MOVE_SPEED;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
        newY2 += MOVE_SPEED;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
        newX2 -= MOVE_SPEED;
        player2Moved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
        newX2 += MOVE_SPEED;
        player2Moved = true;
    }
    
    if (player2Moved) {
        Point newGridPos2 = new Point((int)Math.round(newX2), (int)Math.round(newY2));
        
        if (isValidMove(player2GridPos, newGridPos2) && 
            !newGridPos2.equals(new Point((int)Math.round(player1.x), (int)Math.round(player1.y)))) {
            player2.x = newX2;
            player2.y = newY2;
            player2GridPos = newGridPos2;
            moved = true;
        }
    }
    
    boolean patientMoved = false;
    
    if (pressedKeys.contains(KeyEvent.VK_NUMPAD8) && !patientMoved) {
        patient01.move(KeyEvent.VK_NUMPAD8, moveDistance);
        moved = true;
        patientMoved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_NUMPAD2) && !patientMoved) {
        patient01.move(KeyEvent.VK_NUMPAD2, moveDistance);
        moved = true;
        patientMoved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_NUMPAD4) && !patientMoved) {
        patient01.move(KeyEvent.VK_NUMPAD4, moveDistance);
        moved = true;
        patientMoved = true;
    } else if (pressedKeys.contains(KeyEvent.VK_NUMPAD6) && !patientMoved) {
        patient01.move(KeyEvent.VK_NUMPAD6, moveDistance);
        moved = true;
        patientMoved = true;
    }
    
    if (moved) {
        updateCamera();
        
    }
    repaint();
}
    
private void updateCamera() {
    double targetGridX = (player1.x + player2.x) / 2.0;
    double targetGridY = (player1.y + player2.y) / 2.0;
    
    double isoTargetX = (targetGridX - targetGridY) * TILE_WIDTH / 2.0;
    double isoTargetY = (targetGridX + targetGridY) * TILE_HEIGHT / 2.0;
    
    cameraPos.x = isoTargetX;
    cameraPos.y = isoTargetY;
    
    
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
    
    for (Drawer drawer : drawers) {
        if (drawer.x == to.x && drawer.y == to.y) {
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
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y, FloorTile.FloorType.REGULAR));
            }
            break;
            
        case FLOOR_WALL_NE_SW:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == gridPos.x && floor.y == gridPos.y)) {
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y, FloorTile.FloorType.WALL_NE_SW));
                 placeThinWall(gridPos, WallSegment.Type.DIAGONAL_NE);
            }
            break;
            
        case FLOOR_WALL_NW_SE:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == gridPos.x && floor.y == gridPos.y)) {
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y, FloorTile.FloorType.WALL_NW_SE));
                placeThinWall(gridPos, WallSegment.Type.DIAGONAL_NW);
            }
            break;
            
            
        case FLOOR_WALL_CORNER_NORTH:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == gridPos.x && floor.y == gridPos.y)) {
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y, FloorTile.FloorType.WALL_CORNER_NORTH));
                 placeThinWall(gridPos, WallSegment.Type.CORNER_NORTH);
            }
            break;
            
        case FLOOR_WALL_CORNER_SOUTH:
            if (!placedFloorTiles.stream().anyMatch(floor -> 
                floor.x == gridPos.x && floor.y == gridPos.y)) {
                placedFloorTiles.add(new FloorTile(gridPos.x, gridPos.y, FloorTile.FloorType.WALL_CORNER_SOUTH));
                 placeThinWall(gridPos, WallSegment.Type.CORNER_SOUTH);
            }
            break;
            
        case DRAWER_SW:
            if (!drawers.stream().anyMatch(drawer -> 
                drawer.x == gridPos.x && drawer.y == gridPos.y)) {
                drawers.add(new Drawer(gridPos.x, gridPos.y));
            }
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
    
    drawers.removeIf(drawer -> 
        drawer.x == gridPos.x && drawer.y == gridPos.y);
    

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
        // Pass floating-point coordinates instead of Point objects
        Wheelchair chair1 = getWheelchairNearPlayer(player1.x, player1.y);
        Wheelchair chair2 = getWheelchairNearPlayer(player2.x, player2.y);
        
        if (chair1 != null) {
            isPushingWheelchair = true;
            pushedWheelchair = chair1;
            System.out.println("Started pushing wheelchair near player 1");
        } else if (chair2 != null) {
            isPushingWheelchair = true;
            pushedWheelchair = chair2;
            System.out.println("Started pushing wheelchair near player 2");
        } else {
            System.out.println("No wheelchair found near either player");
        }
    } else {
        isPushingWheelchair = false;
        pushedWheelchair = null;
        System.out.println("Stopped pushing wheelchair");
    }
}
        
        if (e.getKeyCode() == KeyEvent.VK_Z) {
            player1.performCPR(10);
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
        javax.swing.Timer gameTimer = new javax.swing.Timer(16, e -> game.updateGame());
        gameTimer.start();
    }
    
    
    
public boolean shouldWallBeTransparent(int wallX, int wallY, WallSegment.Type wallType) {
    // Use floating-point player positions and round them for comparison
    Point[] playerPositions = {
        new Point((int)Math.round(player1.x), (int)Math.round(player1.y)),
        new Point((int)Math.round(player2.x), (int)Math.round(player2.y))
    };
    
    for (Point playerPos : playerPositions) {
        int dx = wallX - playerPos.x;
        int dy = wallY - playerPos.y;
        
        // Define exactly which relative positions make walls transparent
        boolean shouldBeTransparent = 
            (dx == 1 && dy == 0 && wallType == WallSegment.Type.DIAGONAL_NE) ||  
            (dx == 0 && dy == 1 && wallType == WallSegment.Type.DIAGONAL_NW) ||  
            (dx == 1 && dy == 1) ||  
            (dx == 2 && dy == 1) ||  
            (dx == 1 && dy == 2);    
        
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
        
        // Write camera and zoom settings
        writer.println("# Camera and Zoom Settings");
        writer.println("CAMERA_X=" + cameraPos.x);
        writer.println("CAMERA_Y=" + cameraPos.y);
        writer.println("ZOOM_LEVEL=" + zoomLevel);
        writer.println("CAMERA_LERP=" + cameraLerp);
        writer.println();
        
        // Write player positions (using floating point coordinates)
        writer.println("# Player Positions");
        writer.println("PLAYER1=" + player1.x + "," + player1.y);
        writer.println("PLAYER2=" + player2.x + "," + player2.y);
        writer.println();
        
        // Write thin walls
        writer.println("# Thin Walls (x,y,type)");
        for (WallSegment wall : walls) {
            writer.println("THIN_WALL=" + wall.gridX + "," + wall.gridY + "," + wall.type.toString());
        }
        writer.println();
        
        // Write floor tiles
        writer.println("# Floor Tiles (x,y,type)");
        for (FloorTile floor : placedFloorTiles) {
            writer.println("FLOOR=" + floor.x + "," + floor.y + "," + floor.floorType.toString());
        }
        writer.println();
        
        // Write beds
        writer.println("# Beds (x,y)");
        for (Bed bed : beds) {
            writer.println("BED=" + bed.x + "," + bed.y);
        }
        writer.println();

        // Write wheelchairs
        writer.println("# Wheelchairs (x,y,direction)");
        for (Wheelchair chair : wheelchairs) {
            writer.println("WHEELCHAIR=" + chair.x + "," + chair.y + "," + chair.direction);
        }
        writer.println();
        
        writer.println("# Drawers (x,y)");
        for (Drawer drawer : drawers) {
            writer.println("DRAWER=" + drawer.x + "," + drawer.y);
        }

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
                player1.x = Double.parseDouble(coords[0]);
                player1.y = Double.parseDouble(coords[1]);
                // Update grid position and Point object for compatibility
                player1GridPos = new Point((int)Math.round(player1.x), (int)Math.round(player1.y));
                player1Pos = new Point((int)Math.round(player1.x), (int)Math.round(player1.y));
                
            } else if (line.startsWith("PLAYER2=")) {
                String[] coords = line.substring(8).split(",");
                player2.x = Double.parseDouble(coords[0]);
                player2.y = Double.parseDouble(coords[1]);
                // Update grid position and Point object for compatibility
                player2GridPos = new Point((int)Math.round(player2.x), (int)Math.round(player2.y));
                player2Pos = new Point((int)Math.round(player2.x), (int)Math.round(player2.y));
                
            } else if (line.startsWith("CAMERA_X=")) {
                cameraPos.x = Double.parseDouble(line.substring(9));
                
            } else if (line.startsWith("CAMERA_Y=")) {
                cameraPos.y = Double.parseDouble(line.substring(9));
                
            } else if (line.startsWith("ZOOM_LEVEL=")) {
                zoomLevel = Double.parseDouble(line.substring(11));
                // Clamp zoom to valid range
                zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel));
                
            } else if (line.startsWith("CAMERA_LERP=")) {
                cameraLerp = Boolean.parseBoolean(line.substring(12));
                
            } else if (line.startsWith("THIN_WALL=")) {
                String[] parts = line.substring(10).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                WallSegment.Type type = WallSegment.Type.valueOf(parts[2]);
                walls.add(new WallSegment(x, y, type));
                
            } else if (line.startsWith("FLOOR=")) {
                String[] parts = line.substring(6).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                FloorTile.FloorType type = parts.length > 2 ? 
                    FloorTile.FloorType.valueOf(parts[2]) : FloorTile.FloorType.REGULAR;
                placedFloorTiles.add(new FloorTile(x, y, type));
            }else if (line.startsWith("BED=")) {
                String[] coords = line.substring(4).split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                beds.add(new Bed(x, y));
                
            } else if (line.startsWith("WHEELCHAIR=")) {
                String[] parts = line.substring(11).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int dir = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                Wheelchair chair = new Wheelchair(x, y);
                chair.direction = dir;
                wheelchairs.add(chair);
            }
else if (line.startsWith("DRAWER=")) {
    String[] coords = line.substring(7).split(",");
    int x = Integer.parseInt(coords[0]);
    int y = Integer.parseInt(coords[1]);
    drawers.add(new Drawer(x, y));
}

        }
        
        // Don't call updateCamera() - use the loaded camera position instead
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
    drawers.clear();
    
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

private Wheelchair getWheelchairNearPlayer(double playerX, double playerY) {
    // Convert floating-point player position to grid for comparison
    int playerGridX = (int)Math.round(playerX);
    int playerGridY = (int)Math.round(playerY);
    
    for (Wheelchair chair : wheelchairs) {
        // Check if wheelchair is adjacent to player
        int dx = Math.abs(chair.x - playerGridX);
        int dy = Math.abs(chair.y - playerGridY);
        
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
        File wavFile = new File("BeepBox-Song.wav");
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
    

    
    public double snapToGrid(double value, double gridSize) {
    return Math.round(value / gridSize) * gridSize;
}
    
    
}


class Drawer implements Renderable {
    int x, y;
    
    public Drawer(int x, int y) {
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
    public int getDepthY() { return y + 1; } // Slightly forward in depth
    
    @Override
    public int getRenderPriority() { return 1; } // Same as beds
    
    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        if (game.showSprites) {
            Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
            
            // Drawer display size - adjust as needed for your sprite
            int drawerDisplayWidth = CodeBlue.TILE_WIDTH;
            int drawerDisplayHeight = (int)(drawerDisplayWidth * (501.0 / 320.0)); // Adjust ratio for your sprite
            
            int drawerX = isoPos.x - drawerDisplayWidth / 2;
            int drawerY = isoPos.y - drawerDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
            
            g2d.drawImage(game.drawerSWSprite, drawerX, drawerY, drawerDisplayWidth, drawerDisplayHeight, null);
            
            // Debug coordinates
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


class Bed implements Renderable {
    int x, y;
    
    public Bed(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean occupiesTile(int tileX, int tileY) {
        return tileX >= x && tileX < x + 2 && 
               tileY >= y && tileY < y + 1;
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
public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {

    if (game.showSprites) {
        Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
        
        // Beds now occupy 1x2 tiles - make them slightly wider than single tile
        int bedDisplayWidth = (int)(CodeBlue.TILE_WIDTH * 2); // 1.5x wider 
        int bedDisplayHeight = (int)(bedDisplayWidth * (320.0 / 501.0));
        
        int bedX = isoPos.x - bedDisplayWidth / 2;
        int bedY = isoPos.y - bedDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        Image currentBedSprite = game.bedSprite;
        
        if (currentBedSprite != null) {
            g2d.drawImage(currentBedSprite, bedX, bedY, bedDisplayWidth, bedDisplayHeight, null);
        }
        
        // Debug coordinates remain the same
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
public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
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
    
    // Render method - only renders wall sprite, no floor
    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        Point isoPos = CodeBlue.gridToIso(gridX, gridY, offsetX, offsetY);
        
        int wallDisplayWidth = CodeBlue.TILE_WIDTH;
        int wallDisplayHeight = (int)(wallDisplayWidth * (501.0 / 320.0));
        
        int wallX = isoPos.x - wallDisplayWidth / 2;
        int wallY = isoPos.y - wallDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        // Get wall sprite based on type (no floor sprite)
        Image currentWallSprite = null;
        
        if (type == Type.DIAGONAL_NE) {
            currentWallSprite = game.wallNESWSpriteWall;
        } else if (type == Type.DIAGONAL_NW) {
            currentWallSprite = game.wallNWSESpriteWall;
        } else if (type == Type.DIAGONAL_NW_short) {
            currentWallSprite = game.wallNWSEShortSpriteWall;
        } else if (type == Type.CORNER_NORTH) {
            currentWallSprite = game.wallCornerNorthSpriteWall;
        } else if (type == Type.CORNER_SOUTH) {
            currentWallSprite = game.wallCornerSouthSpriteWall;
        } else {
            return;
        }
        
        // Draw only the wall sprite with transparency if needed
        if (currentWallSprite != null) {
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
                // Draw wall normally
                g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
            }
        }
        
        // Debug coordinates
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
    
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        Point isoPos = CodeBlue.gridToIso(gridPos.x, gridPos.y, offsetX, offsetY);
        
        int wallDisplayWidth = CodeBlue.TILE_WIDTH;
        int wallDisplayHeight = (int)(wallDisplayWidth * (501.0 / 320.0));
        
        int wallX = isoPos.x - wallDisplayWidth / 2;
        int wallY = isoPos.y - wallDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        
        // Get floor and wall sprites based on type
        Image currentWallFloorSprite = null;
        Image currentWallSprite = null;
        
        if (type == WallSegment.Type.DIAGONAL_NE) {
            currentWallFloorSprite = game.wallNESWSpriteFloor;
            currentWallSprite = game.wallNESWSpriteWall;
        } else if (type == WallSegment.Type.DIAGONAL_NW) {
            currentWallFloorSprite = game.wallNWSESpriteFloor;
            currentWallSprite = game.wallNWSESpriteWall;
        } else if (type == WallSegment.Type.DIAGONAL_NW_short) {
            currentWallFloorSprite = game.wallNWSEShortSpriteFloor;
            currentWallSprite = game.wallNWSEShortSpriteWall;
        } else if (type == WallSegment.Type.CORNER_NORTH) {
            currentWallFloorSprite = game.wallCornerNorthSpriteFloor;
            currentWallSprite = game.wallCornerNorthSpriteWall;
        } else if (type == WallSegment.Type.CORNER_SOUTH) {
            currentWallFloorSprite = game.wallCornerSouthSpriteFloor;
            currentWallSprite = game.wallCornerSouthSpriteWall;
        } else {
            return;
        }
        
        // Save original composite for transparency
        Composite originalComposite = g2d.getComposite();
        
        // Draw floor sprite with semi-transparency (preview effect)
        if (currentWallFloorSprite != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.drawImage(currentWallFloorSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
        }
        
        // Draw wall sprite with semi-transparency (preview effect)
        if (currentWallSprite != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
        }
        
        // Restore original composite
        g2d.setComposite(originalComposite);
        
        // Draw colored border to indicate it's a preview
        g2d.setColor(new Color(0, 255, 0, 128)); // Semi-transparent green
        game.setConstantThicknessStroke(g2d, 3.0f);
        g2d.drawRect(wallX, wallY, wallDisplayWidth, wallDisplayHeight);
        g2d.setStroke(new BasicStroke(1.0f));
    }
}

class FloorTile implements Renderable {
    public enum FloorType {
        REGULAR,
        WALL_NE_SW,
        WALL_NW_SE, 
        WALL_NW_SE_SHORT,
        WALL_CORNER_NORTH,
        WALL_CORNER_SOUTH
    }
    
    int x, y;
    FloorType floorType;
    
    public FloorTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.floorType = FloorType.REGULAR;
    }
    
    public FloorTile(int x, int y, FloorType floorType) {
        this.x = x;
        this.y = y;
        this.floorType = floorType;
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
    public int getRenderPriority() { return 10; }
    
    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        if (game.showSprites) {
            Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);
            
            int floorDisplayWidth = CodeBlue.TILE_WIDTH;
            int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
            
            int floorX = isoPos.x - floorDisplayWidth / 2;
            int floorY = isoPos.y - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
            
            // Select sprite based on floor type
            Image currentFloorSprite = null;
            
            switch (floorType) {
                case REGULAR:
                    currentFloorSprite = game.floorSprite;
                    break;
                case WALL_NE_SW:
                    currentFloorSprite = game.wallNESWSpriteFloor;
                    break;
                case WALL_NW_SE:
                    currentFloorSprite = game.wallNWSESpriteFloor;
                    break;
                case WALL_NW_SE_SHORT:
                    currentFloorSprite = game.wallNWSEShortSpriteFloor;
                    break;
                case WALL_CORNER_NORTH:
                    currentFloorSprite = game.wallCornerNorthSpriteFloor;
                    break;
                case WALL_CORNER_SOUTH:
                    currentFloorSprite = game.wallCornerSouthSpriteFloor;
                    break;
            }
            
            if (currentFloorSprite != null) {
                g2d.drawImage(currentFloorSprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
            }
            
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