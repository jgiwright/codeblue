import java.awt.*;

public class Sprites {
    public static Image bedSprite;
    public static Image floorSprite;
    public static Image wallNESWSpriteFloor, wallNESWSpriteWall;
    public static Image wallNWSESpriteFloor, wallNWSESpriteWall;
    public static Image wallNWSEShortSpriteFloor, wallNWSEShortSpriteWall;
    public static Image wallCornerNorthSpriteFloor, wallCornerNorthSpriteWall;
    public static Image wallCornerSouthSpriteFloor, wallCornerSouthSpriteWall;

    public static Image wheelchairNorthSprite;
    public static Image wheelchairEastSprite;
    public static Image wheelchairSouthSprite;
    public static Image wheelchairWestSprite;

    public static Image drawerSWSprite;

    public static Image playerNorthSprite;
    public static Image playerEastSprite;
    public static Image playerSouthSprite;
    public static Image playerWestSprite;
    public static Image[] playerCprSprites;
    public static Image playerNorthWheelchairSprite;
    public static Image playerEastWheelchairSprite;
    public static Image playerSouthWheelchairSprite;
    public static Image playerWestWheelchairSprite;

    public static Image syringe_adrenaline;
    public static Image sharpsContainer;

    private Sprites() {
    }

    public static void loadSprites(Component component) {
        try {
            MediaTracker tracker = new MediaTracker(component);
            int trackerId = 0;

            bedSprite = Toolkit.getDefaultToolkit().getImage("sprites/bed.png");
            tracker.addImage(bedSprite, trackerId++);

            floorSprite = Toolkit.getDefaultToolkit().getImage("sprites/floor.png");
            tracker.addImage(floorSprite, trackerId++);

            wallNESWSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NE-SW_floor.png");
            tracker.addImage(wallNESWSpriteFloor, trackerId++);

            wallNESWSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NE-SW_wall.png");
            tracker.addImage(wallNESWSpriteWall, trackerId++);

            wallNWSESpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_floor.png");
            tracker.addImage(wallNWSESpriteFloor, trackerId++);

            wallNWSESpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_wall.png");
            tracker.addImage(wallNWSESpriteWall, trackerId++);

            wallNWSEShortSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_short_floor.png");
            tracker.addImage(wallNWSEShortSpriteFloor, trackerId++);

            wallNWSEShortSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_short_wall.png");
            tracker.addImage(wallNWSEShortSpriteWall, trackerId++);

            wallCornerNorthSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_north_floor.png");
            tracker.addImage(wallCornerNorthSpriteFloor, trackerId++);

            wallCornerNorthSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_north_wall.png");
            tracker.addImage(wallCornerNorthSpriteWall, trackerId++);

            wallCornerSouthSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_south_floor.png");
            tracker.addImage(wallCornerSouthSpriteFloor, trackerId++);

            wallCornerSouthSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_south_wall.png");
            tracker.addImage(wallCornerSouthSpriteWall, trackerId++);

            wheelchairNorthSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_north.png");
            tracker.addImage(wheelchairNorthSprite, trackerId++);

            wheelchairEastSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_east.png");
            tracker.addImage(wheelchairEastSprite, trackerId++);

            wheelchairSouthSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_south.png");
            tracker.addImage(wheelchairSouthSprite, trackerId++);

            wheelchairWestSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_west.png");
            tracker.addImage(wheelchairWestSprite, trackerId++);

            drawerSWSprite = Toolkit.getDefaultToolkit().getImage("sprites/drawerSW.png");
            tracker.addImage(drawerSWSprite, trackerId++);

            playerNorthSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_north.png");
            tracker.addImage(playerNorthSprite, trackerId++);

            playerSouthSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_south.png");
            tracker.addImage(playerSouthSprite, trackerId++);

            playerWestSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_west.png");
            tracker.addImage(playerWestSprite, trackerId++);

            playerEastSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_east.png");
            tracker.addImage(playerEastSprite, trackerId++);

            sharpsContainer = Toolkit.getDefaultToolkit().getImage("sprites/sharpsContainer.png");
            tracker.addImage(sharpsContainer, trackerId++);

            playerCprSprites = new Image[]{
                    Toolkit.getDefaultToolkit().getImage("sprites/0001.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0002.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0003.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0004.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0005.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0006.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0007.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0008.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0009.png"),
                    Toolkit.getDefaultToolkit().getImage("sprites/0010.png"),
            };
            for (Image img : playerCprSprites) {
                tracker.addImage(img, trackerId++);
            }

            syringe_adrenaline = Toolkit.getDefaultToolkit().getImage("sprites/syringe_adrenaline.png");
            tracker.addImage(syringe_adrenaline, trackerId++);

            playerNorthWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_north_wheelchair.png");
            tracker.addImage(playerNorthWheelchairSprite, trackerId++);

            playerSouthWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_south_wheelchair.png");
            tracker.addImage(playerSouthWheelchairSprite, trackerId++);

            playerWestWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_west_wheelchair.png");
            tracker.addImage(playerWestWheelchairSprite, trackerId++);

            playerEastWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_east_wheelchair.png");
            tracker.addImage(playerEastWheelchairSprite, trackerId++);

            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}