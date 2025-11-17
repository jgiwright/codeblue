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

    public void setPickedUp(boolean isPickedUp) {
        this.pickedUp = isPickedUp;
    }

    @Override
    public boolean canUse(Player player, CodeBlue game) {
        if (player.getCurrentInteraction() != this) return false;

        Interactable nearestInteractable = game.findNearestSecondaryInteractable(player);
        if (nearestInteractable == null) return false;

        double distance;

        // If nearest is a Patient, check if medicine can be administered
        if (nearestInteractable instanceof Patient) {
            Patient patient = (Patient) nearestInteractable;
            distance = Math.sqrt(
                    Math.pow(player.getX() - patient.getX(), 2) +
                            Math.pow(player.getY() - patient.getY(), 2)
            );
            return distance <= 1.5 && canAdminister(patient);
        }

        // If nearest is a SharpsContainer, just check distance
        if (nearestInteractable instanceof SharpsContainer) {
            SharpsContainer container = (SharpsContainer) nearestInteractable;
            distance = Math.sqrt(
                    Math.pow(player.getX() - container.getRenderX(), 2) +
                            Math.pow(player.getY() - container.getRenderY(), 2)
            );
            return distance <= 1.5;
        }

        return false;
    }

    @Override
    public void onUse(Player player, CodeBlue game) {
        Interactable nearestInteractable = game.findNearestSecondaryInteractable(player);
        if (nearestInteractable == null) return;

        if (nearestInteractable instanceof Patient) {
            Patient patient = (Patient) nearestInteractable;
            this.administerTo(patient);
        } else if (nearestInteractable instanceof SharpsContainer) {
            SharpsContainer container = (SharpsContainer) nearestInteractable;
            container.disposeOfMedicine((Medicine)player.getCurrentInteraction(), game);
        }
    }

    @Override
    public boolean canInteract(Player player) {
        return !pickedUp;// && !player.isHoldingMedicine();
    }

    @Override
    public void onInteractionStart(Player player, CodeBlue game) {
        pickedUp = true;
        holder = player;
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

    public Player getHolder() {
        return holder;
    }

    public void setHolder(Player holder) {
        this.holder = holder;
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
        g2d.drawImage(Sprites.syringe_adrenaline, x, y, imgDisplayWidth, imgDisplayHeight, null);
    }
}