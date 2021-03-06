package Core.Menus.CoinGame;

import Core.Actor;
import Core.GameWindow;
import Core.Menus.Personality.PersonalityContainer;
import Core.Utilities;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.TextAlignment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Menus.CoinGame.CharacterCoinBuff.BUFF_DOUBLE_REWARD;
import static Core.Menus.Personality.MachineTrait.*;
import static Core.Menus.Personality.PersonalityTrait.*;
import static Core.Utilities.doCircleOverlap;
import static javafx.scene.paint.Color.BLACK;

public class CoinArea
{
    private static final String CLASSNAME = "DiscussionGame/";
    private static final int HEIGHT = COIN_AREA_HEIGHT;
    private static final int WIDTH = COIN_AREA_WIDTH;
    private static final Point2D SCREEN_POSITION = new Point2D(COINGAME_POSITION.getX() + COIN_AREA_WIDTH_OFFSET, COINGAME_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET);
    private static Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    Image cornerTopLeft, cornerBtmRight;
    Point2D mousePosRelativeToDiscussionOverlay;
    List<Shape> shapeList = new ArrayList<>();
    List<CharacterCoin> coinsList = new ArrayList<>();
    List<CharacterCoin> visibleCoinsList = new ArrayList<>();
    List<CharacterCoin> removedCoinsList = new ArrayList<>();
    Element xmlRoot;
    Long gameStartTime;
    boolean isFinished = false;
    boolean isWon = false;
    long isFinishedTime = 0;
    Map<CoinType, Integer> clickedCoins = new HashMap<>();
    String gameFileName;
    Actor actorOfDiscussion;
    private int totalResult;
    private int motivationResult;
    private int focusResult;
    private int decisionResult;
    private int lifestyleResult;
    private int machineCompute, machineManagement, machineInterface, machineNetwork;
    private static Circle mouseClickSpace = new Circle(WIDTH / 2f, HEIGHT / 2f, 15);
    private Map<String, CharacterCoinBuff> activeBuffs = new HashMap<>();
    private int winThreshold = DISCUSSION_DEFAULT_THRESHOLD_WIN;
    private int maxPossiblePoints = 0;
    private float percentageOfPointsToWin = 0.5f;
    private int maxGameTime = 0;
    private Image backgroundImage;

    public CoinArea(String gameIdentifier, Actor actorOfDiscussion)
    {
        gameFileName = gameIdentifier;
        this.actorOfDiscussion = actorOfDiscussion;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        backgroundImage = Utilities.readImage("discussions/img/background/nerve.png");
        init();
    }

    private void init()
    {
        loadDiscussion();
        gameStartTime = GameWindow.getSingleton().getRenderTime();
        actorOfDiscussion.getPersonalityContainer().increaseCooperation(2);
    }

    private void loadDiscussion()
    {
        String methodName = "loadDiscussion() ";
        xmlRoot = Utilities.readXMLFile(COINGAME_DIRECTORY_PATH + gameFileName);
        NodeList coins = xmlRoot.getElementsByTagName("coin");
        for (int i = 0; i < coins.getLength(); i++) //iterate coins of file
        {
            Element currentCoin = ((Element) coins.item(i));
            CharacterCoin characterCoin = new CharacterCoin(currentCoin);
            coinsList.add(characterCoin);
            if (actorOfDiscussion.getPersonalityContainer().isPersonalityMatch(characterCoin.type))
                maxPossiblePoints++;
            maxGameTime = Math.max(characterCoin.time_spawn + characterCoin.time_max, maxGameTime);
        }
        if (xmlRoot.hasAttribute(DISCUSSION_ATTRIBUTE_PERCENTAGE_OF_POINTS_TO_WIN))
            percentageOfPointsToWin = Float.parseFloat(xmlRoot.getAttribute(DISCUSSION_ATTRIBUTE_PERCENTAGE_OF_POINTS_TO_WIN));
        winThreshold = (int) (maxPossiblePoints * percentageOfPointsToWin);
        activeBuffs.clear();
    }

    public void update(Long currentNanoTime)
    {
        String methodName = "update() ";
        double elapsedTime = (currentNanoTime - gameStartTime) / 1000000000.0;
        visibleCoinsList.clear();
        for (CharacterCoin coin : coinsList)
        {
            if (coin.time_spawn <= elapsedTime && !removedCoinsList.contains(coin))
            {
                visibleCoinsList.add(coin);
            }
        }

        //For all Coins
        for (int i = 0; i < visibleCoinsList.size(); i++)
        {
            CharacterCoin coin = visibleCoinsList.get(i);
            coin.move(currentNanoTime - gameStartTime);
            Circle circle = coin.collisionCircle;
            double elapsedTimeSinceSpawn = ((currentNanoTime - gameStartTime) / 1000000000.0) - coin.time_spawn;

            if(circle.getCenterX() > 954)
                System.out.println(coin.time_spawn + ": " + circle.getCenterX());
            //Check if is visible
            if (!SCREEN_AREA.contains(new Point2D(SCREEN_POSITION.getX() + circle.getCenterX(), SCREEN_POSITION.getY() +circle.getCenterY()))
                   || elapsedTimeSinceSpawn > coin.time_max
            )
            {
                removedCoinsList.add(coin);
            }
        }

        List<String> removeList = new ArrayList<>();
        for (Map.Entry<String, CharacterCoinBuff> buff : activeBuffs.entrySet())
        {
            double elapsedTimeBuff = (currentNanoTime - buff.getValue().activeSince) / 1000000000.0;
            if (buff.getValue().duration <= elapsedTimeBuff)
                removeList.add(buff.getKey());
        }
        removeList.forEach(key ->
        {
            activeBuffs.remove(key);
        });

        if (removedCoinsList.size() == coinsList.size() && !isFinished)
        {
            //Get number of clicked coins
            PersonalityContainer personality = actorOfDiscussion.getPersonalityContainer();
            Integer extroversion = clickedCoins.get(EXTROVERSION) == null ? 0 : clickedCoins.get(EXTROVERSION);
            Integer introversion = clickedCoins.get(INTROVERSION) == null ? 0 : clickedCoins.get(INTROVERSION);
            Integer sensing = clickedCoins.get(SENSING) == null ? 0 : clickedCoins.get(SENSING);
            Integer intuition = clickedCoins.get(INTUITION) == null ? 0 : clickedCoins.get(INTUITION);
            Integer thinking = clickedCoins.get(THINKING) == null ? 0 : clickedCoins.get(THINKING);
            Integer feeling = clickedCoins.get(FEELING) == null ? 0 : clickedCoins.get(FEELING);
            Integer judging = clickedCoins.get(JUDGING) == null ? 0 : clickedCoins.get(JUDGING);
            Integer perceiving = clickedCoins.get(PERCEIVING) == null ? 0 : clickedCoins.get(PERCEIVING);
            Integer machineComputeLocal = clickedCoins.get(COMPUTE_LOCAL) == null ? 0 : clickedCoins.get(COMPUTE_LOCAL);
            Integer machineComputeCloud = clickedCoins.get(COMPUTE_CLOUD) == null ? 0 : clickedCoins.get(COMPUTE_CLOUD);
            Integer machineComputeVirtual = clickedCoins.get(COMPUTE_VIRTUAL) == null ? 0 : clickedCoins.get(COMPUTE_VIRTUAL);
            Integer machineManagementDebug = clickedCoins.get(MANAGEMENT_DEBUG) == null ? 0 : clickedCoins.get(MANAGEMENT_DEBUG);
            Integer machineManagementLogging = clickedCoins.get(MANAGEMENT_LOGGING) == null ? 0 : clickedCoins.get(MANAGEMENT_LOGGING);
            Integer machineManagementMonitoring = clickedCoins.get(MANAGEMENT_MONITORING) == null ? 0 : clickedCoins.get(MANAGEMENT_MONITORING);
            Integer machineInterfaceDirect = clickedCoins.get(INTERFACE_DIRECT) == null ? 0 : clickedCoins.get(INTERFACE_DIRECT);
            Integer machineInterfaceAnalog = clickedCoins.get(INTERFACE_ANALOG) == null ? 0 : clickedCoins.get(INTERFACE_ANALOG);
            Integer machineInterfaceConnection = clickedCoins.get(INTERFACE_CONNECTION) == null ? 0 : clickedCoins.get(INTERFACE_CONNECTION);
            Integer machineNetworkClient = clickedCoins.get(NETWORK_CLIENT) == null ? 0 : clickedCoins.get(NETWORK_CLIENT);
            Integer machineNetworkServer = clickedCoins.get(NETWORK_SERVER) == null ? 0 : clickedCoins.get(NETWORK_SERVER);
            Integer machineNetworkUnconnected = clickedCoins.get(NETWORK_UNCONNECTED) == null ? 0 : clickedCoins.get(NETWORK_UNCONNECTED);

            //Sum coins against traits
            motivationResult = 0;
            motivationResult += personality.getTraits().contains(EXTROVERSION) ? extroversion : -extroversion;
            motivationResult += personality.getTraits().contains(INTROVERSION) ? introversion : -introversion;
            focusResult = 0;
            focusResult += personality.getTraits().contains(SENSING) ? sensing : -sensing;
            focusResult += personality.getTraits().contains(INTUITION) ? intuition : -intuition;
            decisionResult = 0;
            decisionResult += personality.getTraits().contains(THINKING) ? thinking : -thinking;
            decisionResult += personality.getTraits().contains(FEELING) ? feeling : -feeling;
            lifestyleResult = 0;
            lifestyleResult += personality.getTraits().contains(PERCEIVING) ? perceiving : -perceiving;
            lifestyleResult += personality.getTraits().contains(JUDGING) ? judging : -judging;
            machineCompute = 0;
            machineCompute += personality.getTraits().contains(COMPUTE_LOCAL) ? machineComputeLocal : -machineComputeLocal;
            machineCompute += personality.getTraits().contains(COMPUTE_CLOUD) ? machineComputeCloud : -machineComputeCloud;
            machineCompute += personality.getTraits().contains(COMPUTE_VIRTUAL) ? machineComputeVirtual : -machineComputeVirtual;
            machineManagement = 0;
            machineManagement += personality.getTraits().contains(MANAGEMENT_DEBUG) ? machineManagementDebug : -machineManagementDebug;
            machineManagement += personality.getTraits().contains(MANAGEMENT_LOGGING) ? machineManagementLogging : -machineManagementLogging;
            machineManagement += personality.getTraits().contains(MANAGEMENT_MONITORING) ? machineManagementMonitoring : -machineManagementMonitoring;
            machineInterface = 0;
            machineInterface += personality.getTraits().contains(INTERFACE_CONNECTION) ? machineInterfaceConnection : -machineInterfaceConnection;
            machineInterface += personality.getTraits().contains(INTERFACE_ANALOG) ? machineInterfaceAnalog : -machineInterfaceAnalog;
            machineInterface += personality.getTraits().contains(INTERFACE_DIRECT) ? machineInterfaceDirect : -machineInterfaceDirect;
            machineNetwork = 0;
            machineNetwork += personality.getTraits().contains(NETWORK_CLIENT) ? machineNetworkClient : -machineNetworkClient;
            machineNetwork += personality.getTraits().contains(NETWORK_SERVER) ? machineNetworkServer : -machineNetworkServer;
            machineNetwork += personality.getTraits().contains(NETWORK_UNCONNECTED) ? machineNetworkUnconnected : -machineNetworkUnconnected;

            totalResult = motivationResult + focusResult + decisionResult + lifestyleResult + machineCompute + machineManagement + machineInterface + machineNetwork;
            if (totalResult >= winThreshold)
            {
                WorldView.getTextbox().setNextDialogueFromDiscussionResult(true);
                isWon = true;
            }
            else
                WorldView.getTextbox().setNextDialogueFromDiscussionResult(false);
            isFinished = true;
            isFinishedTime = currentNanoTime;
        }
    }

    public void render(GraphicsContext gc, Long currentNanoTime)
    {
        String methodName = "render() ";
        Color font = COLOR_FONT;

        //Background
        gc.setGlobalAlpha(0.45);
        gc.drawImage(backgroundImage,
                SCREEN_POSITION.getX(),
                SCREEN_POSITION.getY());
        gc.setGlobalAlpha(0.7);
        gc.setFill(BLACK);
        gc.fillRect(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);



        gc.setStroke(font);
        int xInterval = 50, yInterval = 50;
        for (int x = 0; x <= WIDTH; x += xInterval)
            gc.strokeLine(SCREEN_POSITION.getX() + x, SCREEN_POSITION.getY(), SCREEN_POSITION.getX() + x, SCREEN_POSITION.getY() + HEIGHT);
        for (int y = 0; y <= HEIGHT; y += yInterval)
            gc.strokeLine(SCREEN_POSITION.getX(), SCREEN_POSITION.getY() + y, SCREEN_POSITION.getX() + WIDTH, SCREEN_POSITION.getY() + y);

        gc.setStroke(Color.RED);
        gc.strokeRect(SCREEN_AREA.getMinX(), SCREEN_AREA.getMinY(), SCREEN_AREA.getWidth(), SCREEN_AREA.getHeight());

        gc.setGlobalAlpha(1);
        for (int i = 0; i < visibleCoinsList.size(); i++)
        {
            CharacterCoin coin = visibleCoinsList.get(i);
            Circle circle = coin.collisionCircle;
            shapeList.add(circle);
            gc.drawImage(coin.image, SCREEN_POSITION.getX() + circle.getCenterX() - circle.getRadius(), SCREEN_POSITION.getY() + circle.getCenterY() - circle.getRadius());
        }
        gc.setFill(COLOR_MARKING);
        gc.fillOval(SCREEN_POSITION.getX() + mouseClickSpace.getCenterX() - mouseClickSpace.getRadius(), SCREEN_POSITION.getY() + mouseClickSpace.getCenterY() - mouseClickSpace.getRadius(), mouseClickSpace.getRadius() * 2, mouseClickSpace.getRadius() * 2);

        if (isFinished)
        {
            gc.setFont(FONT_ESTROG_30_DEFAULT);
            gc.setFill(font);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            float achievedPercentageOfMinWinThreshold = (float) totalResult / winThreshold;
            String hintMsg;
            if (achievedPercentageOfMinWinThreshold >= 0.7)
                hintMsg = "Close to success..";
            else if (achievedPercentageOfMinWinThreshold >= 0.5)
                hintMsg = "This went ok, maybe next time..";
            else
                hintMsg = "This went wrong..";

            if (totalResult >= winThreshold)
                gc.fillText("Success!", SCREEN_POSITION.getX() + WIDTH / 2.0, SCREEN_POSITION.getY() + HEIGHT / 2.0 + gc.getFont().getSize());
            else
            {
                gc.fillText(hintMsg, SCREEN_POSITION.getX() + WIDTH / 2.0, SCREEN_POSITION.getY() + HEIGHT / 2.0 + gc.getFont().getSize());
                gc.fillText("Click here to restart!", SCREEN_POSITION.getX() + WIDTH / 2.0, SCREEN_POSITION.getY() + HEIGHT / 2.0 + gc.getFont().getSize() * 2 + 10);
            }

        }

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        Point2D overlayPosition = SCREEN_POSITION;
        Rectangle2D posRelativeToWorldview = new Rectangle2D(overlayPosition.getX(), overlayPosition.getY(), WIDTH, HEIGHT);
        List<CharacterCoin> hoveredElements = new ArrayList<>();
        boolean isMouseDragged = GameWindow.getSingleton().isMouseDragged();

        //Calculate Mouse Position relative to Discussion
        if (posRelativeToWorldview.contains(mousePosition))
        {
            mousePosRelativeToDiscussionOverlay = new Point2D(mousePosition.getX() - overlayPosition.getX(), mousePosition.getY() - overlayPosition.getY());
            mouseClickSpace.setCenterX(mousePosRelativeToDiscussionOverlay.getX());
            mouseClickSpace.setCenterY(mousePosRelativeToDiscussionOverlay.getY());
        }

        //Check for hovered elements
        if (isMouseClicked || isMouseDragged)
            for (int i = 0; i < visibleCoinsList.size(); i++)
            {
                Circle circle = visibleCoinsList.get(i).collisionCircle;
                if (doCircleOverlap(circle, mouseClickSpace))
                {
                    hoveredElements.add(visibleCoinsList.get(i));
                }
            }

        if (GameWindow.getSingleton().isMouseMoved() && !hoveredElements.isEmpty())//Set highlight if mouse moved
        {
            GameWindow.getSingleton().setMouseMoved(false);
        }

        //Process click
        if ((isMouseClicked || isMouseDragged) && !hoveredElements.isEmpty())
        {
            for (int i = 0; i < hoveredElements.size(); i++)
            {
                Circle circle = hoveredElements.get(i).collisionCircle;
                countClickedCoinTypes(hoveredElements.get(i), currentNanoTime);
                shapeList.remove(circle);
                removedCoinsList.add(hoveredElements.get(i));
            }
        }
    }

    private void countClickedCoinTypes(CharacterCoin coin, Long currentNanoTime)
    {
        String methodName = "countClickedCoinTypes() ";
        boolean debug = false;
        if (coin.type instanceof CharacterCoinBuff)
        {
            ((CharacterCoinBuff) coin.type).activeSince = currentNanoTime;
            activeBuffs.put(coin.type.toString(), (CharacterCoinBuff) coin.type);
            if (debug) System.out.println(CLASSNAME + methodName + "added buff: " + coin.type.toString());
        }

        int buffCoinMultiplicand;
        if (getActiveBuffs().containsKey(BUFF_DOUBLE_REWARD.toString()))
            buffCoinMultiplicand = 2;
        else
            buffCoinMultiplicand = 1;

        if (!clickedCoins.containsKey(coin.type))
            clickedCoins.put(coin.type, 0);
        if (debug) System.out.println(CLASSNAME + methodName + buffCoinMultiplicand + " " + activeBuffs.toString());
        clickedCoins.put(coin.type, (clickedCoins.get(coin.type) + buffCoinMultiplicand));
    }

    public Map<String, CharacterCoinBuff> getActiveBuffs()
    {
        return activeBuffs;
    }

    public int getMaxGameTime()
    {
        return maxGameTime;
    }

    public static Rectangle2D getScreenArea()
    {
        return SCREEN_AREA;
    }

    public void setMaxGameTime(int maxGameTime)
    {
        this.maxGameTime = maxGameTime;
    }
}
