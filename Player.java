import javax.swing.*;
import java.awt.*;

class Player implements Renderable {
    Color color;
    String label;
    double x, y;
    
    private PlayerState state = PlayerState.IDLE;
    private double animationTimer = 0;
    private int currentFrame = 0;
    private int direction = 0; // 0=North, 1=East, 2=South, 3=West
    private int cprFrameCount = 0;
    
    private static final double CPR_FRAME_DURATION = 0.025; // seconds per frame
    private double cprDuration = 0; // total duration for CPR action
    private double cprElapsedTime = 0;
    
    enum PlayerState {
        IDLE,
        WALKING,
        PERFORMING_CPR
        // Add more states as needed: PICKING_UP, USING_ITEM, etc.
    }
    
    public Player(double x, double y, Color color, String label) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.label = label;
        this.direction = 0;
    }
    
    public void update(double deltaTime) {
        animationTimer += deltaTime;
        
        switch (state) {
            case PERFORMING_CPR:
            // Calculate frame without looping
            currentFrame = (int)(animationTimer / CPR_FRAME_DURATION);
            
            // Stop at last frame
            if (cprFrameCount > 0 && currentFrame >= cprFrameCount) {
                currentFrame = cprFrameCount - 1; // Hold last frame
                finishCPR(); // Return to IDLE
            }
            break;
                
            case WALKING:
                // Walking animation logic could go here
                // currentFrame = (int)(animationTimer / WALK_FRAME_DURATION) % walkFrameCount;
                break;
                
            case IDLE:
            default:
                currentFrame = 0;
                break;
        }
    }
    
  // Method to start CPR animation
    public void performCPR() {
        performCPR(0); // Infinite duration until manually stopped
    }
    
    // Method to start CPR animation with specified duration
    public void performCPR(int totalFrames) {
        state = PlayerState.PERFORMING_CPR;
        animationTimer = 0;
        currentFrame = 0;
        cprFrameCount = totalFrames;
        cprElapsedTime = 0;
    }
    
    // Method to manually stop CPR
    public void stopCPR() {
        if (state == PlayerState.PERFORMING_CPR) {
            finishCPR();
        }
    }
    
    // Private helper to finish CPR and return to idle
    private void finishCPR() {
        state = PlayerState.IDLE;
        animationTimer = 0;
        currentFrame = 0;
        // Could trigger a callback or event here
    }
    
    // Check if player is currently performing an action
    public boolean isBusy() {
        return state == PlayerState.PERFORMING_CPR;
        // Expand this for other "busy" states
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
        return 100; // Very high priority to always render last
    }

    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        if (game.showSprites) {
            // Snap player position to 0.25 tile increments before calculating screen position
            double snappedX = game.snapToGrid(x, 0.25);
            double snappedY = game.snapToGrid(y, 0.25);

            // Calculate exact floating point position using snapped coordinates
            double isoX = (snappedX - snappedY) * CodeBlue.TILE_WIDTH / 2.0 + offsetX;
            double isoY = (snappedX + snappedY) * CodeBlue.TILE_HEIGHT / 2.0 + offsetY;

            g2d.setColor(color);
            int playerSize = 6;

            // Round only at the final drawing step
            int drawX = (int)Math.round(isoX - playerSize/2.0);
            int drawY = (int)Math.round(isoY - playerSize/2.0);

          //  g2d.fillOval(drawX, drawY, playerSize, playerSize);


            int floorDisplayWidth = CodeBlue.TILE_WIDTH;
            int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));

            int floorX = (int)Math.round(isoX - floorDisplayWidth / 2);
            int floorY = (int)Math.round(isoY - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2);

            Image sprite = selectSprite(game);

            g2d.drawImage(sprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);


        }
    }

   // Helper method to select the appropriate sprite
    private Image selectSprite(CodeBlue game) {
        switch (state) {
            case PERFORMING_CPR:
                // If you have multiple CPR frames
                if (game.playerCprSprites != null && game.playerCprSprites.length > 0) {
                    int frameIndex = currentFrame % game.playerCprSprites.length;
                    return game.playerCprSprites[frameIndex];
                }
                // Fallback if CPR sprites not loaded
                return game.playerNorthSprite;
                
            case WALKING:
                // Select sprite based on direction
                // Assuming you have directional sprites
                switch (this.direction) {
                    case 0: return game.playerNorthSprite;
                    case 1: return game.playerEastSprite;// != null ? game.playerEastSprite : game.playerNorthSprite;
                    case 2: return game.playerSouthSprite;// != null ? game.playerSouthSprite : game.playerNorthSprite;
                    case 3: return game.playerWestSprite;// != null ? game.playerWestSprite : game.playerNorthSprite;
                    default: return game.playerNorthSprite; 
                }
                
            case IDLE:
            default:
                switch (this.direction) {
                    case 0: return game.playerNorthSprite;
                    case 1: return game.playerEastSprite;// != null ? game.playerEastSprite : game.playerNorthSprite;
                    case 2: return game.playerSouthSprite;// != null ? game.playerSouthSprite : game.playerNorthSprite;
                    case 3: return game.playerWestSprite;// != null ? game.playerWestSprite : game.playerNorthSprite;
                    default: return game.playerNorthSprite; 
                }
        }
    }
    
    // Getter methods for state information
    public PlayerState getState() {
        return state;
    }
    
    public int getDirection() {
        return direction;
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
        System.out.println(this.direction);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }

    
    // Setter for manual state control if needed
    public void setState(PlayerState newState) {
        if (this.state != newState) {
            this.state = newState;
            animationTimer = 0;
            currentFrame = 0;
        }
    }

}