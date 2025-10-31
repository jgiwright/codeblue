import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.*;
import java.awt.*;


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