import javax.swing.*;
import java.awt.*;



public class Patient implements Renderable {
    private String animal;
    private String name;
    private double x, y;
    private String condition;
    
    private long timeCreated;
    private long timeOfCardiacArrest;
    private double timeUntilCardiacArrest;
    private double timeInCardiacArrestUntilDeath;
    
    private PatientState state;
    
    private static final double DETERIORATION_TIME = 10.0;  // seconds
    private static final double CARDIAC_ARREST_TIME = 10.0; // seconds

    public enum PatientState {
        DETERIORATING,
        CARDIAC_ARREST,
        DEAD,
        TREATED
    }
    
    public Patient(double x, double y, String animal, String name, String condition) {
        this.x = x;
        this.y = y;
        this.animal = animal;
        this.name = name;
        this.condition = condition;
        this.timeCreated = System.nanoTime();
        this.timeUntilCardiacArrest = DETERIORATION_TIME; // In seconds
        this.state = PatientState.DETERIORATING;
        
    }
    
    public void update(double deltaTime) {
        switch (state) {
            case DETERIORATING:
                timeUntilCardiacArrest -= deltaTime;
                if (timeUntilCardiacArrest <= 0) {
                    state = PatientState.CARDIAC_ARREST;
                    timeOfCardiacArrest = System.nanoTime();
                    timeInCardiacArrestUntilDeath = CARDIAC_ARREST_TIME;
                }
                break;
                
            case CARDIAC_ARREST:
                timeInCardiacArrestUntilDeath -= deltaTime;
                if (timeInCardiacArrestUntilDeath <= 0) {
                    state = PatientState.DEAD;
                }
                break;
        
        
            case DEAD:
                break;
               
            case TREATED:
                // No updates needed when treated
                break; 
        }
    }
    
    
    public int getHealthPercentage() {
        switch (state) {
            case DETERIORATING:
                return (int)((timeUntilCardiacArrest / DETERIORATION_TIME) * 100);
            case CARDIAC_ARREST:
                return (int)((timeInCardiacArrestUntilDeath / CARDIAC_ARREST_TIME) * 100);
            case DEAD:
                return 0;
            case TREATED:
                return 100;
            default:
                return 100;
        }
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
    
        if (state == PatientState.CARDIAC_ARREST) {
            // Flash red every 0.5 seconds
            long currentTime = System.currentTimeMillis();
            boolean flashOn = (currentTime / 500) % 2 == 0;  // Toggles every 500ms
            g2d.setColor(flashOn ? Color.RED : Color.DARK_GRAY);
        } else if (state == PatientState.DEAD) {
            g2d.setColor(Color.BLACK);
        } else {
            g2d.setColor(Color.GREEN);
        }
        
        g2d.fillOval(drawX, drawY, playerSize, playerSize);
    }
    
    public long getTimeCreated() {
        return this.timeCreated;
    }
    
    public void setTimeOfCardiacArrest(long time) {
        this.timeOfCardiacArrest = time;
    }
    


    public PatientState getState() {
        return state;
    }
    
    public void setState(PatientState newState) {
        if (this.state != newState) {
            this.state = newState;
        }
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