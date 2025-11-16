import java.awt.*;

public class Bed implements Renderable {
    int x, y;

    public Bed(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean occupiesTile(int tileX, int tileY) {
        return tileX >= x && tileX < x + 2 &&
                tileY >= y && tileY < y + 1;
    }

    @Override
    public int getRenderX() { return x; }

    @Override
    public int getRenderY() { return y; }

    @Override
    public int getDepthX() { return x + 1; } // Use bottom-right corner for depth (2x4 bed)

    @Override
    public int getDepthY() { return y + 3; } // Use bottom-right corner for depth

    @Override
    public int getRenderPriority() { return 1; } // Beds render before players

    @Override
    public void render(Graphics2D g2d, double offsetX, double offsetY, CodeBlue game) {

        if (game.showSprites) {
            Point isoPos = CodeBlue.gridToIso(x, y, offsetX, offsetY);

            // Beds now occupy 1x2 tiles - make them slightly wider than single tile
            int bedDisplayWidth = (int)(CodeBlue.TILE_WIDTH * 2); // 1.5x wider
            int bedDisplayHeight = (int)(bedDisplayWidth * (320.0 / 501.0));

            int bedX = isoPos.x - bedDisplayWidth / 2;
            int bedY = isoPos.y - bedDisplayHeight + CodeBlue.TILE_HEIGHT / 2;

            Image currentBedSprite = Sprites.bedSprite;

            if (currentBedSprite != null) {
                g2d.drawImage(currentBedSprite, bedX, bedY, bedDisplayWidth, bedDisplayHeight, null);
            }

            // Debug coordinates remain the same
        }
    }


}