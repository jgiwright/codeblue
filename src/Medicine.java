import java.awt.*;

public abstract class Medicine implements Renderable {
    private int x;
    private int y;
    public Medicine(int x, int y) {
        this.x = x;
        this.y = y;
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
        super(x, y);
    }

    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {
        double isoX = (this.getX() - this.getY()) * CodeBlue.TILE_WIDTH / 2.0 + offsetX;
        double isoY = (this.getX() + this.getY()) * CodeBlue.TILE_HEIGHT / 2.0 + offsetY;
        int drawX = (int)Math.round(isoX - 6/2.0);
        int drawY = (int)Math.round(isoY - 6/2.0);
        int floorDisplayWidth = CodeBlue.TILE_WIDTH;
        int floorDisplayHeight = (int)(floorDisplayWidth * (501.0 / 320.0));
        int floorX = (int)Math.round(isoX - floorDisplayWidth / 2);
        int floorY = (int)Math.round(isoY - floorDisplayHeight + CodeBlue.TILE_HEIGHT / 2);
        Image sprite = game.syringe_adrenaline;
        System.out.println(floorX + " " + floorY);
        g2d.drawImage(sprite, floorX, floorY, floorDisplayWidth, floorDisplayHeight, null);

    }
}