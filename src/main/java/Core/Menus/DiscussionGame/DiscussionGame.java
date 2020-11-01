package Core.Menus.DiscussionGame;

import Core.Actor;
import Core.GameWindow;
import Core.Menus.Personality.MachineTrait;
import Core.Menus.Personality.PersonalityContainer;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Menus.DiscussionGame.CharacterCoinBuff.BUFF_DOUBLE_REWARD;
import static Core.Menus.Personality.MachineTrait.*;
import static Core.Menus.Personality.PersonalityTrait.*;
import static Core.Utilities.doCircleOverlap;

public class DiscussionGame
{
    private static final String CLASSNAME = "DiscussionGame/";
    private static final int HEIGHT = DISCUSSION_HEIGHT;
    private static final int WIDTH = DISCUSSION_WIDTH;
    private static final Point2D SCREEN_POSITION = DISCUSSION_POSITION;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private WritableImage writableImage;
    Point2D mousePosRelativeToDiscussionOverlay;
    List<Shape> shapeList = new ArrayList<>();
    List<CharacterCoin> coinsList = new ArrayList<>();
    List<CharacterCoin> visibleCoinsList = new ArrayList<>();
    List<CharacterCoin> removedCoinsList = new ArrayList<>();
    Element xmlRoot;
    Long gameStartTime;
    boolean isFinished = false;
    Map<CoinType, Integer> clickedCoins = new HashMap<>();
    String gameFileName;
    Actor actorOfDiscussion;
    private int totalResult;
    private int motivationResult;
    private int focusResult;
    private int decisionResult;
    private int lifestyleResult;
    private int machineCompute;
    private static Circle mouseClickSpace = new Circle(WIDTH / 2f, HEIGHT / 2f, 15);
    private static Map<String, CharacterCoinBuff> activeBuffs = new HashMap<>();
    private int winThreshold = DISCUSSION_DEFAULT_THRESHOLD_WIN;
    private int maxPossiblePoints = 0;
    private float percentageOfPointsToWin = 0.5f;

    public DiscussionGame(String gameIdentifier, Actor actorOfDiscussion)
    {
        gameFileName = gameIdentifier;
        this.actorOfDiscussion = actorOfDiscussion;
        init();
    }

    private void init()
    {
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        loadDiscussion();
        gameStartTime = GameWindow.getSingleton().getRenderTime();
        actorOfDiscussion.getPersonalityContainer().increaseCooperation(2);
    }

    private void loadDiscussion()
    {
        String methodName = "loadDiscussion() ";
        xmlRoot = Utilities.readXMLFile(COINGAME_DIRECTORY_PATH + gameFileName + ".xml");
        NodeList coins = xmlRoot.getElementsByTagName("coin");
        for (int i = 0; i < coins.getLength(); i++) //iterate coins of file
        {
            Element currentCoin = ((Element) coins.item(i));
            CharacterCoin characterCoin = new CharacterCoin(currentCoin);
            coinsList.add(characterCoin);
            if (actorOfDiscussion.getPersonalityContainer().isPersonalityMatch(characterCoin.type))
                maxPossiblePoints++;
        }
        if(xmlRoot.hasAttribute(DISCUSSION_ATTRIBUTE_PERCENTAGE_OF_POINTS_TO_WIN))
            percentageOfPointsToWin = Float.parseFloat(xmlRoot.getAttribute(DISCUSSION_ATTRIBUTE_PERCENTAGE_OF_POINTS_TO_WIN));
        winThreshold = (int)(maxPossiblePoints * percentageOfPointsToWin);
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
            Circle circle = coin.collisionCircle;
            double elapsedTimeSinceSpawn = ((currentNanoTime - gameStartTime) / 1000000000.0) - coin.time_spawn;
            coin.move(currentNanoTime - gameStartTime);

            //Check if is visible
            if (!new Rectangle2D(0, 0, canvas.getWidth(), canvas.getHeight()).
                    intersects(circle.getCenterX() - circle.getRadius(), circle.getCenterY() - circle.getRadius(), circle.getCenterX() + circle.getRadius(), circle.getCenterY() + circle.getRadius())
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

        if (removedCoinsList.size() == coinsList.size())
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

            //Sum coins against traits
            motivationResult = 0;
            motivationResult += personality.getTraitsV2().contains(EXTROVERSION) ? extroversion : -extroversion;
            motivationResult += personality.getTraitsV2().contains(INTROVERSION) ? introversion : -introversion;
            focusResult = 0;
            focusResult += personality.getTraitsV2().contains(SENSING) ? sensing : -sensing;
            focusResult += personality.getTraitsV2().contains(INTUITION) ? intuition : -intuition;
            decisionResult = 0;
            decisionResult += personality.getTraitsV2().contains(THINKING) ? thinking : -thinking;
            decisionResult += personality.getTraitsV2().contains(FEELING) ? feeling : -feeling;
            lifestyleResult = 0;
            lifestyleResult += personality.getTraitsV2().contains(PERCEIVING) ? perceiving : -perceiving;
            lifestyleResult += personality.getTraitsV2().contains(JUDGING) ? judging : -judging;
            machineCompute = 0;
            machineCompute += personality.getTraitsV2().contains(COMPUTE_LOCAL) ? machineComputeLocal : -machineComputeLocal;
            machineCompute += personality.getTraitsV2().contains(COMPUTE_CLOUD) ? machineComputeCloud : -machineComputeCloud;
            machineCompute += personality.getTraitsV2().contains(COMPUTE_VIRTUAL) ? machineComputeVirtual : -machineComputeVirtual;

            totalResult = motivationResult + focusResult + decisionResult + lifestyleResult + machineCompute;
            isFinished = true;
        }
    }

    private void draw(Long currentNanoTime) throws NullPointerException
    {
        String methodName = "draw() ";
        graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);
        Color background = Color.rgb(60, 90, 85);
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);

        //Background
        graphicsContext.setGlobalAlpha(0.8);
        graphicsContext.setFill(background);
        int backgroundOffsetX = 0, backgroundOffsetY = 0;
        graphicsContext.fillRect(backgroundOffsetX, backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);
        graphicsContext.setGlobalAlpha(1);

        update(currentNanoTime);
        //Draw list of shapes
        graphicsContext.setFill(marking);
        for (int i = 0; i < visibleCoinsList.size(); i++)
        {
            CharacterCoin coin = visibleCoinsList.get(i);
            Circle circle = coin.collisionCircle;
            shapeList.add(circle);
            graphicsContext.drawImage(coin.image, circle.getCenterX() - circle.getRadius(), circle.getCenterY() - circle.getRadius());
        }

        graphicsContext.fillOval(mouseClickSpace.getCenterX() - mouseClickSpace.getRadius(), mouseClickSpace.getCenterY() - mouseClickSpace.getRadius(), mouseClickSpace.getRadius() * 2, mouseClickSpace.getRadius() * 2);

        if (isFinished)
        {
            graphicsContext.setFont(new Font(30));
            graphicsContext.setFill(font);
            graphicsContext.setTextAlign(TextAlignment.CENTER);
            graphicsContext.setTextBaseline(VPos.CENTER);
            String text = "You got motivation: " + motivationResult + " focus: " + focusResult + " \ndecision: " + decisionResult + " lifestyle: " + lifestyleResult + " \nTotal: " + totalResult
                    + "\n MaxPossiblePoints: " + maxPossiblePoints + " WinThreshold: " + winThreshold;
            graphicsContext.fillText(text, WIDTH / 2.0, HEIGHT / 2.0);
            if (totalResult >= winThreshold)
                graphicsContext.fillText("Convinced!", WIDTH / 2.0, HEIGHT / 2.0 + graphicsContext.getFont().getSize() + 40);
            else
                graphicsContext.fillText("Try again!", WIDTH / 2.0, HEIGHT / 2.0 + graphicsContext.getFont().getSize() + 40);
        }

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        writableImage = canvas.snapshot(transparency, null);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        Point2D overlayPosition = SCREEN_POSITION;
        Rectangle2D posRelativeToWorldview = new Rectangle2D(overlayPosition.getX(), overlayPosition.getY(), WIDTH, HEIGHT);
        List<CharacterCoin> hoveredElements = new ArrayList<>();

        //Calculate Mouse Position relative to Discussion
        if (posRelativeToWorldview.contains(mousePosition))
        {
            mousePosRelativeToDiscussionOverlay = new Point2D(mousePosition.getX() - overlayPosition.getX(), mousePosition.getY() - overlayPosition.getY());
            mouseClickSpace.setCenterX(mousePosRelativeToDiscussionOverlay.getX());
            mouseClickSpace.setCenterY(mousePosRelativeToDiscussionOverlay.getY());
        }

        //Check for hovered elements
        if (isMouseClicked)
            for (Integer i = 0; i < visibleCoinsList.size(); i++)
            {
                Circle circle = visibleCoinsList.get(i).collisionCircle;
                if (doCircleOverlap(circle, mouseClickSpace))
                    hoveredElements.add(visibleCoinsList.get(i));
            }

        if (GameWindow.getSingleton().isMouseMoved() && !hoveredElements.isEmpty())//Set highlight if mouse moved
        {
            GameWindow.getSingleton().setMouseMoved(false);
        }

        //Process click
        if (isMouseClicked && !hoveredElements.isEmpty())
        {
            for (int i = 0; i < hoveredElements.size(); i++)
            {
                Circle circle = hoveredElements.get(i).collisionCircle;
                countClickedCoinTypes(hoveredElements.get(i), currentNanoTime);
                shapeList.remove(circle);
                removedCoinsList.add(hoveredElements.get(i));
            }
        }
        else if (isMouseClicked && isFinished)
        {
            //If won discussion
            if (totalResult >= winThreshold)
                WorldView.getTextbox().setNextDialogueFromDiscussionResult(true);
            else
                WorldView.getTextbox().setNextDialogueFromDiscussionResult(false);

            WorldView.getTextbox().nextMessage(currentNanoTime);
            WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
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
        if (DiscussionGame.getActiveBuffs().containsKey(BUFF_DOUBLE_REWARD.toString()))
            buffCoinMultiplicand = 2;
        else
            buffCoinMultiplicand = 1;

        if (!clickedCoins.containsKey(coin.type))
            clickedCoins.put(coin.type, 0);
        if (debug) System.out.println(CLASSNAME + methodName + buffCoinMultiplicand + " " + activeBuffs.toString());
        clickedCoins.put(coin.type, (clickedCoins.get(coin.type) + buffCoinMultiplicand));
    }

    public WritableImage getWritableImage(Long currentNanoTime)
    {
        draw(currentNanoTime);
        return writableImage;
    }

    public static Map<String, CharacterCoinBuff> getActiveBuffs()
    {
        return activeBuffs;
    }
}
