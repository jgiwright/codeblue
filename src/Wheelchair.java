import javax.swing.*;
import java.awt.*;

public class Wheelchair implements Renderable, Interactable {
    private int x, y;
    private int direction; // 0=North, 1=East, 2=South, 3=West
    private Patient passenger;
    private Player pusher;

    public Wheelchair(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = 0; // Default facing north
        this.passenger = null;
        this.pusher = null;
    }

    @Override
    public boolean canInteract(Player player) {
        return pusher == null;
    }
    @Override
    public void onInteractionStart(Player player) {
        this.pusher = player;
        System.out.println(player.label + " is pushing wheelchair");
    }
    @Override
    public void onInteractionUpdate(Player player, double deltaTime) {
        this.direction = player.getDirection();
        this.x = (int)Math.round(player.getX());
        this.y = (int)Math.round(player.getY());

        if (passenger != null) {
            passenger.setX(this.x);
            passenger.setY(this.y);
        }

    }
    @Override
    public void onInteractionEnd(Player player) {
        this.pusher = null;
        System.out.println(player.label + "stopped pushing");
    }
    @Override
    public String getInteractionPrompt() {
        return "Press to push wheelchair";
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
    public int getDirection() { return this.direction; }
    public void setDirection(int direction) { this.direction = direction; }
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public void setX(int x) {
        this.x = x;
        if (passenger != null) {
            passenger.setX(x);
        }
    }
    public void setY(int y) {
        this.y = y;
        if (passenger != null) {
            passenger.setY(y);
        }
    }

    public void setPassenger(Patient patient) {
        this.passenger = patient;
        if (patient != null) {
            patient.setX(this.x);
            patient.setY(this.y);
        }
    }

    public Patient getPassenger() {
        return this.passenger;
    }

    public boolean hasPassenger() {
        return this.passenger != null;
    }

    public void removePassenger() {
        this.passenger = null;
    }
    
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