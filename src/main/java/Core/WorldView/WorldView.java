package Core.WorldView;

import Core.*;
import Core.Configs.Config;
import Core.Enums.ActorTag;
import Core.Enums.Direction;
import Core.GameTime.ClockMode;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Menus.CoinGame.CoinGame;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.Menus.Email.EmailButtonOverlay;
import Core.Menus.Email.EmailOverlay;
import Core.Menus.Inventory.InventoryController;
import Core.Menus.Personality.PersonalityScreenController;
import Core.Menus.StatusOverlay.BarStatusConfig;
import Core.Menus.StatusOverlay.BarStatusOverlay;
import Core.Menus.StatusOverlay.ClockOverlay;
import Core.Menus.StatusOverlay.VariableStatusOverlay;
import Core.Menus.Textbox.Textbox;
import Core.Sprite.Sprite;
import Core.Sprite.SpriteComparator;
import Core.Utils.FXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.INVENTORY;
import static Core.WorldView.WorldViewStatus.WORLD;
import static javafx.scene.paint.Color.BLACK;

public class WorldView
{
    private static final String CLASSNAME = "WorldView/";
    static InventoryController inventoryController;
    static Long lastTimeMenuWasOpened = 0L;
    static Textbox textbox;
    static PersonalityScreenController personalityScreenController;
    static Point2D personalityScreenPosition = PERSONALITY_POSITION;
    static CoinGame coinGame;
    static DaySummaryScreenController daySummaryScreenController = new DaySummaryScreenController();
    static Point2D daySummaryScreenPosition = DAY_SUMMARY_POSITION;
    static BarStatusOverlay mamOverlay = new BarStatusOverlay(
            new BarStatusConfig("interface/bars/MaM_bar_400x64.png", null, COLOR_RED,
                    MAM_BAR_WIDTH, MAM_BAR_HEIGHT, 100, GameVariables.getPlayerMaM_duringDayProperty(), MAM_BAR_POSITION));
    static VariableStatusOverlay moneyOverlay = new VariableStatusOverlay(MONEY_FIELD_WIDTH, MONEY_FIELD_HEIGHT, GameVariables.playerMoneyProperty(), "interface/bars/money_field_150x64.png", MONEY_POSITION);
    static BarStatusOverlay hungerOverlay = new BarStatusOverlay(new BarStatusConfig("interface/bars/food_bar_400x64.png", null, COLOR_GREEN,
            MAM_BAR_WIDTH, MAM_BAR_HEIGHT, MAX_HUNGER, GameVariables.playerHungerProperty(), HUNGER_BAR_POSITION));
    static EmailButtonOverlay emailButtonOverlay = new EmailButtonOverlay();
    static EmailOverlay emailOverlay = new EmailOverlay();
    static ClockOverlay boardTimeOverlay;
    static CentralMessageOverlay centralMessageOverlay = new CentralMessageOverlay();
    static List<Actor> actorList = new ArrayList<>();
    static List<Sprite> actorSpritesLayer = new ArrayList<>();
    static List<Sprite> passiveCollisionRelevantSpritesLayer = new ArrayList<>();
    static List<Sprite> passiveSpritesLayer = new ArrayList<>();
    static List<Sprite> bottomLayer = new ArrayList<>();
    static List<Sprite> middleLayer = new ArrayList<>();
    static List<Sprite> upperLayer = new ArrayList<>();
    static List<Sprite> topLayer = new ArrayList<>();
    static Sprite player;
    static Map<String, WorldLoader.SpawnData> spawnPointsMap = new HashMap<>();
    static List<Sprite> toRemove = new ArrayList<>();
    static double camX;
    static double camY;
    static String levelName;
    static Color shadowColor;
    private static WorldView singleton;
    private static Rectangle2D borders;
    private static MapTimeData mapTimeData;

    //Render
    Pane root;
    Canvas worldCanvas;
    GraphicsContext gc;
    Canvas shadowMask;
    GraphicsContext shadowMaskGc;
    Canvas hudCanvas;
    Map<String, Image> lightsImageMap = new HashMap<>();
    Canvas blackOverlayCanvas;
    GraphicsContext blackOverlayGc;
    boolean isFadedOut = false;
    float fadedOutPercent = 0;
    long lastBlackOverlayChangeTime = 0;
    //Camera
    double offsetMaxX;
    double offsetMaxY;
    int offsetMinX = 0;
    int offsetMinY = 0;
    double bumpX = 0, bumpY = 0, rumbleGrade = RUMBLE_GRADE;
    Long timeStartBump = null;
    float durationBump = RUMBLE_MAX_DURATION;
    boolean bumpActive = false;
    private GridManager gridManager = new GridManager();

    private WorldView(String levelName)
    {
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        root = stackPane;
        worldCanvas = new Canvas(CAMERA_WIDTH, Config.CAMERA_HEIGHT);
        shadowMask = new Canvas(CAMERA_WIDTH, Config.CAMERA_HEIGHT);
        blackOverlayCanvas = new Canvas(CAMERA_WIDTH, CAMERA_HEIGHT);
        blackOverlayGc = blackOverlayCanvas.getGraphicsContext2D();
        hudCanvas = new Canvas(CAMERA_WIDTH, CAMERA_HEIGHT);
        hudCanvas.getGraphicsContext2D().setFont(FONT_ESTROG_30_DEFAULT);
        gc = worldCanvas.getGraphicsContext2D();
        gc.setFont(FONT_ESTROG_30_DEFAULT);
        GameVariables.init();
        shadowMaskGc = shadowMask.getGraphicsContext2D();
        loadStage(levelName, "default");
        inventoryController = new InventoryController();
        textbox = new Textbox();
        WorldViewController.setWorldViewStatus(WORLD);
        boardTimeOverlay = new ClockOverlay(new BarStatusConfig("interface/bars/clock.png", null, null,
                BOARD_TIME_WIDTH, BOARD_TIME_HEIGHT, 0, null, BOARD_TIME_POSITION), GameVariables.getClock());
    }

    public static void startConversation(String dialogueFile, String dialogueId, Long currentNanoTime)
    {
        textbox.startConversation(dialogueFile, dialogueId);
        WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
        player.getActor().setLastInteraction(currentNanoTime);
    }

    public static void addToLayer(Sprite sprite)
    {
        getBottomLayer().remove(sprite);
        getMiddleLayer().remove(sprite);
        getUpperLayer().remove(sprite);
        if (sprite.getActor() != null && !actorList.contains(sprite.getActor()))
        {
            actorList.add(sprite.getActor());
            passiveCollisionRelevantSpritesLayer.remove(sprite);
            passiveCollisionRelevantSpritesLayer.add(sprite);
        }
        switch (sprite.getLayer())
        {
            case 0:
                getBottomLayer().add(sprite);
                break;
            case 1:
                getMiddleLayer().add(sprite);
                break;
            case 2:
                getUpperLayer().add(sprite);
                break;
        }
    }

    public static boolean isSpriteLoaded(Sprite sprite)
    {
        return bottomLayer.contains(sprite) || middleLayer.contains(sprite) || upperLayer.contains(sprite) || topLayer.contains(sprite);
    }

    public static boolean isSpriteAtPosition(List<Sprite> layer, Rectangle2D checkedArea)
    {
        for (Sprite sprite : layer)
        {
            if (sprite.getHitbox().intersects(checkedArea))
                return true;
        }
        return false;
    }

    public static Actor getSpriteAtPosition(List<Sprite> layer, Rectangle2D checkedArea)
    {
        for (Sprite sprite : layer)
        {
            if (sprite.getHitbox().intersects(checkedArea) && sprite.hasActor())
                return sprite.getActor();
        }
        return null;
    }

    private void clearLevel()
    {
        actorList = new ArrayList<>();
        passiveSpritesLayer = new ArrayList<>();
        actorSpritesLayer = new ArrayList<>();
        bottomLayer = new ArrayList<>();
        middleLayer = new ArrayList<>();
        upperLayer = new ArrayList<>();
        topLayer = new ArrayList<>();
        passiveCollisionRelevantSpritesLayer = new ArrayList<>();
        borders = null;
    }

    public void changeStage(String levelName, String spawnId)
    {
        saveStage();
        loadStage(levelName, spawnId);
    }

    public void changeStage(String levelName, Point2D spawnPoint)
    {
        Direction d = player.getActor().getDirection();
        changeStage(levelName, "default");
        player.setPosition(spawnPoint);//Maybe refactor out from WorldLoader
        player.getActor().setDirection(d);
    }

    private void saveStage()
    {
        var persistentActors = actorList.stream().filter(a -> a.hasTag(ActorTag.PERSISTENT)).collect(Collectors.toList());
        GameVariables.saveLevelState(levelName, persistentActors);
    }

    public void loadStage(String levelName, String spawnId)
    {
        clearLevel();
        WorldView.levelName = levelName;
        setFadedOut(false);
        fadedOutPercent = 1;

        loadLevelFromPersistentState(spawnId);
        GameVariables.getClock().updateWorldFromTime(GameWindow.getCurrentNanoRenderTimeGameWindow());

        offsetMaxX = borders.getMaxX() - CAMERA_WIDTH;
        offsetMaxY = borders.getMaxY() - CAMERA_HEIGHT;
    }



    private void loadLevelFromPersistentState(String spawnId)
    {
        boolean readFirstTime = !GameVariables.levelDataExists(levelName);
        LevelState levelState = GameVariables.getLevelData(levelName);
        WorldLoader worldLoader = new WorldLoader();
        worldLoader.load(levelName, spawnId, readFirstTime);
        List<Sprite> tmp_passiveSpritesLayer = worldLoader.getPassivLayer();
        List<Sprite> tmp_actorSpritesLayer = worldLoader.getActorSprites();
        List<Sprite> tmp_bottomLayer = worldLoader.getBttmLayer();
        List<Sprite> tmp_middleLayer = worldLoader.getMiddleLayer();
        List<Sprite> tmp_upperLayer = worldLoader.getUpperLayer();
        List<Sprite> tmp_topLayer = worldLoader.getTopLayer();
        List<Actor> tmp_actors = worldLoader.getActorsList();

        var persistentActors = levelState.getactorList().stream().filter(a -> a.tags.contains(ActorTag.PERSISTENT)).collect(Collectors.toList());
        tmp_actors.addAll(persistentActors);
        List<Sprite> persistentActorSprites = persistentActors.stream().flatMap(actor -> actor.getSpriteList().stream()).collect(Collectors.toList());
        //add persistent actors from state
        for (Sprite activeSprite : persistentActorSprites)
        {
            if (activeSprite.getActor().tags.contains(ActorTag.PERSISTENT))
            {
                tmp_actorSpritesLayer.add(activeSprite);
                switch (activeSprite.getLayer())
                {
                    case 0:
                        tmp_bottomLayer.add(activeSprite);
                        break;
                    case 1:
                        tmp_middleLayer.add(activeSprite);
                        break;
                    case 2:
                        tmp_upperLayer.add(activeSprite);
                        break;
                    case 3:
                        tmp_topLayer.add(activeSprite);
                        break;
                    default:
                        throw new RuntimeException("Invalid Layer: " + activeSprite.getLayer());
                }
            }
        }

        passiveSpritesLayer = tmp_passiveSpritesLayer;
        actorSpritesLayer = tmp_actorSpritesLayer;
        actorList = tmp_actors;
        bottomLayer = tmp_bottomLayer;
        middleLayer = tmp_middleLayer;
        upperLayer = tmp_upperLayer;
        topLayer = tmp_topLayer;

        //Player
        player = GameVariables.getPlayer() == null ? worldLoader.getPlayer() : GameVariables.getPlayer();
        WorldLoader.SpawnData spawnData = worldLoader.getSpawnPointsMap().get(spawnId);// levelState.getSpawnPointsMap().get(spawnId);
        player.getActor().setDirection(spawnData.getDirection());
        player.setPosition(spawnData.getX() * 64, spawnData.getY() * 64);
        middleLayer.add(player); //assumption player on layer 1
        actorSpritesLayer.add(player);

        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        passiveCollisionRelevantSpritesLayer.addAll(topLayer);
        borders = worldLoader.getBorders();
        var persistentShadowColor = shadowColor == COLOR_EMERGENCY_LIGHT ? COLOR_EMERGENCY_LIGHT : worldLoader.getShadowColor();
        setShadowColor(persistentShadowColor);
        spawnPointsMap = worldLoader.getSpawnPointsMap();
        GameVariables.getClock().setClockMode(worldLoader.getClockMode());
        mapTimeData = worldLoader.getMapTimeData();
    }

    public void update(Long currentUpdateTime)
    {
        long updateStartTime = System.nanoTime();
        ArrayList<String> input = GameWindow.getInput();
        double elapsedTimeSinceLastInteraction = (currentUpdateTime - lastTimeMenuWasOpened) / 1000000000.0;

        //Test Menu Hotkeys
        if (input.contains("T") && elapsedTimeSinceLastInteraction > 1)
            loadStage("test", "default");
        if (input.contains("Z") && elapsedTimeSinceLastInteraction > 1)
            activateBump();
        if (input.contains("U") && elapsedTimeSinceLastInteraction > 1)
        {
            if (shadowColor != COLOR_EMERGENCY_LIGHT)
                shadowColor = COLOR_EMERGENCY_LIGHT;
            else
                shadowColor = null;
            lastTimeMenuWasOpened = currentUpdateTime;
        }
        if (input.contains("I") && elapsedTimeSinceLastInteraction > 1)
        {
            isFadedOut = !isFadedOut;
            lastTimeMenuWasOpened = currentUpdateTime;
        }

        //Process Input
        if (WorldViewController.getWorldViewStatus() != WORLD && player.getActor().isMoving())
            player.getActor().setVelocity(0, 0);
        switch (WorldViewController.getWorldViewStatus())
        {
            case WORLD:
                processInputAsMovement(input, currentUpdateTime);
                break;
            case TEXTBOX:
                textbox.processKey(input, currentUpdateTime);
                break;
            case PERSONALITY:
                personalityScreenController.processKey(input, currentUpdateTime);
                break;
            case DAY_SUMMARY:
                daySummaryScreenController.processKey(input, currentUpdateTime);
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
                if (input.contains(KEYBOARD_INVENTORY) || input.contains(KEYBOARD_INTERACT) || input.contains(KEYBOARD_ESCAPE))
                    toggleInventory(currentUpdateTime);
                break;
            case COIN_GAME://No keyboard input so far
                coinGame.update(currentUpdateTime);
                break;
            case EMAIL:
            case INCUBATOR:
                break;
            case COLLECTIBLE_USE:
                gridManager.setCollectibleToPlace(inventoryController.getMenuCollectible());
                if (input.contains(KEYBOARD_ESCAPE))
                {
                    WorldViewController.setWorldViewStatus(WORLD);
                    inventoryController.setMenuCollectible(CollectibleStack.empty());
                }
                break;

            default:
                System.out.println(CLASSNAME  + "Undefined WorldViewStatus: " + WorldViewController.getWorldViewStatus());
        }

        processMouse(currentUpdateTime);

        long inputEndTime = System.nanoTime();
        List<Sprite> spritesOfActiveActor = actorList.stream().filter(Actor::isActiveActor).map(Actor::getSpriteList).collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());//Maybe a predefined list?
        for (Sprite active : spritesOfActiveActor)
            active.update(currentUpdateTime);
        for (Sprite sprite : toRemove)
        {
            WorldView.bottomLayer.remove(sprite);
            WorldView.middleLayer.remove(sprite);
            WorldView.upperLayer.remove(sprite);
            WorldView.actorSpritesLayer.remove(sprite);
            WorldView.passiveSpritesLayer.remove(sprite);
            WorldView.passiveCollisionRelevantSpritesLayer.remove(sprite);
        }
        toRemove.clear();
        long spritesEndTime = System.nanoTime();

        calcCameraPosition();

        GameVariables.getClock().tryIncrementTime(currentUpdateTime);
        long hudEndTime = System.nanoTime();

        long timeInput = inputEndTime - updateStartTime;
        long timeSprites = spritesEndTime - updateStartTime - timeInput;
        long hudTime = hudEndTime - updateStartTime - timeInput - timeSprites;
        FXUtils.addData("Input: " + timeInput / 1000000);
        FXUtils.addData("Sprites: " + timeSprites / 1000000);
        FXUtils.addData("Hud: " + hudTime / 1000000);
        //System.out.println("inputTime: " + timeInput + " sprites: " + timeSprites + " hud: " + hudTime);
    }

    private void toggleInventory(Long currentNanoTime)
    {
        double elapsedTimeSinceLastInteraction = (currentNanoTime - player.getActor().getLastInteraction()) / 1000000000.0;
        if (elapsedTimeSinceLastInteraction > 1)
        {
            WorldViewController.toggleInventory();
            player.getActor().setLastInteraction(currentNanoTime);
        }
    }

    private void processInputAsMovement(ArrayList<String> input, Long currentNanoTime)
    {
        boolean moveButtonPressed = false;
        int addedVelocityX = 0, addedVelocityY = 0;
        Direction newDirection = null;
        Actor playerActor = player.getActor();
        double elapsedTimeSinceLastInteraction = (currentNanoTime - playerActor.getLastInteraction()) / 1000000000.0;

        if (input.contains("LEFT") || input.contains("A"))
        {
            addedVelocityX += -playerActor.getVelocity();
            moveButtonPressed = true;
            newDirection = Direction.WEST;
        }
        if (input.contains("RIGHT") || input.contains("D"))
        {
            addedVelocityX += playerActor.getVelocity();
            moveButtonPressed = true;
            newDirection = Direction.EAST;
        }
        if (input.contains("UP") || input.contains("W"))
        {
            addedVelocityY += -playerActor.getVelocity();
            moveButtonPressed = true;
            newDirection = Direction.NORTH;
        }
        if (input.contains("DOWN") || input.contains("S"))
        {
            addedVelocityY += playerActor.getVelocity();
            moveButtonPressed = true;
            newDirection = Direction.SOUTH;
        }
        if (input.contains(KEYBOARD_SPRINT))
        {
            addedVelocityX *= 2;
            addedVelocityY *= 2;
        }

        if (moveButtonPressed)
            player.getActor().setVelocity(addedVelocityX, addedVelocityY);
        else if (player.getActor().isMoving())
            player.getActor().setVelocity(0, 0);

        if (newDirection != null && playerActor.getDirection() != newDirection)
            playerActor.setDirection(newDirection);

        if (input.contains(KEYBOARD_INTERACT) && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS)
        {
            player.setInteract(true);
        }

        if (input.contains(KEYBOARD_INVENTORY))
        {
            toggleInventory(currentNanoTime);
        }

    }

    private void processMouse(Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        Point2D mousePosition = GameWindow.getSingleton().getMousePosition();
        boolean isMouseClicked = GameWindow.getSingleton().isMouseClicked();
        boolean isMouseDragged = GameWindow.getSingleton().isMouseDragged();

        Set<Sprite> mouseHoveredSprites = new HashSet<>();
        for (Sprite blocker : passiveCollisionRelevantSpritesLayer)
            if (blocker.intersectsRelativeToWorldView(mousePosition))
                mouseHoveredSprites.add(blocker);

        switch (WorldViewController.getWorldViewStatus())
        {
            case WORLD:
                if (EmailButtonOverlay.getScreenArea().contains(mousePosition))
                {
                    emailButtonOverlay.processMouse(mousePosition, isMouseClicked);
                }
                else
                {
                    for (Sprite clicked : mouseHoveredSprites)
                        if (isMouseClicked)
                            clicked.onClick(currentNanoTime);//Wraps onInteraction
                }
                break;
            case COIN_GAME:
                coinGame.processMouse(mousePosition, isMouseClicked, currentNanoTime);
                break;
            case DAY_SUMMARY:
                daySummaryScreenController.processMouse(mousePosition, isMouseClicked, currentNanoTime);
                break;
            case PERSONALITY:
                personalityScreenController.processMouse(mousePosition, isMouseClicked, currentNanoTime);
                break;
            case TEXTBOX:
                textbox.processMouse(mousePosition, isMouseClicked);
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
            case INCUBATOR:
                inventoryController.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
                break;
            case COLLECTIBLE_USE:
                mouseWorldInteraction(mousePosition, isMouseClicked);
                break;
            case EMAIL:
                emailOverlay.processMouse(mousePosition, isMouseClicked);
                break;
            default:
                System.out.println(CLASSNAME + methodName + "mouseinput undefined for: " + WorldViewController.getWorldViewStatus());

        }

        if (DEBUG_MOUSE_ANALYSIS)
            for (Sprite active : actorSpritesLayer)
                if (active.intersectsRelativeToWorldView(mousePosition) && active.getActor() != null && isMouseClicked)
                {
                    Actor actor = active.getActor();
                    System.out.println(actor.getActorInGameName() + ": " + actor.getSensorStatus().getStatusName() + " Sprite: " + actor.getGeneralStatus() + " : " + actor.getGenericActorAttributes().toString());
                }
        Point2D mouseWorldPosition = new Point2D(mousePosition.getX() + camX, mousePosition.getY() + camY);
        if (DEBUG_MOUSE_ANALYSIS && isMouseClicked)
            System.out.println(CLASSNAME + methodName + "Clicked on tile X/Y " + (int) mouseWorldPosition.getX() / 64 + "/" + (int) mouseWorldPosition.getY() / 64 + ", exact: " + Utilities.roundTwoDigits(mouseWorldPosition.getX()) + "/" + Utilities.roundTwoDigits(mouseWorldPosition.getY()));

        GameWindow.getSingleton().setMouseClicked(false);
    }

    private void mouseWorldInteraction(Point2D mousePosition, boolean isMouseClicked)
    {
        int xpos = (int) ((getCamX() + mousePosition.getX()) - (getCamX() + mousePosition.getX()) % 64);
        int ypos = (int) ((getCamY() + mousePosition.getY()) - (getCamY() + mousePosition.getY()) % 64);
        gridManager.setHoveredGrid(new Rectangle2D(xpos, ypos, 64, 64));
        if (isMouseClicked && !gridManager.isGridBlocked()) //set on ground
        {
            System.out.println(CLASSNAME + "Clicked on tile X/Y " + xpos + "/" + ypos);
            getPlayer().getActor().getInventory().removeCollectibleStack(gridManager.collectibleToPlace);
            GameVariables.getStolenCollectibles().remove(gridManager.collectibleToPlace);//TODO das wird nicht mehr funktionieren, items verschwinden ja
            inventoryController.setMenuCollectible(CollectibleStack.empty());
            addToLayer(gridManager.collectibeSprite);
            WorldViewController.setWorldViewStatus(INVENTORY);
        }
        else if (isMouseClicked && gridManager.isGridBlocked() && gridManager.getBlockingActor() != null) //interact with actor at pos
        {
            gridManager.getBlockingActor().interactWithMenuItem(gridManager.collectibleToPlace);
        }
        else if (isMouseClicked)
        {
            System.out.println(CLASSNAME + "Blocked due to other Sprite");
        }

        //close grid
        if (gridManager.collectibleToPlace.isEmpty())
        {
            inventoryController.setMenuCollectible(CollectibleStack.empty());
            WorldViewController.setWorldViewStatus(WORLD);
        }


    }

    private void calcCameraPosition()
    {
        //Camera at world border
        camX = player.getX() - CAMERA_WIDTH / 2f;
        camY = player.getY() - CAMERA_HEIGHT / 2f;
        if (camX < offsetMinX)
            camX = offsetMinX;
        if (camY < offsetMinY)
            camY = offsetMinY;
        if (camX > offsetMaxX)
            camX = offsetMaxX;
        if (camY > offsetMaxY)
            camY = offsetMaxY;

        //If World smaller as Camera
        if (CAMERA_WIDTH > borders.getWidth())
            camX = borders.getWidth() / 2 - CAMERA_WIDTH / 2f;
        if (CAMERA_HEIGHT > borders.getHeight())
            camY = borders.getHeight() / 2 - CAMERA_HEIGHT / 2f;


        //Bump
        if (bumpActive)
        {
            if (timeStartBump == null)
                timeStartBump = GameWindow.getCurrentNanoRenderTimeGameWindow();
            double elapsedTimeSinceBump = (GameWindow.getSingleton().getRenderTime() - timeStartBump) / 1000000000.0;
            double offsetCamX = 0, offsetCamY = 0;
            if (durationBump < elapsedTimeSinceBump)
            {
                bumpActive = false;
                rumbleGrade = RUMBLE_GRADE;//Reset
                timeStartBump = null;//To manual retrigger
            }
            else
            {
                offsetCamX += Math.sin(bumpX) * rumbleGrade;
                offsetCamY += Math.cos(bumpY) * (rumbleGrade + 3);
                bumpX++;
                bumpY++;
                rumbleGrade = rumbleGrade - RUMBLE_GRADE_DECREASE;//Decreasing rumble
                rumbleGrade = Math.max(rumbleGrade, 0);
            }
            camX += offsetCamX;
            camY += offsetCamY;

        }

    }

    public void render(Long currentNanoTime)
    {
        Long methodStartTime = System.nanoTime();
        gc.clearRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        gc.translate(-camX, -camY);

        for (Sprite sprite : passiveSpritesLayer)
        {
            sprite.render(gc, currentNanoTime);
        }
        bottomLayer.sort(new SpriteComparator());
        for (Sprite sprite : bottomLayer)
        {
            sprite.render(gc, currentNanoTime);
        }
        middleLayer.sort(new SpriteComparator());
        for (Sprite sprite : middleLayer)
        {
            sprite.render(gc, currentNanoTime);
        }
        upperLayer.sort(new SpriteComparator());
        for (Sprite sprite : upperLayer)
        {
            sprite.render(gc, currentNanoTime);
        }
        topLayer.sort(new SpriteComparator());
        for (Sprite sprite : topLayer)
        {
            sprite.render(gc, currentNanoTime);
        }

        //Overlays
        hudCanvas.getGraphicsContext2D().clearRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        renderHUD(currentNanoTime);
        switch (WorldViewController.getWorldViewStatus())
        {

            case WORLD:
                break;
            case TEXTBOX:
                textbox.render(hudCanvas.getGraphicsContext2D());
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
            case INCUBATOR:
                inventoryController.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
                break;
            case PERSONALITY:
                WritableImage personalityScreenOverlay = personalityScreenController.getWritableImage();
                hudCanvas.getGraphicsContext2D().drawImage(personalityScreenOverlay, personalityScreenPosition.getX(), personalityScreenPosition.getY());
                break;
            case COIN_GAME:
                coinGame.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
                break;
            case DAY_SUMMARY:
                WritableImage daySummaryImage = daySummaryScreenController.getWritableImage();
                hudCanvas.getGraphicsContext2D().drawImage(daySummaryImage, daySummaryScreenPosition.getX(), daySummaryScreenPosition.getY());
                break;
            case COLLECTIBLE_USE:
                gridManager.drawGrid(gc);
                break;
            case EMAIL:
                emailOverlay.render(hudCanvas.getGraphicsContext2D());
                break;

        }

        //Debugdata
        if (Config.DEBUG_BLOCKER)
        {
            gc.setLineWidth(1);
            gc.strokeRect(borders.getMinX(), borders.getMinY(), borders.getWidth() + player.getBasewidth(), borders.getHeight() + player.getBaseheight());
        }

        root.getChildren().clear();
        root.getChildren().add(worldCanvas);

        //LightMap
        if (shadowColor != null)
        {
            renderLightEffect(currentNanoTime);
            root.getChildren().add(shadowMask);
            shadowMask.setBlendMode(BlendMode.MULTIPLY);
        }

        calcBlackOverlay(currentNanoTime);
        root.getChildren().add(blackOverlayCanvas);
        root.getChildren().add(hudCanvas);
        gc.translate(camX, camY);
    }

    public void calcBlackOverlay(long currentNanoTime)
    {
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastBlackOverlayChangeTime) / 1000000000.0;
        if ((elapsedTimeSinceLastInteraction > 0.05) &&
                ((fadedOutPercent < 1 && isFadedOut) || (fadedOutPercent > 0 && !isFadedOut)))
        {
            blackOverlayGc.clearRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
            fadedOutPercent += isFadedOut ? 0.1 : -0.1;
            blackOverlayGc.setGlobalAlpha(fadedOutPercent);
            blackOverlayGc.setFill(BLACK);
            blackOverlayGc.fillRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
            lastBlackOverlayChangeTime = currentNanoTime;
        }
    }

    public void activateBump()
    {
        this.bumpActive = true;
        timeStartBump = null;
        rumbleGrade = RUMBLE_GRADE;
    }

    private void renderHUD(Long currentNanoTime)
    {
        hungerOverlay.render(hudCanvas.getGraphicsContext2D());
        mamOverlay.render(hudCanvas.getGraphicsContext2D());
        moneyOverlay.render(hudCanvas.getGraphicsContext2D());
        if (mapTimeData.getClockMode() == ClockMode.RUNNING)
            boardTimeOverlay.render(hudCanvas.getGraphicsContext2D());
        if (centralMessageOverlay.isVisible())
            centralMessageOverlay.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
        emailButtonOverlay.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
    }

    private void renderLightEffect(Long currentNanoTime)
    {
        shadowMaskGc.setFill(shadowColor);
        shadowMaskGc.fillRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        for (Sprite sprite : passiveCollisionRelevantSpritesLayer)
        {
            if (sprite.getLightningSpriteName().equalsIgnoreCase("none"))
                continue;

            String lightSpriteName = sprite.getLightningSpriteName();
            if (!lightsImageMap.containsKey(sprite.getLightningSpriteName()))
            {
                lightsImageMap.put(lightSpriteName, Utilities.readImage(IMAGE_DIRECTORY_PATH + "lightglows/" + lightSpriteName + ".png"));
            }
            Image lightImage = lightsImageMap.get(lightSpriteName);
            shadowMaskGc.drawImage(lightImage, sprite.getX() + sprite.getHitBoxOffsetX() + sprite.getHitBoxWidth() / 2 - lightImage.getWidth() / 2 - camX, sprite.getY() + sprite.getHitBoxOffsetY() + sprite.getHitBoxHeight() / 2 - lightImage.getHeight() / 2 - camY);
        }
    }

    public List<Actor> getSpritesByName(String id)
    {
        String methodName = "getSpriteByName() ";
        List<Actor> re = new ArrayList<>();
        for (Sprite sprite : actorSpritesLayer)
        {
            if (sprite.getActor().getActorId().equals(id))
                re.add(sprite.getActor());
        }
        if (re.isEmpty())
            System.out.println(CLASSNAME + methodName + "No Actor found with ID: " + id);
        return re;
    }

    public Pane getRoot()
    {
        return root;
    }

    public Canvas getWorldCanvas()
    {
        return worldCanvas;
    }

    public GraphicsContext getGc()
    {
        return gc;
    }

    public boolean isFadedOut()
    {
        return isFadedOut;
    }

    public void setFadedOut(boolean fadedOut)
    {
        isFadedOut = fadedOut;
    }

    public static Color getShadowColor()
    {
        return shadowColor;
    }

    public static void setShadowColor(Color shadowColor)
    {
        WorldView.shadowColor = shadowColor;
    }

    static public String getLevelName()
    {
        return levelName;
    }

    public static MapTimeData getMapTimeData()
    {
        return mapTimeData;
    }

    public static List<Sprite> getPassiveCollisionRelevantSpritesLayer()
    {
        return passiveCollisionRelevantSpritesLayer;
    }

    public static List<Sprite> getMiddleLayer()
    {
        return middleLayer;
    }

    public static List<Sprite> getUpperLayer()
    {
        return upperLayer;
    }

    public static Rectangle2D getBorders()
    {
        return borders;
    }

    public static Sprite getPlayer()
    {
        return player;
    }

    public static WorldView getSingleton()
    {
        if (singleton == null)
            singleton = new WorldView(Config.FIRST_LEVEL);
        return singleton;
    }

    public static Textbox getTextbox()
    {
        return textbox;
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static void setPersonalityScreenController(PersonalityScreenController personalityScreenController)
    {
        WorldView.personalityScreenController = personalityScreenController;
    }

    public static void setDiscussionGame(CoinGame coinArea)
    {
        WorldView.coinGame = coinArea;
    }

    public static List<Sprite> getBottomLayer()
    {
        return bottomLayer;
    }

    public static Map<String, WorldLoader.SpawnData> getSpawnPointsMap()
    {
        return spawnPointsMap;
    }

    public static List<Sprite> getToRemove()
    {
        return toRemove;
    }

    public static double getCamX()
    {
        return camX;
    }

    public static double getCamY()
    {
        return camY;
    }

    public static List<Sprite> getmiddleLayer()
    {
        return middleLayer;
    }

    public static List<Sprite> getactorSpritesLayer()
    {
        return actorSpritesLayer;
    }

    public static List<Actor> getActorList()
    {
        return actorList;
    }

}
