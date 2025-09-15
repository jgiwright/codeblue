import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple 3D Java Game using pure Java 3D math and rendering
 * Features a player that can move around and collect cubes in a 3D environment
 */
public class Simple3DGame extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Camera camera;
    private List<Cube> cubes;
    private Player player;
    private boolean[] keys = new boolean[256];
    private int score = 0;
    
    // Screen dimensions
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;
    
    public Simple3DGame() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        initializeGame();
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }
    
    private void initializeGame() {
        camera = new Camera(0, 2, 5);
        player = new Player(0, 0, 0);
        cubes = new ArrayList<>();
        
        // Create some cubes to collect
        cubes.add(new Cube(3, 0, -3, Color.RED));
        cubes.add(new Cube(-3, 0, -3, Color.GREEN));
        cubes.add(new Cube(0, 0, -6, Color.BLUE));
        cubes.add(new Cube(5, 1, -8, Color.YELLOW));
        cubes.add(new Cube(-5, 1, -8, Color.MAGENTA));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Clear screen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        // Draw ground grid
        drawGround(g2d);
        
        // Draw all cubes
        for (Cube cube : cubes) {
            if (!cube.collected) {
                drawCube(g2d, cube);
            }
        }
        
        // Draw player
        drawPlayer(g2d);
        
        // Draw UI
        drawUI(g2d);
    }
    
    private void drawGround(Graphics2D g2d) {
        g2d.setColor(new Color(50, 50, 50));
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                Point3D p1 = project(new Point3D(x, 0, z));
                Point3D p2 = project(new Point3D(x + 1, 0, z));
                Point3D p3 = project(new Point3D(x, 0, z + 1));
                
                if (p1 != null && p2 != null) {
                    g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                }
                if (p1 != null && p3 != null) {
                    g2d.drawLine((int)p1.x, (int)p1.y, (int)p3.x, (int)p3.y);
                }
            }
        }
    }
    
    private void drawCube(Graphics2D g2d, Cube cube) {
        float size = 0.5f;
        Point3D[] vertices = {
            new Point3D(cube.x - size, cube.y - size, cube.z - size),
            new Point3D(cube.x + size, cube.y - size, cube.z - size),
            new Point3D(cube.x + size, cube.y + size, cube.z - size),
            new Point3D(cube.x - size, cube.y + size, cube.z - size),
            new Point3D(cube.x - size, cube.y - size, cube.z + size),
            new Point3D(cube.x + size, cube.y - size, cube.z + size),
            new Point3D(cube.x + size, cube.y + size, cube.z + size),
            new Point3D(cube.x - size, cube.y + size, cube.z + size)
        };
        
        Point3D[] projected = new Point3D[8];
        boolean allVisible = true;
        
        for (int i = 0; i < 8; i++) {
            projected[i] = project(vertices[i]);
            if (projected[i] == null) allVisible = false;
        }
        
        if (!allVisible) return;
        
        // Draw cube faces
        g2d.setColor(cube.color);
        
        // Front face
        drawQuad(g2d, projected[0], projected[1], projected[2], projected[3]);
        
        // Back face
        g2d.setColor(cube.color.darker());
        drawQuad(g2d, projected[4], projected[5], projected[6], projected[7]);
        
        // Draw edges
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        
        // Front face edges
        drawLine(g2d, projected[0], projected[1]);
        drawLine(g2d, projected[1], projected[2]);
        drawLine(g2d, projected[2], projected[3]);
        drawLine(g2d, projected[3], projected[0]);
        
        // Back face edges
        drawLine(g2d, projected[4], projected[5]);
        drawLine(g2d, projected[5], projected[6]);
        drawLine(g2d, projected[6], projected[7]);
        drawLine(g2d, projected[7], projected[4]);
        
        // Connecting edges
        drawLine(g2d, projected[0], projected[4]);
        drawLine(g2d, projected[1], projected[5]);
        drawLine(g2d, projected[2], projected[6]);
        drawLine(g2d, projected[3], projected[7]);
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawPlayer(Graphics2D g2d) {
        // Draw player as a simple pyramid
        float size = 0.3f;
        Point3D[] vertices = {
            new Point3D(player.x - size, player.y, player.z - size),
            new Point3D(player.x + size, player.y, player.z - size),
            new Point3D(player.x + size, player.y, player.z + size),
            new Point3D(player.x - size, player.y, player.z + size),
            new Point3D(player.x, player.y + size * 2, player.z)
        };
        
        Point3D[] projected = new Point3D[5];
        for (int i = 0; i < 5; i++) {
            projected[i] = project(vertices[i]);
            if (projected[i] == null) return;
        }
        
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(3));
        
        // Draw base
        drawLine(g2d, projected[0], projected[1]);
        drawLine(g2d, projected[1], projected[2]);
        drawLine(g2d, projected[2], projected[3]);
        drawLine(g2d, projected[3], projected[0]);
        
        // Draw pyramid sides
        drawLine(g2d, projected[0], projected[4]);
        drawLine(g2d, projected[1], projected[4]);
        drawLine(g2d, projected[2], projected[4]);
        drawLine(g2d, projected[3], projected[4]);
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawQuad(Graphics2D g2d, Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        int[] xPoints = {(int)p1.x, (int)p2.x, (int)p3.x, (int)p4.x};
        int[] yPoints = {(int)p1.y, (int)p2.y, (int)p3.y, (int)p4.y};
        g2d.fillPolygon(xPoints, yPoints, 4);
    }
    
    private void drawLine(Graphics2D g2d, Point3D p1, Point3D p2) {
        g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
    }
    
    private Point3D project(Point3D point) {
        // Simple perspective projection
        float distance = (float) Math.sqrt(
            (point.x - camera.x) * (point.x - camera.x) +
            (point.y - camera.y) * (point.y - camera.y) +
            (point.z - camera.z) * (point.z - camera.z)
        );
        
        if (point.z - camera.z >= 0) return null; // Behind camera
        
        float fov = 60; // Field of view
        float perspective = (float) (SCREEN_HEIGHT / (2 * Math.tan(Math.toRadians(fov / 2))));
        
        float x = (point.x - camera.x) * perspective / (camera.z - point.z) + SCREEN_WIDTH / 2;
        float y = (point.y - camera.y) * perspective / (camera.z - point.z) + SCREEN_HEIGHT / 2;
        
        return new Point3D(x, y, point.z);
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Movement: WASD, Space to jump", 20, 60);
        g2d.drawString("Camera: Q/E rotate, R/F look up/down, Z/X zoom", 20, 90);
        
        // Display camera info with better formatting
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        float yawDegrees = camera.rotationY * 180.0f / (float)Math.PI;
        float pitchDegrees = camera.rotationX * 180.0f / (float)Math.PI;
        g2d.drawString("Camera Yaw: " + Math.round(yawDegrees) + "°, Pitch: " + Math.round(pitchDegrees) + "°", 20, 120);
        g2d.drawString("Distance: " + Math.round(camera.distance * 10) / 10.0, 20, 140);
        
        if (cubes.stream().allMatch(c -> c.collected)) {
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("YOU WIN!", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }
    
    private void updateGame() {
        // Handle input
        float speed = 0.1f;
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP]) player.z -= speed;
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN]) player.z += speed;
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT]) player.x -= speed;
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) player.x += speed;
        if (keys[KeyEvent.VK_SPACE]) {
            if (player.velocityY == 0) player.velocityY = 0.2f;
        }
        
        // Update player physics
        player.y += player.velocityY;
        player.velocityY -= 0.01f; // Gravity
        
        if (player.y <= 0) {
            player.y = 0;
            player.velocityY = 0;
        }
        
        // Update camera to follow player
        camera.x = player.x;
        camera.z = player.z + 5;
        
        // Check collisions with cubes
        for (Cube cube : cubes) {
            if (!cube.collected) {
                float distance = (float) Math.sqrt(
                    (player.x - cube.x) * (player.x - cube.x) +
                    (player.y - cube.y) * (player.y - cube.y) +
                    (player.z - cube.z) * (player.z - cube.z)
                );
                
                if (distance < 1.0f) {
                    cube.collected = true;
                    score += 10;
                }
            }
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // 3D Point class
    class Point3D {
        float x, y, z;
        
        Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    // Camera class
    class Camera {
        float x, y, z;
        float rotationX = 0; // Pitch (up/down rotation)
        float rotationY = 0; // Yaw (left/right rotation)
        float distance = 5;   // Distance from player
        
        Camera(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        // Update camera position based on rotation and player position
        void updatePosition(Player player) {
            // Calculate camera position relative to player using spherical coordinates
            // Convert spherical to cartesian coordinates
            float offsetX = (float) (distance * Math.sin(rotationY) * Math.cos(rotationX));
            float offsetY = (float) (distance * Math.sin(rotationX));
            float offsetZ = (float) (distance * Math.cos(rotationY) * Math.cos(rotationX));
            
            this.x = player.x + offsetX;
            this.y = player.y + 2 + offsetY; // Base height of 2 units above ground
            this.z = player.z + offsetZ;
        }
    }
    
    // Player class
    class Player {
        float x, y, z;
        float velocityY = 0;
        
        Player(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    // Cube class
    class Cube {
        float x, y, z;
        Color color;
        boolean collected = false;
        
        Cube(float x, float y, float z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple 3D Java Game");
            Simple3DGame game = new Simple3DGame();
            
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