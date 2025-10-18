import javax.swing.*;
import java.awt.*;



public class Patient implements Renderable {
    private String animal;
    private String name;
    private double x, y;
    private String condition;
    public Patient(double x, double y, String animal, String name, String condition) {
        this.x = x;
        this.y = y;
        this.animal = animal;
        this.name = name;
        this.condition = condition;
    }
    
    @Override
    public int getRenderX() { return (int)Math.round(x); }
    
    @Override
    public int getRenderY() { return (int)Math.round(y); }
    
    @Override
    public int getDepthX() { 
        return (int)Math.round(x);
    }

    @Override
    public int getDepthY() { 
        return (int)Math.round(y);
    }

    @Override
    public int getRenderPriority() { 
        return 100; 
    }
    
    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
     
        double isoX = (this.x - this.y) * CodeBlue.TILE_WIDTH / 2.0 + offsetX;
        double isoY = (this.x + this.y) * CodeBlue.TILE_HEIGHT / 2.0 + offsetY;
        int playerSize = 6;
        
        int drawX = (int)Math.round(isoX - playerSize/2.0);
        int drawY = (int)Math.round(isoY - playerSize/2.0);
        
        int floorDisplayWidth = CodeBlue.TILE_WIDTH;
        int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
        
        int floorX = (int)Math.round(isoX - floorDisplayWidth / 2);
        int floorY = (int)Math.round(isoY - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2);
    
        g2d.fillOval(drawX, drawY, playerSize, playerSize);
    }
    
    public void move(int direction, double moveDistance) {
        System.out.println(direction);
        if (direction == 104) {
            this.y -= moveDistance;
        }
        if (direction == 100) {
            this.x -= moveDistance;
        }
        if (direction == 102) {
            this.x += moveDistance;
        }
        if (direction == 98) {
            this.y += moveDistance;
        }
    }
}