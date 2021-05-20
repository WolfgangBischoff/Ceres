package Core.WorldView;

import Core.*;
import Core.Configs.Config;
import Core.Enums.ActorTag;
import Core.Enums.Direction;
import Core.GameTime.DateTime;
import Core.GameTime.Time;
import Core.GameTime.TimeMode;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Menus.CoinGame.CoinGame;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.Menus.Inventory.InventoryController;
import Core.Menus.Personality.PersonalityScreenController;
import Core.Menus.StatusOverlay.*;
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
import static Core.WorldView.WorldViewStatus.WORLD;
import static javafx.scene.paint.Color.BLACK;

public class WorldView
{
    private static final String CLASSNAME = "WorldView/";
    //Inventory Overlay
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
    private static WorldView singleton;
    private static Rectangle2D borders;
    String levelName;

    //Render
    Pane root;
    Canvas worldCanvas;
    GraphicsContext gc;
    Canvas shadowMask;
    GraphicsContext shadowMaskGc;
    Canvas hudCanvas;
    Map<String, Image> lightsImageMap = new HashMap<>();
    Color shadowColor;

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

    public static void addToLayer(Sprite sprite)
    {
        getBottomLayer().remove(sprite);
        getMiddleLayer().remove(sprite);
        getUpperLayer().remove(sprite);
        if (sprite.getActor() != null && !actorList.contains(sprite.getActor()))
            actorList.add(sprite.getActor());
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

    public void changeStage(String levelName, String spawnId, boolean invalidateSavedStages)
    {
        if (!invalidateSavedStages)
            saveStage();
        else
            GameVariables.getLevelData().forEach((stageName, stage) -> stage.setValid(false));
        loadStage(levelName, spawnId);
    }

    private void saveStage()
    {
        String levelNameToSave = this.levelName;
        actorSpritesLayer.remove(player);
        middleLayer.remove(player); //Player Layer
        GameVariables.setPlayer(player);
        GameVariables.saveLevelState(new LevelState(levelNameToSave, GameVariables.gameDateTime().getDays(), borders, actorSpritesLayer, passiveSpritesLayer, bottomLayer, middleLayer, upperLayer, shadowColor, spawnPointsMap, GameVariables.getClock().getTimeMode(), actorList));
    }

    public void loadStage(String levelName, String spawnId)
    {
        String methodName = "loadStage() ";
        clearLevel();
        this.levelName = levelName;
        setFadedOut(false);
        fadedOutPercent = 1;
        LevelState levelState = GameVariables.getLevelData().get(this.levelName);
        //System.out.println(CLASSNAME + methodName + "past days: " + GameVariables.gameDateTime().getDays());

        if (levelState != null && levelState.isValid())
            loadFromLevelDailyState(levelState, spawnId);
        else if (levelState != null)//Level was already loaded on another day
        {
            System.out.println(CLASSNAME + methodName + "loaded persistent state");
            loadLevelFromPersistentState(levelState, spawnId);//TODO find solution for respawning items to spawn after time not always if invalidated
        }
        else //Level loaded the first time
        {
            System.out.println(CLASSNAME + methodName + "loaded from file");
            loadLevelFromFile(spawnId);
        }

        offsetMaxX = borders.getMaxX() - CAMERA_WIDTH;
        offsetMaxY = borders.getMaxY() - CAMERA_HEIGHT;
    }

    private void clearLevel()
    {
        //Not clear(), lists are copied to LevelState
        actorList = new ArrayList<>();
        passiveSpritesLayer = new ArrayList<>();
        actorSpritesLayer = new ArrayList<>();
        bottomLayer = new ArrayList<>();
        middleLayer = new ArrayList<>();
        upperLayer = new ArrayList<>();
        topLayer = new ArrayList<>();
        passiveCollisionRelevantSpritesLayer = new ArrayList<>();
        borders = null;
        shadowColor = null;
    }

    private void loadLevelFromPersistentState(LevelState levelState, String spawnId)
    {
        String methodName = "loadLevelFromPersistentState() ";
        WorldLoader worldLoader = new WorldLoader();
        worldLoader.load(levelName, spawnId);
        List<Sprite> tmp_passiveSpritesLayer = worldLoader.getPassivLayer();
        List<Sprite> tmp_actorSpritesLayer = worldLoader.getActorSprites();
        List<Sprite> tmp_bottomLayer = worldLoader.getBttmLayer();
        List<Sprite> tmp_middleLayer = worldLoader.getMediumLayer();
        List<Sprite> tmp_upperLayer = worldLoader.getUpperLayer();
        List<Sprite> tmp_topLayer = worldLoader.getTopLayer();
        List<Actor> tmp_actors = worldLoader.getActorsList();

        //remove persistent actors, just not persistent should remain or actorless sprites
        tmp_actorSpritesLayer = tmp_actorSpritesLayer.stream()
                .filter(sprite ->
                                sprite.getActor() == null || //just a tile
                                        !sprite.getActor().tags.contains(ActorTag.PERSISTENT) // Wenn du den Tag hast, wirst du nicht neu geladen
                        //  || (sprite.getActor().tags.contains(ActorTag.PERSISTENT) && !activeSpritesLayer.contains(sprite))//persisten
                )
                .collect(Collectors.toList());
        tmp_bottomLayer = tmp_bottomLayer.stream()
                .filter(sprite ->
                        sprite.getActor() == null || !sprite.getActor().tags.contains(ActorTag.PERSISTENT))
                .collect(Collectors.toList());
        tmp_middleLayer = tmp_middleLayer.stream()
                .filter(sprite ->
                        sprite.getActor() == null || !sprite.getActor().tags.contains(ActorTag.PERSISTENT))
                .collect(Collectors.toList());
        tmp_upperLayer = tmp_upperLayer.stream()
                .filter(sprite ->
                        sprite.getActor() == null || !sprite.getActor().tags.contains(ActorTag.PERSISTENT))
                .collect(Collectors.toList());
        tmp_topLayer = tmp_topLayer.stream()
                .filter(sprite ->
                        sprite.getActor() == null || !sprite.getActor().tags.contains(ActorTag.PERSISTENT))
                .collect(Collectors.toList());

        tmp_actors = tmp_actors.stream()
                .filter(actor ->
                        !actor.tags.contains(ActorTag.PERSISTENT))
                .collect(Collectors.toList());
        var persistentActors = levelState.getactorList().stream().filter(a -> a.tags.contains(ActorTag.PERSISTENT)).collect(Collectors.toList());
        tmp_actors.addAll(persistentActors);

        //add persistent actors from state
        for (Sprite activeSprite : levelState.getActorSpritesLayer())
        {
            if (activeSprite.getActor().tags.contains(ActorTag.PERSISTENT))
            {
                //System.out.println(CLASSNAME + methodName + activeSprite.getActor().getActorInGameName());
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
        player = GameVariables.getPlayer();
        WorldLoader.SpawnData spawnData = levelState.getSpawnPointsMap().get(spawnId);
        player.getActor().setDirection(spawnData.getDirection());
        player.setPosition(spawnData.getX() * 64, spawnData.getY() * 64);
        middleLayer.add(player); //assumption player on layer 1
        actorSpritesLayer.add(player);

        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        passiveCollisionRelevantSpritesLayer.addAll(topLayer);
        borders = worldLoader.getBorders();
        shadowColor = worldLoader.getShadowColor();
        spawnPointsMap = worldLoader.getSpawnPointsMap();
        GameVariables.getClock().setTimeMode(worldLoader.getTimeMode());
    }

    private void loadLevelFromFile(String spawnId)
    {
        WorldLoader worldLoader = new WorldLoader();
        worldLoader.load(levelName, spawnId);
        player = worldLoader.getPlayer();
        passiveSpritesLayer = worldLoader.getPassivLayer(); //No collision just render
        actorSpritesLayer = worldLoader.getActorSprites();
        bottomLayer = worldLoader.getBttmLayer(); //Render height
        middleLayer = worldLoader.getMediumLayer();
        upperLayer = worldLoader.getUpperLayer();
        topLayer = worldLoader.getTopLayer();
        actorList = worldLoader.getActorsList();
        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        passiveCollisionRelevantSpritesLayer.addAll(topLayer);
        borders = worldLoader.getBorders();
        setShadowColor(worldLoader.getShadowColor());
        spawnPointsMap = worldLoader.getSpawnPointsMap();
        GameVariables.getClock().setTimeMode(worldLoader.getTimeMode());
    }

    private void loadFromLevelDailyState(LevelState levelState, String spawnId)
    {
        String methodName = "loadFromLevelDailyState() ";
        actorList = levelState.getactorList();
        passiveSpritesLayer = levelState.getPassiveSpritesLayer(); //No collision just render
        actorSpritesLayer = levelState.getActorSpritesLayer();
        bottomLayer = levelState.getBottomLayer(); //Render height
        middleLayer = levelState.getMiddleLayer();
        upperLayer = levelState.getTopLayer();
        borders = levelState.getBorders();
        shadowColor = levelState.getShadowColor();
        spawnPointsMap = levelState.getSpawnPointsMap();
        GameVariables.getClock().setTimeMode(levelState.getTimeMode());

        //Player
        player = GameVariables.getPlayer();
        WorldLoader.SpawnData spawnData = levelState.getSpawnPointsMap().get(spawnId);
        player.getActor().setDirection(spawnData.getDirection());
        player.setPosition(spawnData.getX() * 64, spawnData.getY() * 64);
        middleLayer.add(player); //assumption player on layer 1
        actorSpritesLayer.add(player);

        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);

        System.out.println(CLASSNAME + methodName);
    }

    public void update(Long currentUpdateTime)
    {
        String methodName = "update(Long) ";
        long updateStartTime = System.nanoTime();
        ArrayList<String> input = GameWindow.getInput();
        double elapsedTimeSinceLastInteraction = (currentUpdateTime - lastTimeMenuWasOpened) / 1000000000.0;

        updateAccordingToTime();

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
            case INCUBATOR:
                break;
            default:
                System.out.println(CLASSNAME + methodName + "Undefined WorldViewStatus: " + WorldViewController.getWorldViewStatus());
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
        GameVariables.updateFromTime(currentUpdateTime, actorList);
        long hudEndTime = System.nanoTime();

        long timeInput = inputEndTime - updateStartTime;
        long timeSprites = spritesEndTime - updateStartTime - timeInput;
        long hudTime = hudEndTime - updateStartTime - timeInput - timeSprites;
        FXUtils.addData("Input: " + timeInput / 1000000);
        FXUtils.addData("Sprites: " + timeSprites / 1000000);
        FXUtils.addData("Hud: " + hudTime / 1000000);
        //System.out.println("inputTime: " + timeInput + " sprites: " + timeSprites + " hud: " + hudTime);
    }

    private void updateAccordingToTime()
    {
        DateTime time = GameVariables.gameDateTime();
        //update Level
        if (shadowColor == COLOR_EMERGENCY_LIGHT)
        {
            //No energy
        }
        else if (Time.isWithin(DAY_LIGHT_ON_TIME, DAY_LIGHT_OFF_TIME, time.getTime()))
        {
            setShadowColor(null);
        }
        else
            setShadowColor(COLOR_NIGHT_LIGHT);

        //TODO update Actors that are time dependent
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
        String methodName = "processInputAsMovement(ArrayList<String>()";
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
                for (Sprite clicked : mouseHoveredSprites)
                    if (isMouseClicked)
                        clicked.onClick(currentNanoTime);//Wraps onInteraction
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
            default:
                System.out.println(CLASSNAME + methodName + "mouseinput undefined for: " + WorldViewController.getWorldViewStatus());

        }

        for (Sprite active : actorSpritesLayer)
            if (active.intersectsRelativeToWorldView(mousePosition) && DEBUG_MOUSE_ANALYSIS && active.getActor() != null && isMouseClicked)
            {
                Actor actor = active.getActor();
                System.out.println(actor.getActorInGameName() + ": " + actor.getSensorStatus().getStatusName() + " Sprite: " + actor.getGeneralStatus());
            }
        Point2D mouseWorldPosition = new Point2D(mousePosition.getX() + camX, mousePosition.getY() + camY);
        if (DEBUG_MOUSE_ANALYSIS && isMouseClicked)
            System.out.println(CLASSNAME + methodName + "Clicked on tile X/Y " + (int) mouseWorldPosition.getX() / 64 + "/" + (int) mouseWorldPosition.getY() / 64 + ", exact: " + Utilities.roundTwoDigits(mouseWorldPosition.getX()) + "/" + Utilities.roundTwoDigits(mouseWorldPosition.getY()));

        GameWindow.getSingleton().setMouseClicked(false);
    }

    private void calcCameraPosition()
    {
        String methodName = "calcCameraPosition() ";
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
        String methodName = "render(Long) ";
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
        }

        //Debugdata
        if (Config.DEBUG_BLOCKER)
        {
            gc.setStroke(Color.RED);
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
        inventoryController.renderQuickInventory(hudCanvas.getGraphicsContext2D());
        hungerOverlay.render(hudCanvas.getGraphicsContext2D());
        mamOverlay.render(hudCanvas.getGraphicsContext2D());
        moneyOverlay.render(hudCanvas.getGraphicsContext2D());
        if (GameVariables.getClock().getTimeMode() == TimeMode.RUNNING)
            boardTimeOverlay.render(hudCanvas.getGraphicsContext2D());
        if (centralMessageOverlay.isVisible())
            centralMessageOverlay.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
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

    public String getLevelName()
    {
        return levelName;
    }

    public Canvas getWorldCanvas()
    {
        return worldCanvas;
    }

    public GraphicsContext getGc()
    {
        return gc;
    }

    public void setShadowColor(Color shadowColor)
    {
        this.shadowColor = shadowColor;
    }

    public boolean isFadedOut()
    {
        return isFadedOut;
    }

    public void setFadedOut(boolean fadedOut)
    {
        isFadedOut = fadedOut;
    }
}
