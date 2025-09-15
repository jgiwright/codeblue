import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Path2D;

public class IsometricTileGame extends JPanel implements KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TILE_WIDTH = 64;
    private static final int TILE_HEIGHT = 32;
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 20;
    
    // Player position in tile coordinates
    private double playerX = 10;
    private double playerY = 10;
    
    // Camera offset for smooth movement
    private int cameraX = 0;
    private int cameraY = 0;
    
    // Simple tile map (0 = grass, 1 = stone, 2 = water)
    private int[][] tileMap;
    
    // Colors for different tile types
    private Color[] tileColors = {
        new Color(34, 139, 34),   // Grass - green
        new Color(105, 105, 105), // Stone - gray  
        new Color(30, 144, 255),  // Water - blue
        new Color(139, 69, 19)    // Dirt - brown
    };
    
    public IsometricTileGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize tile map with some variety
        generateTileMap();
    }
    
    private void generateTileMap() {
        tileMap = new int[MAP_HEIGHT][MAP_WIDTH];
        
        // Create a simple pattern
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                // Create some variety in the terrain
                if ((x + y) % 4 == 0) {
                    tileMap[y][x] = 1; // Stone
                } else if ((x * y) % 7 == 0) {
                    tileMap[y][x] = 2; // Water
                } else if ((x - y) % 6 == 0) {
                    tileMap[y][x] = 3; // Dirt
                } else {
                    tileMap[y][x] = 0; // Grass
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Update camera to follow player
        updateCamera();
        
        // Draw tiles
        drawTileMap(g2d);
        
        // Draw player
        drawPlayer(g2d);
        
        // Draw UI
        drawUI(g2d);
    }
    
    private void updateCamera() {
        // Convert player position to screen coordinates
        Point playerScreen = tileToScreen((int)playerX, (int)playerY);
        
        // Center camera on player
        cameraX = WINDOW_WIDTH / 2 - playerScreen.x;
        cameraY = WINDOW_HEIGHT / 2 - playerScreen.y;
    }
    
    private void drawTileMap(Graphics2D g2d) {
        // Draw tiles from back to front for proper depth sorting
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                drawTile(g2d, x, y, tileMap[y][x]);
            }
        }
    }
    
    private void drawTile(Graphics2D g2d, int tileX, int tileY, int tileType) {
        Point screenPos = tileToScreen(tileX, tileY);
        screenPos.x += cameraX;
        screenPos.y += cameraY;
        
        // Skip tiles that are off-screen
        if (screenPos.x < -TILE_WIDTH || screenPos.x > WINDOW_WIDTH + TILE_WIDTH ||
            screenPos.y < -TILE_HEIGHT || screenPos.y > WINDOW_HEIGHT + TILE_HEIGHT) {
            return;
        }
        
        // Create diamond-shaped tile (isometric view)
        Path2D.Double diamond = new Path2D.Double();
        diamond.moveTo(screenPos.x, screenPos.y - TILE_HEIGHT / 2); // Top
        diamond.lineTo(screenPos.x + TILE_WIDTH / 2, screenPos.y);  // Right
        diamond.lineTo(screenPos.x, screenPos.y + TILE_HEIGHT / 2); // Bottom
        diamond.lineTo(screenPos.x - TILE_WIDTH / 2, screenPos.y);  // Left
        diamond.closePath();
        
        // Fill tile with appropriate color
        g2d.setColor(tileColors[tileType]);
        g2d.fill(diamond);
        
        // Draw tile border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(diamond);
        
        // Add some height effect for stone tiles
        if (tileType == 1) {
            g2d.setColor(tileColors[tileType].darker());
            Path2D.Double topFace = new Path2D.Double();
            topFace.moveTo(screenPos.x, screenPos.y - TILE_HEIGHT / 2 - 8);
            topFace.lineTo(screenPos.x + TILE_WIDTH / 2, screenPos.y - 8);
            topFace.lineTo(screenPos.x, screenPos.y + TILE_HEIGHT / 2 - 8);
            topFace.lineTo(screenPos.x - TILE_WIDTH / 2, screenPos.y - 8);
            topFace.closePath();
            g2d.fill(topFace);
            g2d.setColor(Color.BLACK);
            g2d.draw(topFace);
        }
    }
    
    private void drawPlayer(Graphics2D g2d) {
        Point playerScreen = tileToScreenExact(playerX, playerY);
        playerScreen.x += cameraX;
        playerScreen.y += cameraY;
        
        // Draw player as a circle
        g2d.setColor(Color.RED);
        int playerSize = 12;
        g2d.fillOval(playerScreen.x - playerSize / 2, 
                     playerScreen.y - playerSize / 2 - 16, // Offset to appear on top of tile
                     playerSize, playerSize);
        
        // Draw player border
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(playerScreen.x - playerSize / 2, 
                     playerScreen.y - playerSize / 2 - 16,
                     playerSize, playerSize);
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Position: (" + String.format("%.1f", playerX) + ", " + 
                      String.format("%.1f", playerY) + ")", 10, 20);
        g2d.drawString("Use WASD or Arrow Keys to move", 10, 40);
        g2d.drawString("Colors: Green=Grass, Gray=Stone, Blue=Water, Brown=Dirt", 10, 60);
    }
    
    private Point tileToScreen(int tileX, int tileY) {
        // Convert tile coordinates to isometric screen coordinates
        int screenX = (tileX - tileY) * TILE_WIDTH / 2;
        int screenY = (tileX + tileY) * TILE_HEIGHT / 2;
        return new Point(screenX, screenY);
    }
    
    private Point tileToScreenExact(double tileX, double tileY) {
        // Convert tile coordinates to isometric screen coordinates (with decimals)
        int screenX = (int)((tileX - tileY) * TILE_WIDTH / 2);
        int screenY = (int)((tileX + tileY) * TILE_HEIGHT / 2);
        return new Point(screenX, screenY);
    }
    
    private void movePlayer(double dx, double dy) {
        double newX = playerX + dx;
        double newY = playerY + dy;
        
        // Keep player within map bounds
        if (newX >= 0 && newX < MAP_WIDTH && newY >= 0 && newY < MAP_HEIGHT) {
            playerX = newX;
            playerY = newY;
            repaint();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        double moveSpeed = 0.2;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                movePlayer(-moveSpeed, 0); // Move up-left in isometric view
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                movePlayer(moveSpeed, 0);  // Move down-right in isometric view
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                movePlayer(0, -moveSpeed); // Move up-right in isometric view
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                movePlayer(0, moveSpeed);  // Move down-left in isometric view
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Isometric Tile Map Game");
            IsometricTileGame game = new IsometricTileGame();
            
            frame.add(game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            game.requestFocusInWindow();
        });
    }
}