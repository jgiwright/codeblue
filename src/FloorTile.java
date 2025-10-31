import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.*;
import java.awt.*;

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