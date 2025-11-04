import java.awt.*;

public abstract class Medicine implements Renderable, Interactable {
    private String name;
    private String treatsCondition;
    private int x;
    private int y;
    private Player holder;
    private boolean pickedUp;
    private boolean used;

    public Medicine(String name, String treatsCondition, int x, int y) {
        this.name = name;
        this.treatsCondition = treatsCondition;
        this.x = x;
        this.y = y;
        this.holder = null;
        this.pickedUp = false;
        this.used = false;
    }

    @Override
    public boolean canInteract(Player player) {
        return !pickedUp;// && !player.isHoldingMedicine();
    }

    @Override
    public void onInteractionStart(Player player) {
        pickedUp = true;
      //  player.pickUpMedicine(this);
        System.out.println(player.label + " picked up " + name);
    }

    @Override
    public void onInteractionUpdate(Player player, double deltaTime) {
        if (pickedUp) {
            this.x = (int) Math.round(player.getX());
            this.y = (int) Math.round(player.getY());
        }
    }

    @Override
    public void onInteractionEnd(Player player) {
        this.holder = null;
        pickedUp = false;
        System.out.println(player.label + "stopped holding");
    }

    @Override
    public String getInteractionPrompt() {
        return "Press to hold medicine";
    }

    public boolean canAdminister(Patient patient) {
        if (patient == null || patient.getCondition() == null || this.used) return false;

        String[] required = patient.getCondition().getTreatmentsRequired();
        for (String treatment : required) {
            if (treatment.equals(this.name)) {
                return true;
            }
        }
        return false;
    }

    public void administerTo(Patient patient) {
        if (canAdminister(patient)) {
            patient.receiveTreatment(this.name);
            this.used = true;
            System.out.println("Administered " + name + "to patient");
        }
    }

    public String getName() { return name; }
    public boolean isPickedUp() { return pickedUp; }

    public int getY() {return y;}
    public void setY(int y) {this.y = y;}
    public int getX() {return x;}
    public void setX(int x) {this.x = x;}
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
}

class Adrenaline extends Medicine {
    public Adrenaline(int x, int y) {
        super("adrenaline", "anaphylaxis", x, y);
    }

    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        Point isoPos = CodeBlue.gridToIso(this.getX(), this.getY(), offsetX, offsetY);
        int imgDisplayWidth = CodeBlue.TILE_WIDTH;
        int imgDisplayHeight = (int)(imgDisplayWidth * (501.0 / 320.0)); // Adjust ratio for your sprite
        int x = isoPos.x - imgDisplayWidth / 2;
        int y = isoPos.y - imgDisplayHeight + CodeBlue.TILE_HEIGHT / 2;
        g2d.drawImage(game.syringe_adrenaline, x, y, imgDisplayWidth, imgDisplayHeight, null);
    }
}