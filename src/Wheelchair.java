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
public boolean canUse(Player player, CodeBlue game) {
        return player.getCurrentInteraction() == this;
}

    @Override
    public void onUse(Player player, CodeBlue game) {
        // Check if player is next to wheelchair
        double distance = Math.sqrt(
                Math.pow(player.getX() - this.x, 2) +
                        Math.pow(player.getY() - this.y, 2)
        );

        if (distance <= 1.5) { // Adjacent
            if (this.hasPassenger()) {
                // Unload patient
                Patient patient = this.getPassenger();
                this.removePassenger();
                System.out.println("Patient unloaded from wheelchair");
            } else {
                // Find nearby patient to load
                Patient nearbyPatient = game.findNearestPatient(player);
                if (nearbyPatient != null) {
                    double patientDistance = Math.sqrt(
                            Math.pow(this.x - nearbyPatient.getX(), 2) +
                                    Math.pow(this.y - nearbyPatient.getY(), 2)
                    );

                    if (patientDistance <= 1.5) {
                        this.setPassenger(nearbyPatient);
                        System.out.println("Patient loaded into wheelchair");
                    }
                }
            }
        }
    }

    @Override
    public boolean canInteract(Player player) {
        return pusher == null;
    }
    @Override
    public void onInteractionStart(Player player, CodeBlue game) {
        this.pusher = player;
        System.out.println(player.label + " is pushing wheelchair");
    }
    @Override
    public void onInteractionUpdate(Player player, double deltaTime) {
        this.direction = player.getDirection();

        // Calculate wheelchair position based on player direction
        // Wheelchair should be 1 tile ahead of player in the direction they're facing
        switch (player.getDirection()) {
            case 0: // North (moving up, Y decreases)
                this.x = (int)Math.round(player.getX());
                this.y = (int)Math.round(player.getY() - 1);
                break;
            case 1: // East (moving right, X increases)
                this.x = (int)Math.round(player.getX() + 1);
                this.y = (int)Math.round(player.getY());
                break;
            case 2: // South (moving down, Y increases)
                this.x = (int)Math.round(player.getX());
                this.y = (int)Math.round(player.getY() + 1);
                break;
            case 3: // West (moving left, X decreases)
                this.x = (int)Math.round(player.getX() - 1);
                this.y = (int)Math.round(player.getY());
                break;
        }

        // Update passenger position to match wheelchair
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
            patient.setInWheelchair(true);
            patient.setCurrentWheelchair(this);
        }
    }

    public Patient getPassenger() {
        return this.passenger;
    }

    public boolean hasPassenger() {
        return this.passenger != null;
    }

    public void removePassenger() {
        if (this.passenger != null) {
            this.passenger.setInWheelchair(false);
            this.passenger.setCurrentWheelchair(null);
        }
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
       // System.out.println("wheelchair " + isoPos.x + " " + isoPos.y);
      // System.out.println("x vs render x " + x + " " + y + " " + getRenderX() + " " + getRenderY());

        // Select sprite based on direction
        Image sprite;
        if (passenger != null) {
            switch (direction) {
                case 0: sprite = Sprites.playerNorthWheelchairSprite; break;
                case 1: sprite = Sprites.playerEastWheelchairSprite; break;
                case 2: sprite = Sprites.playerSouthWheelchairSprite; break;
                case 3: sprite = Sprites.playerWestWheelchairSprite; break;
                default: sprite = Sprites.playerNorthWheelchairSprite; break;
            }
        } else {
            switch (direction) {
                case 0:
                    sprite = Sprites.wheelchairNorthSprite;
                    break;
                case 1:
                    sprite = Sprites.wheelchairEastSprite;
                    break;
                case 2:
                    sprite = Sprites.wheelchairSouthSprite;
                    break;
                case 3:
                    sprite = Sprites.wheelchairWestSprite;
                    break;
                default:
                    sprite = Sprites.wheelchairNorthSprite;
                    break;
            }
        }
        
        g2d.drawImage(sprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);
    }
}
}