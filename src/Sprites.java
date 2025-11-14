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

    private Sprites() {
    }

    public static void loadSprites(Component component) {


        try {
            bedSprite = Toolkit.getDefaultToolkit().getImage("sprites/bed.png");
            floorSprite = Toolkit.getDefaultToolkit().getImage("sprites/floor.png");

            wallNESWSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NE-SW_floor.png");
            wallNESWSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NE-SW_wall.png");
            wallNWSESpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_floor.png");
            wallNWSESpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_wall.png");
            wallNWSEShortSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_short_floor.png");
            wallNWSEShortSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_NW-SE_short_wall.png");
            wallCornerNorthSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_north_floor.png");
            wallCornerNorthSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_north_wall.png");
            wallCornerSouthSpriteFloor = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_south_floor.png");
            wallCornerSouthSpriteWall = Toolkit.getDefaultToolkit().getImage("sprites/wall_corner_south_wall.png");

            wheelchairNorthSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_north.png");
            wheelchairEastSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_east.png");
            wheelchairSouthSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_south.png");
            wheelchairWestSprite = Toolkit.getDefaultToolkit().getImage("sprites/wheelchair_west.png");

            drawerSWSprite = Toolkit.getDefaultToolkit().getImage("sprites/drawerSW.png");

            playerNorthSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_north.png");
            playerSouthSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_south.png");
            playerWestSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_west.png");
            playerEastSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_east.png");

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
                    // Add as many frames as you have
            };

            syringe_adrenaline = Toolkit.getDefaultToolkit().getImage("sprites/syringe_adrenaline.png");

            playerNorthWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_north_wheelchair.png");
            playerSouthWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_south_wheelchair.png");
            playerWestWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_west_wheelchair.png");
            playerEastWheelchairSprite = Toolkit.getDefaultToolkit().getImage("sprites/player_east_wheelchair.png");

            MediaTracker tracker = new MediaTracker(component);
            tracker.addImage(bedSprite, 0);
            tracker.addImage(floorSprite, 1);

            // Add floor sprites to tracker
            tracker.addImage(wallNESWSpriteFloor, 2);
            tracker.addImage(wallNWSESpriteFloor, 3);
            tracker.addImage(wallNWSEShortSpriteFloor, 4);
            tracker.addImage(wallCornerNorthSpriteFloor, 5);
            tracker.addImage(wallCornerSouthSpriteFloor, 6);

            // Add wall sprites to tracker
            tracker.addImage(wallNESWSpriteWall, 7);
            tracker.addImage(wallNWSESpriteWall, 8);
            tracker.addImage(wallNWSEShortSpriteWall, 9);
            tracker.addImage(wallCornerNorthSpriteWall, 10);
            tracker.addImage(wallCornerSouthSpriteWall, 11);

            tracker.addImage(wheelchairNorthSprite, 12);
            tracker.addImage(wheelchairEastSprite, 13);
            tracker.addImage(wheelchairSouthSprite, 14);
            tracker.addImage(wheelchairWestSprite, 15);

            tracker.addImage(drawerSWSprite, 16);

            tracker.addImage(playerNorthSprite, 17);
            tracker.addImage(playerSouthSprite, 18);
            tracker.addImage(playerWestSprite, 19);
            tracker.addImage(playerEastSprite, 20);

            tracker.addImage(syringe_adrenaline, 21);

            tracker.addImage(playerNorthWheelchairSprite, 22);
            tracker.addImage(playerSouthWheelchairSprite, 23);
            tracker.addImage(playerWestWheelchairSprite, 24);
            tracker.addImage(playerEastWheelchairSprite, 25);

            tracker.waitForAll();
        } catch (Exception e) {
            // bedSprite = createPlaceholderBed();
            // floorSprite = createPlaceholderFloor();
            //  wallNESWSpriteFloor = createPlaceholderFloor();
            //  wallNESWSpriteWall = createPlaceholderWall();
        }
    }
}
