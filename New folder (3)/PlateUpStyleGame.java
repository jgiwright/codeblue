import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlateUpStyleGame extends JPanel implements KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TILE_SIZE = 40;
    private static final int TILE_HEIGHT = 12; // Height of the "3D" effect
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 15;
    
    // Player position in tile coordinates
    private double playerX = 10;
    private double playerY = 7;
    
    // Camera offset for smooth movement
    private int cameraX = 0;
    private int cameraY = 0;
    
    // Simple tile map (0 = grass, 1 = stone, 2 = water, 3 = dirt)
    private int[][] tileMap;
    
    // Colors for different tile types
    private Color[] tileColors = {
        new Color(34, 139, 34),   // Grass - green
        new Color(105, 105, 105), // Stone - gray  
        new Color(30, 144, 255),  // Water - blue
        new Color(139, 69, 19)    // Dirt - brown
    };
    
    public PlateUpStyleGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(new Color(20, 20, 20));
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize tile map with some variety
        generateTileMap();
    }
    
    private void generateTileMap() {
        tileMap = new int[MAP_HEIGHT][MAP_WIDTH];
        
        // Create a kitchen-like layout
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                // Create walls around the edges
                if (x == 0 || x == MAP_WIDTH - 1 || y == 0 || y == MAP_HEIGHT - 1) {
                    tileMap[y][x] = 1; // Stone walls
                }
                // Create some interior walls and obstacles
                else if ((x == 5 || x == 15) && y > 2 && y < MAP_HEIGHT - 3) {
                    tileMap[y][x] = 1; // Stone pillars
                }
                // Water areas (like sinks)
                else if ((x >= 2 && x <= 3 && y >= 2 && y <= 3) || 
                         (x >= 16 && x <= 17 && y >= 11 && y <= 12)) {
                    tileMap[y][x] = 2; // Water
                }
                // Dirt areas (like prep stations)
                else if ((x >= 7 && x <= 9 && y >= 6 && y <= 8) ||
                         (x >= 11 && x <= 13 && y >= 6 && y <= 8)) {
                    tileMap[y][x] = 3; // Dirt
                }
                else {
                    tileMap[y][x] = 0; // Grass (floor)
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
        
        // Draw tiles from back to front for proper depth
        drawTileMap(g2d);
        
        // Draw player
        drawPlayer(g2d);
        
        // Draw UI
        drawUI(g2d);
    }
    
    private void updateCamera() {
        // Convert player position to screen coordinates
        int playerScreenX = (int)(playerX * TILE_SIZE);
        int playerScreenY = (int)(playerY * TILE_SIZE);
        
        // Center camera on player
        cameraX = WINDOW_WIDTH / 2 - playerScreenX;
        cameraY = WINDOW_HEIGHT / 2 - playerScreenY;
    }
    
    private void drawTileMap(Graphics2D g2d) {
        // Draw tiles from back to front (top to bottom) for proper depth sorting
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                drawTile(g2d, x, y, tileMap[y][x]);
            }
        }
    }
    
    private void drawTile(Graphics2D g2d, int tileX, int tileY, int tileType) {
        int screenX = tileX * TILE_SIZE + cameraX;
        int screenY = tileY * TILE_SIZE + cameraY;
        
        // Skip tiles that are off-screen
        if (screenX < -TILE_SIZE || screenX > WINDOW_WIDTH + TILE_SIZE ||
            screenY < -TILE_SIZE || screenY > WINDOW_HEIGHT + TILE_SIZE + TILE_HEIGHT) {
            return;
        }
        
        Color baseColor = tileColors[tileType];
        Color topColor = baseColor.brighter();
        Color sideColor = baseColor.darker();
        
        // Draw the "3D" tile with PlateUp/Overcooked style perspective
        
        // Draw the front face (darker)
        g2d.setColor(sideColor);
        g2d.fillRect(screenX, screenY + TILE_HEIGHT, TILE_SIZE, TILE_SIZE - TILE_HEIGHT);
        
        // Draw the top face (brighter)
        g2d.setColor(topColor);
        int[] topX = {screenX, screenX + TILE_SIZE, screenX + TILE_SIZE, screenX};
        int[] topY = {screenY, screenY, screenY + TILE_HEIGHT, screenY + TILE_HEIGHT};
        g2d.fillPolygon(topX, topY, 4);
        
        // Draw outlines for definition
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        
        // Outline the front face
        g2d.drawRect(screenX, screenY + TILE_HEIGHT, TILE_SIZE, TILE_SIZE - TILE_HEIGHT);
        
        // Outline the top face
        g2d.drawPolygon(topX, topY, 4);
        
        // Draw the edge between top and front
        g2d.drawLine(screenX, screenY + TILE_HEIGHT, screenX + TILE_SIZE, screenY + TILE_HEIGHT);
        
        // Add extra height for walls/obstacles
        if (tileType == 1) { // Stone walls
            drawWall(g2d, screenX, screenY, baseColor);
        }
        
        // Add special effects for water
        if (tileType == 2) {
            drawWaterEffect(g2d, screenX, screenY);
        }
    }
    
    private void drawWall(Graphics2D g2d, int screenX, int screenY, Color baseColor) {
        int wallHeight = TILE_SIZE / 2;
        
        // Draw wall front
        g2d.setColor(baseColor.darker());
        g2d.fillRect(screenX, screenY + TILE_HEIGHT - wallHeight, TILE_SIZE, wallHeight);
        
        // Draw wall top
        g2d.setColor(baseColor);
        g2d.fillRect(screenX, screenY - wallHeight, TILE_SIZE, TILE_HEIGHT + wallHeight);
        
        // Wall outlines
        g2d.setColor(Color.BLACK);
        g2d.drawRect(screenX, screenY + TILE_HEIGHT - wallHeight, TILE_SIZE, wallHeight);
        g2d.drawRect(screenX, screenY - wallHeight, TILE_SIZE, TILE_HEIGHT + wallHeight);
    }
    
    private void drawWaterEffect(Graphics2D g2d, int screenX, int screenY) {
        // Add a subtle water animation effect
        g2d.setColor(new Color(100, 200, 255, 100));
        long time = System.currentTimeMillis();
        int wave = (int)(Math.sin(time * 0.005 + screenX * 0.1) * 3);
        g2d.fillOval(screenX + 5, screenY + 5 + wave, TILE_SIZE - 10, TILE_SIZE - 10);
    }
    
    private void drawPlayer(Graphics2D g2d) {
        int playerScreenX = (int)(playerX * TILE_SIZE) + cameraX;
        int playerScreenY = (int)(playerY * TILE_SIZE) + cameraY;
        
        // Draw player shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(playerScreenX + 8, playerScreenY + TILE_SIZE - 8, 16, 8);
        
        // Draw player as a 3D character
        int playerSize = 16;
        int playerHeight = 20;
        
        // Player body (front face)
        g2d.setColor(new Color(200, 100, 100));
        g2d.fillRect(playerScreenX + TILE_SIZE/2 - playerSize/2, 
                     playerScreenY + TILE_SIZE - playerHeight, 
                     playerSize, playerHeight);
        
        // Player body (top)
        g2d.setColor(new Color(255, 150, 150));
        g2d.fillRect(playerScreenX + TILE_SIZE/2 - playerSize/2, 
                     playerScreenY + TILE_SIZE - playerHeight - 4, 
                     playerSize, 4);
        
        // Player outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(playerScreenX + TILE_SIZE/2 - playerSize/2, 
                     playerScreenY + TILE_SIZE - playerHeight, 
                     playerSize, playerHeight);
        g2d.drawRect(playerScreenX + TILE_SIZE/2 - playerSize/2, 
                     playerScreenY + TILE_SIZE - playerHeight - 4, 
                     playerSize, 4);
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Position: (" + String.format("%.1f", playerX) + ", " + 
                      String.format("%.1f", playerY) + ")", 10, 20);
        g2d.drawString("Use WASD or Arrow Keys to move", 10, 40);
        g2d.drawString("PlateUp!/Overcooked Style View", 10, 60);
        g2d.drawString("Green=Floor, Gray=Walls, Blue=Water, Brown=Prep Stations", 10, 580);
    }
    
    private void movePlayer(double dx, double dy) {
        double newX = playerX + dx;
        double newY = playerY + dy;
        
        // Keep player within map bounds and check for collisions
        if (newX >= 0.5 && newX < MAP_WIDTH - 0.5 && 
            newY >= 0.5 && newY < MAP_HEIGHT - 0.5) {
            
            // Check if the new position is not a wall
            int tileX = (int)newX;
            int tileY = (int)newY;
            
            if (tileMap[tileY][tileX] != 1) { // Not a wall
                playerX = newX;
                playerY = newY;
                repaint();
            }
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        double moveSpeed = 0.15;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                movePlayer(0, -moveSpeed);
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                movePlayer(0, moveSpeed);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                movePlayer(-moveSpeed, 0);
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                movePlayer(moveSpeed, 0);
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PlateUp Style Tile Game");
            PlateUpStyleGame game = new PlateUpStyleGame();
            
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