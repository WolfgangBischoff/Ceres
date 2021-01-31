package Core.WorldView;

import Core.*;
import Core.Configs.Config;
import Core.Enums.ActorTag;
import Core.Enums.Direction;
import Core.GameTime.DateTime;
import Core.GameTime.Time;
import Core.Menus.AchievmentLog.NewMessageOverlay;
import Core.Menus.CoinGame.CoinGame;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.Menus.Inventory.InventoryController;
import Core.Menus.Personality.PersonalityScreenController;
import Core.Menus.StatusOverlay.BarStatusConfig;
import Core.Menus.StatusOverlay.BarStatusOverlay;
import Core.Menus.StatusOverlay.ClockOverlay;
import Core.Menus.StatusOverlay.VariableStatusOverlay;
import Core.Menus.Textbox.Textbox;
import Core.Sprite.Sprite;
import Core.Sprite.SpriteComparator;
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

public class WorldView
{
    private static final String CLASSNAME = "WorldView/";
    private static WorldView singleton;
    Pane root;
    Canvas worldCanvas;
    GraphicsContext gc;
    Canvas shadowMask;
    GraphicsContext shadowMaskGc;
    Canvas hudCanvas;
    Map<String, Image> lightsImageMap = new HashMap<>();
    Color shadowColor;

    //Inventory Overlay
    static InventoryController inventoryController;
    static Point2D inventoryOverlayPosition = new Point2D(0, 0);
    static Long lastTimeMenuWasOpened = 0L;

    //TextBox Overlay
    static Textbox textbox;
    static Point2D textBoxPosition = TEXT_BOX_POSITION;

    //Personality Overlay
    static PersonalityScreenController personalityScreenController;
    static Point2D personalityScreenPosition = PERSONALITY_POSITION;

    //Discussion Game Overlay
    static CoinGame coinGame;
    static Point2D discussionGamePosition = COINGAME_POSITION;

    //DaySummary Overlay
    static DaySummaryScreenController daySummaryScreenController = new DaySummaryScreenController();
    static Point2D daySummaryScreenPosition = DAY_SUMMARY_POSITION;

    //Management Attention Meter Overlay
    static BarStatusOverlay mamOverlay = new BarStatusOverlay(
            new BarStatusConfig("interface/bars/MaM_bar_400x64.png", null, COLOR_RED,
                    MAM_BAR_WIDTH, MAM_BAR_HEIGHT, 100, GameVariables.getPlayerMaM_duringDayProperty(), MAM_BAR_POSITION));
    static Point2D mamOverlayPosition = MAM_BAR_POSITION;

    //Money Overlay
    static VariableStatusOverlay moneyOverlay = new VariableStatusOverlay(MONEY_FIELD_WIDTH, MONEY_FIELD_HEIGHT, GameVariables.playerMoneyProperty(), "interface/bars/money_field_150x64.png", MONEY_POSITION);

    //Hunger Overlay
    static BarStatusOverlay hungerOverlay = new BarStatusOverlay(new BarStatusConfig("interface/bars/food_bar_400x64.png", null, COLOR_GREEN,
            MAM_BAR_WIDTH, MAM_BAR_HEIGHT, MAX_HUNGER, GameVariables.playerHungerProperty(), HUNGER_BAR_POSITION));

    //Clock Overlay
    static ClockOverlay boardTimeOverlay;

    //Message Overlay
    static NewMessageOverlay newMessageOverlay = new NewMessageOverlay();

    //Sprites
    String levelName;
    private static Rectangle2D borders;
    static List<Sprite> activeSpritesLayer = new ArrayList<>();
    static List<Sprite> passiveCollisionRelevantSpritesLayer = new ArrayList<>();
    static List<Sprite> passiveSpritesLayer = new ArrayList<>();
    static List<Sprite> bottomLayer = new ArrayList<>();
    static List<Sprite> middleLayer = new ArrayList<>();
    static List<Sprite> upperLayer = new ArrayList<>();
    static List<Sprite> topLayer = new ArrayList<>();
    static Sprite player;
    static Map<String, WorldLoader.SpawnData> spawnPointsMap = new HashMap<>();
    static List<Sprite> toRemove = new ArrayList<>();

    //Camera
    double offsetMaxX;
    double offsetMaxY;
    int offsetMinX = 0;
    int offsetMinY = 0;
    static double camX;
    static double camY;

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

    public void saveStage()
    {
        String levelNameToSave = this.levelName;
        activeSpritesLayer.remove(player);
        middleLayer.remove(player); //Player Layer
        GameVariables.setPlayer(player);
        GameVariables.saveLevelState(new LevelState(levelNameToSave, GameVariables.gameDateTime().getDays(), borders, activeSpritesLayer, passiveSpritesLayer, bottomLayer, middleLayer, upperLayer, shadowColor, spawnPointsMap));
    }

    public void loadStage(String levelName, String spawnId)
    {
        String methodName = "loadStage() ";

        clearLevel();
        this.levelName = levelName;
        //check if level was already loaded today
        LevelState levelState = GameVariables.getLevelData().get(this.levelName);
        if (levelState != null && levelState.getDay() == GameVariables.gameDateTime().getDays())//Level was loaded on same day
            loadFromLevelDailyState(levelState, spawnId);
        else if (levelState != null)//Level was already loaded on another day
        {
            System.out.println(CLASSNAME + methodName + "loaded persistent state");
            loadLevelFromPersistentState(levelState, spawnId);
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
        passiveSpritesLayer = new ArrayList<>();
        activeSpritesLayer = new ArrayList<>();
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
        List<Sprite> tmp_activeSpritesLayer = worldLoader.getActiveLayer();
        List<Sprite> tmp_bottomLayer = worldLoader.getBttmLayer();
        List<Sprite> tmp_middleLayer = worldLoader.getMediumLayer();
        List<Sprite> tmp_upperLayer = worldLoader.getUpperLayer();
        List<Sprite> tmp_topLayer = worldLoader.getTopLayer();

        //remove persistent actors, just not persistent should remain or actorless sprites
        tmp_activeSpritesLayer = tmp_activeSpritesLayer.stream()
                .filter(sprite ->
                        sprite.getActor() == null || !sprite.getActor().tags.contains(ActorTag.PERSISTENT))
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

        //add persistent actors from state
        for (Sprite activeSprite : levelState.getActiveSpritesLayer()) {
            if (activeSprite.getActor().tags.contains(ActorTag.PERSISTENT)) {
                System.out.println(CLASSNAME + methodName + activeSprite.getActor().getActorInGameName());
                tmp_activeSpritesLayer.add(activeSprite);
                switch (activeSprite.getLayer()) {
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
        activeSpritesLayer = tmp_activeSpritesLayer;
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
        activeSpritesLayer.add(player);

        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        passiveCollisionRelevantSpritesLayer.addAll(topLayer);
        borders = worldLoader.getBorders();
        shadowColor = worldLoader.getShadowColor();
        spawnPointsMap = worldLoader.getSpawnPointsMap();
    }

    private void loadLevelFromFile(String spawnId)
    {
        WorldLoader worldLoader = new WorldLoader();
        worldLoader.load(levelName, spawnId);
        player = worldLoader.getPlayer();
        passiveSpritesLayer = worldLoader.getPassivLayer(); //No collision just render
        activeSpritesLayer = worldLoader.getActiveLayer();
        bottomLayer = worldLoader.getBttmLayer(); //Render height
        middleLayer = worldLoader.getMediumLayer();
        upperLayer = worldLoader.getUpperLayer();
        topLayer = worldLoader.getTopLayer();
        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        passiveCollisionRelevantSpritesLayer.addAll(topLayer);
        borders = worldLoader.getBorders();
        setShadowColor(worldLoader.getShadowColor());
        spawnPointsMap = worldLoader.getSpawnPointsMap();
    }

    private void loadFromLevelDailyState(LevelState levelState, String spawnId)
    {
        String methodName = "loadFromLevelDailyState() ";
        passiveSpritesLayer = levelState.getPassiveSpritesLayer(); //No collision just render
        activeSpritesLayer = levelState.getActiveSpritesLayer();
        bottomLayer = levelState.getBottomLayer(); //Render height
        middleLayer = levelState.getMiddleLayer();
        upperLayer = levelState.getTopLayer();
        borders = levelState.getBorders();
        shadowColor = levelState.getShadowColor();
        spawnPointsMap = levelState.getSpawnPointsMap();

        //Player
        player = GameVariables.getPlayer();
        WorldLoader.SpawnData spawnData = levelState.getSpawnPointsMap().get(spawnId);
        player.getActor().setDirection(spawnData.getDirection());
        player.setPosition(spawnData.getX() * 64, spawnData.getY() * 64);
        middleLayer.add(player); //assumption player on layer 1
        activeSpritesLayer.add(player);

        passiveCollisionRelevantSpritesLayer.addAll(bottomLayer); //For passive collision check
        passiveCollisionRelevantSpritesLayer.addAll(middleLayer);
        passiveCollisionRelevantSpritesLayer.addAll(upperLayer);
        System.out.println(CLASSNAME + methodName);
    }


    public void update(Long currentNanoTime)
    {
        String methodName = "update(Long) ";
        long updateStartTime = System.nanoTime();
        ArrayList<String> input = GameWindow.getInput();
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastTimeMenuWasOpened) / 1000000000.0;

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
            lastTimeMenuWasOpened = currentNanoTime;
        }


        //Process Input
        if (WorldViewController.getWorldViewStatus() != WORLD && player.getActor().isMoving())
            player.getActor().setVelocity(0, 0);
        switch (WorldViewController.getWorldViewStatus())
        {
            case WORLD:
                processInputAsMovement(input, currentNanoTime);
                break;
            case TEXTBOX:
                textbox.processKey(input, currentNanoTime);
                break;
            case PERSONALITY:
                personalityScreenController.processKey(input, currentNanoTime);
                break;
            case DAY_SUMMARY:
                daySummaryScreenController.processKey(input, currentNanoTime);
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
                if (input.contains(KEYBOARD_INVENTORY) || input.contains(KEYBOARD_INTERACT) || input.contains(KEYBOARD_ESCAPE))
                    toggleInventory(currentNanoTime);
                break;
            case COIN_GAME://No keyboard input so far
                break;
            default:
                System.out.println(CLASSNAME + methodName + "Undefined WorldViewStatus: " + WorldViewController.getWorldViewStatus());
        }

        processMouse(currentNanoTime);

        long inputStartTime = System.nanoTime();
        //Update Sprites
        player.update(currentNanoTime);
        for (Sprite active : activeSpritesLayer)
            active.update(currentNanoTime);
        long spritesStartTime = System.nanoTime();
        //Remove Sprites
        for (Sprite sprite : toRemove)
        {
            WorldView.bottomLayer.remove(sprite);
            WorldView.middleLayer.remove(sprite);
            WorldView.upperLayer.remove(sprite);
            WorldView.activeSpritesLayer.remove(sprite);
            WorldView.passiveSpritesLayer.remove(sprite);
            WorldView.passiveCollisionRelevantSpritesLayer.remove(sprite);
        }
        toRemove.clear();

        calcCameraPosition();

        GameVariables.getClock().tryIncrementTime(currentNanoTime);
        GameVariables.updateHunger(currentNanoTime);

        long ClockHungerStartTime = System.nanoTime();
        long timeInput = inputStartTime - updateStartTime;
        long timeSprites = spritesStartTime - updateStartTime - timeInput;
        long hudTime = ClockHungerStartTime - updateStartTime - timeInput - timeSprites;
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

        //TODO update Actors
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

    public static void startConversation(String dialogueFile, String dialogueId, Long currentNanoTime)
    {
        textbox.startConversation(dialogueFile, dialogueId);
        WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
        player.getActor().setLastInteraction(currentNanoTime);
    }

    private void processMouse(Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        double screenWidth = GameWindow.getSingleton().getScreenWidth();
        double screenHeight = GameWindow.getSingleton().getScreenHeight();
        Point2D mousePosition = GameWindow.getSingleton().getMousePosition();
        Point2D mousePositionRelativeToCamera = new Point2D(mousePosition.getX() - (screenWidth - Config.CAMERA_WIDTH) / 2, mousePosition.getY() - (screenHeight - Config.CAMERA_HEIGHT) / 2);
        boolean isMouseClicked = GameWindow.getSingleton().isMouseClicked();

        Set<Sprite> mouseHoveredSprites = new HashSet<>();
        for (Sprite blocker : passiveCollisionRelevantSpritesLayer)
            if (blocker.intersectsRelativeToWorldView(mousePositionRelativeToCamera))
                mouseHoveredSprites.add(blocker);

        switch (WorldViewController.getWorldViewStatus())
        {
            case WORLD:
                for (Sprite clicked : mouseHoveredSprites)
                    if (isMouseClicked)
                        clicked.onClick(currentNanoTime);//Wraps onInteraction
                break;
            case COIN_GAME:
                coinGame.processMouse(mousePositionRelativeToCamera, isMouseClicked, currentNanoTime);
                break;
            case DAY_SUMMARY:
                daySummaryScreenController.processMouse(mousePositionRelativeToCamera, isMouseClicked, currentNanoTime);
                break;
            case PERSONALITY:
                personalityScreenController.processMouse(mousePositionRelativeToCamera, isMouseClicked, currentNanoTime);
                break;
            case TEXTBOX:
                textbox.processMouse(mousePositionRelativeToCamera, isMouseClicked);
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
                inventoryController.processMouse(mousePositionRelativeToCamera, isMouseClicked, currentNanoTime);
                break;
            default:
                System.out.println(CLASSNAME + methodName + "mouseinput undefined for: " + WorldViewController.getWorldViewStatus());

        }

        for (Sprite active : activeSpritesLayer)
            if (active.intersectsRelativeToWorldView(mousePositionRelativeToCamera) && DEBUG_MOUSE_ANALYSIS && active.getActor() != null && isMouseClicked)
            {
                Actor actor = active.getActor();
                System.out.println(actor.getActorInGameName() + ": " + actor.getSensorStatus().getStatusName() + " Sprite: " + actor.getGeneralStatus());
            }
        Point2D mouseWorldPosition = new Point2D(mousePositionRelativeToCamera.getX() + camX, mousePositionRelativeToCamera.getY() + camY);
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

        //Passiv Layer
        for (Sprite sprite : passiveSpritesLayer)
        {
            sprite.render(gc, currentNanoTime);
        }
        //Bottom heightLayer
        bottomLayer.sort(new SpriteComparator());
        for (Sprite sprite : bottomLayer) {
            sprite.render(gc, currentNanoTime);
        }
        //Middle Layer
        middleLayer.sort(new SpriteComparator());
        for (Sprite sprite : middleLayer) {
            sprite.render(gc, currentNanoTime);
        }
        //Upper Layer
        upperLayer.sort(new SpriteComparator());
        for (Sprite sprite : upperLayer) {
            sprite.render(gc, currentNanoTime);
        }
        //Top Layer
        topLayer.sort(new SpriteComparator());
        for (Sprite sprite : topLayer) {
            sprite.render(gc, currentNanoTime);
        }

        //Overlays
        hudCanvas.getGraphicsContext2D().clearRect(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        renderHUD(currentNanoTime);
        switch (WorldViewController.getWorldViewStatus()) {

            case WORLD:
                break;
            case TEXTBOX:
                textbox.render(hudCanvas.getGraphicsContext2D());
                break;
            case INVENTORY:
            case INVENTORY_EXCHANGE:
            case INVENTORY_SHOP:
                WritableImage inventoryOverlayMenuImage = inventoryController.getMenuImage();
                hudCanvas.getGraphicsContext2D().drawImage(inventoryOverlayMenuImage, inventoryOverlayPosition.getX(), inventoryOverlayPosition.getY());
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
        root.getChildren().add(hudCanvas);

        gc.translate(camX, camY);

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
        boardTimeOverlay.render(hudCanvas.getGraphicsContext2D());
        if (newMessageOverlay.isVisible())
            newMessageOverlay.render(hudCanvas.getGraphicsContext2D(), currentNanoTime);
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

    public Actor getSpriteByName(String id)
    {
        String methodName = "getSpriteByName() ";
        for (Sprite sprite : activeSpritesLayer)
            if (sprite.getActor().getActorId().equals(id))
                return sprite.getActor();
        System.out.println(CLASSNAME + methodName + "No Actor found with ID: " + id);
        return null;
    }

    public Pane getRoot()
    {
        return root;
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

    public static Point2D getInventoryOverlayPosition()
    {
        return inventoryOverlayPosition;
    }

    public static Point2D getTextBoxPosition()
    {
        return textBoxPosition;
    }

    public static Point2D getPersonalityScreenPosition()
    {
        return personalityScreenPosition;
    }

    public static void setPersonalityScreenController(PersonalityScreenController personalityScreenController)
    {
        WorldView.personalityScreenController = personalityScreenController;
    }

    public static void setDiscussionGame(CoinGame coinArea)
    {
        WorldView.coinGame = coinArea;
    }

    public static Textbox getTextbox()
    {
        return textbox;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public Canvas getWorldCanvas()
    {
        return worldCanvas;
    }

    public GraphicsContext getGc()
    {
        return gc;
    }

    public Canvas getShadowMask()
    {
        return shadowMask;
    }

    public GraphicsContext getShadowMaskGc()
    {
        return shadowMaskGc;
    }

    public Map<String, Image> getLightsImageMap()
    {
        return lightsImageMap;
    }

    public Color getShadowColor()
    {
        return shadowColor;
    }


    public static Long getLastTimeMenuWasOpened()
    {
        return lastTimeMenuWasOpened;
    }


    public static PersonalityScreenController getPersonalityScreenController()
    {
        return personalityScreenController;
    }


    public static CoinGame getDiscussionGame()
    {
        return coinGame;
    }

    public static Point2D getDiscussionGamePosition()
    {
        return discussionGamePosition;
    }


    public static DaySummaryScreenController getDaySummaryScreenController()
    {
        return daySummaryScreenController;
    }

    public static Point2D getDaySummaryScreenPosition()
    {
        return daySummaryScreenPosition;
    }


    public static BarStatusOverlay getMamOverlay()
    {
        return mamOverlay;
    }

    public static Point2D getMamOverlayPosition()
    {
        return mamOverlayPosition;
    }

    public static List<Sprite> getActiveSpritesLayer()
    {
        return activeSpritesLayer;
    }

    public static List<Sprite> getPassiveSpritesLayer()
    {
        return passiveSpritesLayer;
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

    public double getOffsetMaxX()
    {
        return offsetMaxX;
    }

    public double getOffsetMaxY()
    {
        return offsetMaxY;
    }

    public int getOffsetMinX()
    {
        return offsetMinX;
    }

    public int getOffsetMinY()
    {
        return offsetMinY;
    }

    public static double getCamX()
    {
        return camX;
    }

    public static double getCamY()
    {
        return camY;
    }

    public double getBumpX()
    {
        return bumpX;
    }

    public double getBumpY()
    {
        return bumpY;
    }

    public double getRumbleGrade()
    {
        return rumbleGrade;
    }

    public Long getTimeStartBump()
    {
        return timeStartBump;
    }

    public float getDurationBump()
    {
        return durationBump;
    }

    public boolean isBumpActive()
    {
        return bumpActive;
    }

    public void setBumpActive(boolean bumpActive)
    {
        this.bumpActive = bumpActive;
    }

    public void setShadowColor(Color shadowColor)
    {
        this.shadowColor = shadowColor;
    }
}
