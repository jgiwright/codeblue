import java.awt.*;

public abstract class MedicineDispenser implements Renderable, Interactable {
    private int x, y;
    private String name;
    public MedicineDispenser(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }


    @Override
    public void onInteractionUpdate(Player player, double deltaTime) {

    }

    @Override
    public void onInteractionEnd(Player player) {

    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }

    @Override
    public String getInteractionPrompt() {
        return "";
    }

    @Override
    public boolean canUse(Player player, CodeBlue game) {
        return false;
    }

    @Override
    public void onUse(Player player, CodeBlue game) {

    }

    @Override
    public int getRenderX() {
        return this.x;
    }

    @Override
    public int getRenderY() {
        return this.y;
    }

    @Override
    public int getRenderPriority() {
        return 2;
    }

    @Override
    public int getDepthX() {
        return 0;
    }

    @Override
    public int getDepthY() {
        return 0;
    }

}


class AdrenalineDispenser extends MedicineDispenser {
    public AdrenalineDispenser(int x, int y) {
        super(x, y, "adrenaline");
    }

    @Override
    public void onInteractionStart(Player player, CodeBlue game) {
        Adrenaline newMedicine = new Adrenaline(this.getRenderX(), this.getRenderY());
        newMedicine.setHolder(player);
        newMedicine.setPickedUp(true);
        player.setCurrentInteraction(newMedicine);
        game.addMedicine(newMedicine);
    }

    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        Point isoPos = game.gridToIso(this.getRenderX(), this.getRenderY(), offsetX, offsetY);
        int imgDisplayWidth = game.TILE_WIDTH;
        int imgDisplayHeight = (int)(imgDisplayWidth * (501.0 / 320.0));
        int x = isoPos.x - imgDisplayWidth / 2;
        int y = isoPos.y - imgDisplayHeight + game.TILE_HEIGHT / 2;
        g2d.drawImage(Sprites.adrenaline_dispenser, x, y, imgDisplayWidth, imgDisplayHeight, null);
    }
}
