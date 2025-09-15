import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.MediaTracker;

interface Renderable {
    int getRenderX();
    int getRenderY();
    int getRenderPriority();
    int getDepthX(); // Bottom-right X for depth calculation
    int getDepthY(); // Bottom-right Y for depth calculation
    void render(Graphics2D g2d, int offsetX, int offsetY, CodeBlue game);
}

public class CodeBlue extends JPanel implements KeyListener, MouseListener {
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int TILE_WIDTH = 16;
    private static final int TILE_HEIGHT = 8;
    private static final int MAP_WIDTH = 100;
    private static final int MAP_HEIGHT = 100;
    
    // Player positions (in grid coordinates)
    private Point player1Pos = new Point(5, 5);
    private Point player2Pos = new Point(7, 7);
    
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 50; // milliseconds between moves
    
    // Wall grid - true means there's a wall at that position
   // private boolean[][] walls = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private boolean showGrid = true;
    
    // Input handling
    private Set<Integer> pressedKeys = new HashSet<>();
    private boolean isWallMode = false;
    private boolean isEraseMode = false;
    
    // Camera system
    private Point2D.Double cameraPos = new Point2D.Double(0, 0);
    private static final double CAMERA_LERP_SPEED = 0.15;
    private boolean cameraLerp = true;
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 8.0;
    private static final double ZOOM_STEP = 0.1;
    private Color floorColor = new Color(240, 240, 240);
    private Color wallColor = new Color(139, 69, 19);
    private Color player1Color = new Color(255, 100, 100);
    private Color player2Color = new Color(100, 100, 255);
    private Color gridColor = new Color(200, 200, 200);
    
    private java.util.List<Bed> beds = new ArrayList<>();
    private boolean isBedMode = false;
    public Image bedSprite;
    public Image floorSprite;
public Image wallNESWSprite;  // wall_NE-SW.png
public Image wallNWSESprite;  // wall_NW-SE.png  
public Image wallCornerSprite; // wall_corner.png
private int currentWallType = 0; // 0=NE-SW, 1=NW-SE, 2=corner
private int[][] walls = new int[MAP_WIDTH][MAP_HEIGHT]; // 0=no wall, 1=NE-SW, 2=NW-SE, 3=corner
    
    public boolean showSprites = true;
    

    
    public CodeBlue() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        loadSprites();
        
        // Initialize with some sample walls
        initializeSampleWalls();
    }
    
private void initializeSampleWalls() {
    // Create a simple room outline with NE-SW walls
    for (int x = 3; x < 12; x++) {
        walls[x][3] = 1;  // Top wall (NE-SW)
        walls[x][10] = 1; // Bottom wall (NE-SW)
    }
    for (int y = 3; y < 11; y++) {
        walls[3][y] = 2;  // Left wall (NW-SE)
        walls[11][y] = 2; // Right wall (NW-SE)
    }
    // Add corners
    walls[3][3] = 3;   // Top-left corner
    walls[11][3] = 3;  // Top-right corner
    walls[3][10] = 3;  // Bottom-left corner
    walls[11][10] = 3; // Bottom-right corner
    
    // Add a door
    walls[7][3] = 0;
}
    
private void loadSprites() {
    try {
        bedSprite = Toolkit.getDefaultToolkit().getImage("bed.png");
        floorSprite = Toolkit.getDefaultToolkit().getImage("floor_50x501.png");
        wallNESWSprite = Toolkit.getDefaultToolkit().getImage("wall_NE-SW.png");
        wallNWSESprite = Toolkit.getDefaultToolkit().getImage("wall_NW-SE.png");
        wallCornerSprite = Toolkit.getDefaultToolkit().getImage("wall_corner.png");
        
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(bedSprite, 0);
        tracker.addImage(floorSprite, 1);
        tracker.addImage(wallNESWSprite, 2);
        tracker.addImage(wallNWSESprite, 3);
        tracker.addImage(wallCornerSprite, 4);
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
    
    // Draw floor tiles
    drawFloor(g2d, offsetX, offsetY);
    
    // Draw walls
    drawWalls(g2d, offsetX, offsetY);
    
    // Collect all renderable objects
    List<Renderable> renderables = new ArrayList<>();
    renderables.addAll(beds);
    renderables.add(new Player(player1Pos, player1Color, "P1"));
    renderables.add(new Player(player2Pos, player2Color, "P2"));

 
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
    
    // Reset transformation for UI
    g2d.scale(1.0/zoomLevel, 1.0/zoomLevel);
    
    // Draw UI
    drawUI(g2d);
}
  
    
private void drawFloor(Graphics2D g2d, int offsetX, int offsetY) {
    for (int x = 0; x < MAP_WIDTH; x++) {
        for (int y = 0; y < MAP_HEIGHT; y++) {
            Point isoPos = gridToIso(x, y, offsetX, offsetY);
            
            // Draw floor sprite first
            if (showSprites) {
                int floorX = isoPos.x - 25; // Center 50x50 sprite
                int floorY = isoPos.y - 12;
                g2d.drawImage(floorSprite, floorX, floorY, null);
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
                drawIsometricTile(g2d, isoPos.x, isoPos.y, tileColor, false);
            } else if (showGrid && !showSprites) {
                drawIsometricTile(g2d, isoPos.x, isoPos.y, floorColor, true);
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
    
private void drawWalls(Graphics2D g2d, int offsetX, int offsetY) {
    for (int x = 0; x < MAP_WIDTH; x++) {
        for (int y = 0; y < MAP_HEIGHT; y++) {
            if (walls[x][y] > 0) {
                Point isoPos = gridToIso(x, y, offsetX, offsetY);
                
                if (showSprites) {
                    // Draw grid BEFORE wall sprite if grid is enabled
                    if (showGrid) {
                        g2d.setColor(new Color(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 128));
                        setConstantThicknessStroke(g2d, 1.0f);
                        int[] xPoints = {isoPos.x, isoPos.x + TILE_WIDTH/2, isoPos.x, isoPos.x - TILE_WIDTH/2};
                        int[] yPoints = {isoPos.y - TILE_HEIGHT/2, isoPos.y, isoPos.y + TILE_HEIGHT/2, isoPos.y};
                        g2d.drawPolygon(xPoints, yPoints, 4);
                        g2d.setStroke(new BasicStroke(1.0f));
                    }
                    
                    // Wall width should match the tile width
                    int wallDisplayWidth = TILE_WIDTH;
                    int wallDisplayHeight = (int)(wallDisplayWidth * (524.0 / 304.0));
                    
                    int wallX = isoPos.x - wallDisplayWidth / 2;
                    int wallY = isoPos.y - wallDisplayHeight + TILE_HEIGHT / 2;
                    
                    // Select appropriate sprite based on wall type
                    Image currentWallSprite;
                    switch (walls[x][y]) {
                        case 1: currentWallSprite = wallNWSESprite; break; 
                        case 2: currentWallSprite = wallNESWSprite; break;
                        case 3: currentWallSprite = wallCornerSprite; break;
                        default: currentWallSprite = wallNESWSprite; break;
                    }
                    
                    g2d.drawImage(currentWallSprite, wallX, wallY, wallDisplayWidth, wallDisplayHeight, null);
                    
                } else {
                    drawIsometricTile(g2d, isoPos.x, isoPos.y, wallColor, showGrid);
                }
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
    
private void setConstantThicknessStroke(Graphics2D g2d, float pixelWidth) {
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
    
    String[] wallTypes = {"NE-SW", "NW-SE", "Corner"};
    
    String[] instructions = {
        "Player 1 (Red): WASD to move",
        "Player 2 (Blue): Arrow keys to move",
        "Mouse Click: Toggle wall/floor",
        "SPACE: Toggle wall mode " + (isWallMode ? "[ON]" : "[OFF]"),
        "E: Toggle erase mode " + (isEraseMode ? "[ON]" : "[OFF]"),
        "B: Toggle bed mode " + (isBedMode ? "[ON]" : "[OFF]"),
        "H: Toggle sprites " + (showSprites ? "[ON]" : "[OFF]"),
        "1-3: Select wall type - Current: " + wallTypes[currentWallType],
        "+ / =: Zoom in",
        "- : Zoom out",
        "Zoom: " + String.format("%.1f", zoomLevel) + "x",
        "Current mode: " + (isEraseMode ? "ERASE" : (isWallMode ? "PLACE WALLS" : "NORMAL"))
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
        
        // Handle continuous key presses for smooth movement
        boolean moved = false;
        
        // Player 1 movement (WASD)
        Point newPos1 = new Point(player1Pos);
        if (pressedKeys.contains(KeyEvent.VK_W)) newPos1.y--;
        if (pressedKeys.contains(KeyEvent.VK_S)) newPos1.y++;
        if (pressedKeys.contains(KeyEvent.VK_A)) newPos1.x--;
        if (pressedKeys.contains(KeyEvent.VK_D)) newPos1.x++;
        
        if (isValidMove(newPos1) && !newPos1.equals(player2Pos)) {
            player1Pos = newPos1;
            moved = true;
        }
        
        // Player 2 movement (Arrow keys)
        Point newPos2 = new Point(player2Pos);
        if (pressedKeys.contains(KeyEvent.VK_UP)) newPos2.y--;
        if (pressedKeys.contains(KeyEvent.VK_DOWN)) newPos2.y++;
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) newPos2.x--;
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) newPos2.x++;
        
        if (isValidMove(newPos2) && !newPos2.equals(player1Pos)) {
            player2Pos = newPos2;
            moved = true;
        }
        

        
        if (moved) {
            lastMoveTime = currentTime; // Update last move time
            // Update camera to follow the midpoint between players
            updateCamera();
            repaint();
        }
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
    
private boolean isValidMove(Point pos) {
    if (pos.x < 0 || pos.x >= MAP_WIDTH || pos.y < 0 || pos.y >= MAP_HEIGHT) {
        return false;
    }
    if (walls[pos.x][pos.y] > 0) { // Any wall type blocks movement
        return false;
    }
    for (Bed bed : beds) {
        if (bed.occupiesTile(pos.x, pos.y)) {
            return false;
        }
    }
    return true;
}
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        
        // Toggle modes
    // Wall type selection
    if (e.getKeyCode() == KeyEvent.VK_1) {
        currentWallType = 0; // NE-SW
        repaint();
    } else if (e.getKeyCode() == KeyEvent.VK_2) {
        currentWallType = 1; // NW-SE
        repaint();
    } else if (e.getKeyCode() == KeyEvent.VK_3) {
        currentWallType = 2; // Corner
        repaint();
    }
       else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isWallMode = !isWallMode;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            isEraseMode = !isEraseMode;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) {
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
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            isBedMode = !isBedMode;
            isWallMode = false;
            repaint();   
        } else if (e.getKeyCode() == KeyEvent.VK_H) {
            showSprites = !showSprites;
            repaint();
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
    // Adjust mouse coordinates for zoom and camera
    double worldX = (e.getX() / zoomLevel) + cameraPos.x - (WINDOW_WIDTH / 2.0 / zoomLevel);
    double worldY = (e.getY() / zoomLevel) + cameraPos.y - (WINDOW_HEIGHT / 2.0 / zoomLevel);
    
    // Convert world coordinates to grid using improved isometric conversion
    Point gridPos = worldToGrid(worldX, worldY);
    
    // Check if click is within bounds
    if (gridPos.x >= 0 && gridPos.x < MAP_WIDTH && 
        gridPos.y >= 0 && gridPos.y < MAP_HEIGHT) {
        
        if (isBedMode) {
            // Bed placement logic
            if (isEraseMode) {
                // Remove bed if exists at this position
                beds.removeIf(bed -> bed.occupiesTile(gridPos.x, gridPos.y));
            } else {
                // Try to place a bed (4x2) at this position
                boolean canPlace = true;
                
                // Check if 4x2 area is clear
                for (int dx = 0; dx < 4 && canPlace; dx++) {
                    for (int dy = 0; dy < 2 && canPlace; dy++) {
                        int checkX = gridPos.x + dx;
                        int checkY = gridPos.y + dy;
                        
                        // Check bounds
                        if (checkX >= MAP_WIDTH || checkY >= MAP_HEIGHT) {
                            canPlace = false;
                            break;
                        }
                        
                        // Check walls, players, other beds
                        if (walls[checkX][checkY] > 0 || 
                            (checkX == player1Pos.x && checkY == player1Pos.y) ||
                            (checkX == player2Pos.x && checkY == player2Pos.y)) {
                            canPlace = false;
                            break;
                        }
                        
                        // Check other beds
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
        } else {
            // Wall placement logic (existing code)
            // Don't place walls on player positions
            if (gridPos.equals(player1Pos) || gridPos.equals(player2Pos)) {
                return;
            }
            
            // Don't place walls on beds
            for (Bed bed : beds) {
                if (bed.occupiesTile(gridPos.x, gridPos.y)) {
                    return;
                }
            }
            
            if (isEraseMode) {
                walls[gridPos.x][gridPos.y] = 0; // Remove wall
            } else if (isWallMode) {
                walls[gridPos.x][gridPos.y] = currentWallType + 1; // Place current wall type
            } else {
                // Toggle mode - cycle through wall types
                walls[gridPos.x][gridPos.y] = (walls[gridPos.x][gridPos.y] + 1) % 4;
            }
        }
        
        repaint();
    }
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

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            g2d.drawString(label, isoPos.x - textWidth/2, isoPos.y - 15);
        }
    }
}