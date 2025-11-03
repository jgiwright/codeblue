import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


public class Patient implements Renderable {
    private final String animal;
    private String name;
    private double x, y;
    private Condition condition;
    
    private final long timeCreated;
    private long timeOfCardiacArrest;
    private double timeUntilCardiacArrest;
    private double timeInCardiacArrestUntilDeath;
    private double timeSinceLastCPR = 0;
    private static final double CPR_EFFECTIVE_WINDOW = 0.3; // seconds
    private boolean receivingCPR;
    private static final double CARDIAC_ARREST_TIME = 30.0; // seconds

    private String[] treatmentsReceived;
    
    private PatientState state;
    


    public enum PatientState {
        DETERIORATING,
        CARDIAC_ARREST,
        DEAD,
        TREATED
    }
    
    public Patient(double x, double y, String animal, String name, Condition condition) {
        this.x = x;
        this.y = y;
        this.animal = animal;
        this.name = name;
        this.condition = condition;
        this.timeCreated = System.nanoTime();
        this.timeUntilCardiacArrest = condition.getTimeToCardiacArrest(); // In seconds
        this.state = PatientState.DETERIORATING;
        this.receivingCPR = false;
        
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
                timeSinceLastCPR += deltaTime;
                
                // CPR is "active" if pressed within the last second
                boolean cprActive = timeSinceLastCPR < CPR_EFFECTIVE_WINDOW;
                
                // Only countdown if CPR is not active
                if (!cprActive) {
                    timeInCardiacArrestUntilDeath -= deltaTime;
                }
                
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

    public boolean isTreatmentComplete() {
        Arrays.sort(treatmentsReceived);
        Arrays.sort(this.condition.getTreatmentsRequired());
        return Arrays.equals(treatmentsReceived, this.condition.getTreatmentsRequired());
    }
    
    public void resetCPRTimer() {
        timeSinceLastCPR = 0;
    }
    
    
    public int getHealthPercentage() {
        switch (state) {
            case DETERIORATING:
                return (int)((timeUntilCardiacArrest / this.condition.getTimeToCardiacArrest()) * 100);
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
        return (int)Math.round(y) + 2;
    }

    @Override
    public int getRenderPriority() { return 0; }


    
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
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public PatientState getState() {
        return state;
    }
    
    public void setState(PatientState newState) {
        if (this.state != newState) {
            this.state = newState;
        }
    }
    
    public void setReceivingCPR(boolean receiving) {
        this.receivingCPR = receiving;
    }
    
    public boolean isReceivingCPR() {
        return receivingCPR;
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