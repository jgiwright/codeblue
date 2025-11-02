import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class SaveLoadGame {
    private CodeBlue game;
    public SaveLoadGame(CodeBlue game) {
        this.game = game;
    }
    
    // Save map to file
public void saveMap() {
    File file = new File("map.map");
     
    try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
        // Write map header
        writer.println("# Hospital Map File");
        writer.println("VERSION=1.0");
        writer.println("MAP_WIDTH=" + game.MAP_WIDTH);
        writer.println("MAP_HEIGHT=" + game.MAP_HEIGHT);
        writer.println();
        
        // Write camera and zoom settings
        writer.println("# Camera and Zoom Settings");
        writer.println("CAMERA_X=" + game.cameraPos.x);
        writer.println("CAMERA_Y=" + game.cameraPos.y);
        writer.println("ZOOM_LEVEL=" + game.zoomLevel);
        writer.println("CAMERA_LERP=" + game.cameraLerp);
        writer.println();
        
        // Write player positions (using floating point coordinates)
        writer.println("# Player Positions");
        writer.println("PLAYER1=" + game.player1.x + "," + game.player1.y);
        writer.println("PLAYER2=" + game.player2.x + "," + game.player2.y);
        writer.println();
        
        // Write thin walls
        writer.println("# Thin Walls (x,y,type)");
        for (WallSegment wall : game.walls) {
            writer.println("THIN_WALL=" + wall.gridX + "," + wall.gridY + "," + wall.type.toString());
        }
        writer.println();
        
        // Write floor tiles
        writer.println("# Floor Tiles (x,y,type)");
        for (FloorTile floor : game.placedFloorTiles) {
            writer.println("FLOOR=" + floor.x + "," + floor.y + "," + floor.floorType.toString());
        }
        writer.println();
        
        // Write beds
        writer.println("# Beds (x,y)");
        for (Bed bed : game.beds) {
            writer.println("BED=" + bed.x + "," + bed.y);
        }
        writer.println();

        // Write wheelchairs
        writer.println("# Wheelchairs (x,y,direction)");
        for (Wheelchair chair : game.wheelchairs) {
            writer.println("WHEELCHAIR=" + chair.getX() + "," + chair.getY() + "," + chair.getDirection());
        }
        writer.println();
        
        writer.println("# Drawers (x,y)");
        for (Drawer drawer : game.drawers) {
            writer.println("DRAWER=" + drawer.x + "," + drawer.y);
        }

        JOptionPane.showMessageDialog(game, "Map saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException e) {
        JOptionPane.showMessageDialog(game, "Error saving map: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Load map from file
public void loadMap() {
    File file = new File("map.map");
    
    if (!file.exists()) {
        JOptionPane.showMessageDialog(game, "No map.map file found in current directory", "Load Error", JOptionPane.ERROR_MESSAGE);
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
                game.player1.x = Double.parseDouble(coords[0]);
                game.player1.y = Double.parseDouble(coords[1]);
                // Update grid position and Point object for compatibility
                game.player1GridPos = new Point((int)Math.round(game.player1.x), (int)Math.round(game.player1.y));
                game.player1Pos = new Point((int)Math.round(game.player1.x), (int)Math.round(game.player1.y));
                
            } else if (line.startsWith("PLAYER2=")) {
                String[] coords = line.substring(8).split(",");
                game.player2.x = Double.parseDouble(coords[0]);
                game.player2.y = Double.parseDouble(coords[1]);
                // Update grid position and Point object for compatibility
                game.player2GridPos = new Point((int)Math.round(game.player2.x), (int)Math.round(game.player2.y));
                game.player2Pos = new Point((int)Math.round(game.player2.x), (int)Math.round(game.player2.y));
                
            } else if (line.startsWith("CAMERA_X=")) {
                game.cameraPos.x = Double.parseDouble(line.substring(9));
                
            } else if (line.startsWith("CAMERA_Y=")) {
                game.cameraPos.y = Double.parseDouble(line.substring(9));
                
            } else if (line.startsWith("ZOOM_LEVEL=")) {
                game.zoomLevel = Double.parseDouble(line.substring(11));
                // Clamp zoom to valid range
                game.zoomLevel = Math.max(game.MIN_ZOOM, Math.min(game.MAX_ZOOM, game.zoomLevel));
                
            } else if (line.startsWith("CAMERA_LERP=")) {
                game.cameraLerp = Boolean.parseBoolean(line.substring(12));
                
            } else if (line.startsWith("THIN_WALL=")) {
                String[] parts = line.substring(10).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                WallSegment.Type type = WallSegment.Type.valueOf(parts[2]);
                game.walls.add(new WallSegment(x, y, type));
                
            } else if (line.startsWith("FLOOR=")) {
                String[] parts = line.substring(6).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                FloorTile.FloorType type = parts.length > 2 ? 
                    FloorTile.FloorType.valueOf(parts[2]) : FloorTile.FloorType.REGULAR;
                game.placedFloorTiles.add(new FloorTile(x, y, type));
            }else if (line.startsWith("BED=")) {
                String[] coords = line.substring(4).split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                game.beds.add(new Bed(x, y));
                
            } else if (line.startsWith("WHEELCHAIR=")) {
                String[] parts = line.substring(11).split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int dir = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                Wheelchair chair = new Wheelchair(x, y);
                chair.setDirection(dir);
                game.wheelchairs.add(chair);
            }
else if (line.startsWith("DRAWER=")) {
    String[] coords = line.substring(7).split(",");
    int x = Integer.parseInt(coords[0]);
    int y = Integer.parseInt(coords[1]);
    game.drawers.add(new Drawer(x, y));
}

        }
        
        // Don't call updateCamera() - use the loaded camera position instead
        game.repaint();
        
        JOptionPane.showMessageDialog(game, "Map loaded successfully!", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (IOException e) {
        JOptionPane.showMessageDialog(game, "Error loading map: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(game, "Error parsing map file: " + e.getMessage(), "Parse Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Clear all map data
public void clearMap() {
    // Clear  objects
    game.walls.clear();
    game.placedFloorTiles.clear();
    game.beds.clear();
    game.wheelchairs.clear();
    game.drawers.clear();
    
    // Reset player positions
    game.player1Pos = new Point(5, 5);
    game.player2Pos = new Point(7, 7);
}

// Create new empty map
public void newMap() {
    int result = JOptionPane.showConfirmDialog(game, 
        "Create a new map? This will clear all current data.", 
        "New Map", 
        JOptionPane.YES_NO_OPTION);
    
    if (result == JOptionPane.YES_OPTION) {
        clearMap();
        game.updateCamera();
        game.repaint();
    }
}    

}