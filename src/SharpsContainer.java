import java.awt.*;

public class SharpsContainer implements Renderable, Interactable {

    private int x;
    private int y;
    private int disposedOfCounter = 0;

    public SharpsContainer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void disposeOfMedicine(Medicine medicine, CodeBlue game) {
        medicine.getHolder().setCurrentInteraction(null);
        medicine.setHolder(null);
        game.medicines.remove(medicine);
        this.disposedOfCounter++;
    }

    @Override
    public void onInteractionStart(Player medicine, CodeBlue game) {
        System.out.println("interacting with Sharps Container");
    }

    @Override
    public void onInteractionUpdate(Player player, double deltaTime) {

    }

    @Override
    public void onInteractionEnd(Player player) {

    }

    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    @Override
    public String getInteractionPrompt() {
        return "";
    }

    @Override
    public boolean canUse(Player player, CodeBlue game) {
        return (player.getCurrentInteraction() instanceof Medicine);
    }

    @Override
    public void onUse(Player player, CodeBlue game) {
        System.out.println("interacting with Sharps Container");
    }

    public int getY() {return y;}
    public void setY(int y) {this.y = y;}
    public int getX() {return x;}
    public void setX(int x) {this.x = x;}

    @Override
    public int getRenderX() { return (int)Math.round(x); }

    @Override
    public int getRenderY() { return (int)Math.round(y); }

    @Override
    public int getRenderPriority() {
        return 2;
    }

    @Override
    public int getDepthX() {
        return (int)Math.round(x);
    }

    @Override
    public int getDepthY() {
        return (int)Math.round(y);
    }

    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        Point isoPos = game.gridToIso(this.getX(), this.getY(), offsetX, offsetY);
        int imgDisplayWidth = game.TILE_WIDTH;
        int imgDisplayHeight = (int)(imgDisplayWidth * (501.0 / 320.0));
        int x = isoPos.x - imgDisplayWidth / 2;
        int y = isoPos.y - imgDisplayHeight + game.TILE_HEIGHT / 2;
        g2d.drawImage(Sprites.sharpsContainer, x, y, imgDisplayWidth, imgDisplayHeight, null);
    }
}
