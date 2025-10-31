import javax.swing.*;
import java.awt.*;

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