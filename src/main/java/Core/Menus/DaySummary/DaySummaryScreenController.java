package Core.Menus.DaySummary;

import Core.CollectibleStack;
import Core.GameVariables;
import Core.GameWindow;
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
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class DaySummaryScreenController
{
    private static final String CLASSNAME = "DaySummaryScreenController ";
    private static final int WIDTH = DAY_SUMMARY_WIDTH;
    private static final int HEIGHT = DAY_SUMMARY_HEIGHT;
    private static final Point2D SCREEN_POSITION = DAY_SUMMARY_POSITION;
    private static DaySummary daySummary;
    Image cornerTopLeft;
    Image cornerBtmRight;
    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage writableImage;
    private Integer highlightedElement;
    private List<String> interfaceElements_list = new ArrayList<>();
    //MAM Information
    private int mamInfoWidth = 300;
    private int mamInfoHeight = 300;
    private int mamInfo_x = WIDTH - mamInfoWidth - 100;
    private int mamInfo_y = 80;
    Rectangle2D mamInformationArea = new Rectangle2D(mamInfo_x, mamInfo_y, mamInfoWidth, mamInfoHeight);

    //Close Button
    private String CLOSE_BUTTON_ID = "Close";
    private int closeButton_x = 700;
    private int closeButton_y = 500;
    private int closeButton_width = 150;
    private int closeButton_height = 50;
    Rectangle2D closeButton = new Rectangle2D(closeButton_x, closeButton_y, closeButton_width, closeButton_height);

    public DaySummaryScreenController()
    {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        highlightedElement = 0;
        daySummary = new DaySummary();
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");

    }

    public static void newDay()
    {
        daySummary.init();
    }

    public static int getMenuWidth()
    {
        return WIDTH;
    }

    public static int getMenuHeight()
    {
        return HEIGHT;
    }

    private void render() throws NullPointerException
    {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        Color marking = COLOR_MARKING;
        Color font = COLOR_FONT;
        interfaceElements_list.clear();

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(backgroundOffsetX, backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        //MaM message field
        int strokeThickness = 6;
        int round = 15;
        if (daySummary.isHasInterrogation())
            gc.setFill(COLOR_RED);
        else
            gc.setFill(COLOR_GREEN);
        gc.fillRoundRect(mamInformationArea.getMinX() - strokeThickness, mamInformationArea.getMinY() - strokeThickness, mamInformationArea.getWidth() + strokeThickness * 2, mamInformationArea.getHeight() + strokeThickness * 2, round, round);
        gc.setFill(marking);
        gc.fillRoundRect(mamInformationArea.getMinX(), mamInformationArea.getMinY(), mamInformationArea.getWidth(), mamInformationArea.getHeight(), round, round);

        //Text
        int spaceY = 5;
        int initOffsetY = 20;
        int tmpOffsetY = 0;
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setGlobalAlpha(1);
        gc.setFill(font);
        StringBuilder stringBuilder = new StringBuilder();
        if (daySummary.isHasInterrogation())
        {
            stringBuilder.append("You were interrogated.");
            if (!daySummary.foundStolenCollectibles.isEmpty())
                stringBuilder.append(" Some items were confiscated.");
            else
                stringBuilder.append(" They found no stolen items.");
        }
        else
            stringBuilder.append("You had a silent night.");
        gc.fillText(stringBuilder.toString(), mamInformationArea.getMinX() + 5, mamInformationArea.getMinY() + initOffsetY);
        for (int i = 0; i < daySummary.foundStolenCollectibles.size(); i++)
        {
            CollectibleStack collectible = daySummary.foundStolenCollectibles.get(i);
            tmpOffsetY += 20 + i * (gc.getFont().getSize() + spaceY);
            gc.fillText(collectible.getIngameName(), mamInformationArea.getMinX() + 5,
                    mamInformationArea.getMinY() + initOffsetY + tmpOffsetY);
        }


        if (!daySummary.newMails.isEmpty())
            gc.fillText(daySummary.newMails.get(0).getText(), 50, 50);
        else
            gc.fillText("No New Emails", 50, 50);

        //Healt Info
        StringBuilder healthMsg = new StringBuilder();
        if (daySummary.isStarving())
            healthMsg.append("You are starving.");
        else
            healthMsg.append("You are well fed.");
        healthMsg.append(" Health: ").append(GameVariables.getHealth());
        tmpOffsetY += 20 + (gc.getFont().getSize() + spaceY);
        gc.fillText(healthMsg.toString(), mamInformationArea.getMinX() + 5,
                mamInformationArea.getMinY() + initOffsetY + tmpOffsetY);

        //Close button
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(marking);
        interfaceElements_list.add(CLOSE_BUTTON_ID);
        if (highlightedElement == interfaceElements_list.indexOf(CLOSE_BUTTON_ID))
            gc.fillRect(closeButton.getMinX(), closeButton.getMinY(), closeButton.getWidth(), closeButton.getHeight());
        gc.setFill(font);
        gc.fillText(CLOSE_BUTTON_ID, closeButton.getMinX() + closeButton.getWidth() / 2, closeButton.getMinY() + closeButton.getHeight() / 2);

        //Decoration
        gc.drawImage(cornerTopLeft, 0, 0);
        gc.drawImage(cornerBtmRight, WIDTH - cornerBtmRight.getWidth(), HEIGHT - cornerBtmRight.getHeight());

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        writableImage = canvas.snapshot(transparency, null);

    }

    public void processKey(ArrayList<String> input, Long currentNanoTime)
    {
        int maxMarkedOptionIdx = interfaceElements_list.size() - 1;
        int newMarkedOption = highlightedElement;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - WorldView.getPlayer().getActor().getLastInteraction()) / 1000000000.0;
        if (!(elapsedTimeSinceLastInteraction > TIME_BETWEEN_DIALOGUE))
            return;

        if (input.contains("E") || input.contains("ENTER") || input.contains("SPACE"))
        {
            activateHighlightedOption(currentNanoTime);
            return;
        }
        if (input.contains("W") || input.contains("UP"))
        {
            newMarkedOption--;
        }
        if (input.contains("S") || input.contains("DOWN"))
            newMarkedOption++;

        if (newMarkedOption < 0)
            newMarkedOption = maxMarkedOptionIdx;
        if (newMarkedOption > maxMarkedOptionIdx)
            newMarkedOption = 0;

        if (highlightedElement != newMarkedOption)
        {
            setHighlightedElement(newMarkedOption);
            WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
            render();
        }

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        boolean debug = false;
        Point2D screenPosition = SCREEN_POSITION;
        Rectangle2D posRelativeToWorldview = new Rectangle2D(screenPosition.getX(), screenPosition.getY(), WIDTH, HEIGHT);

        //Calculate Mouse Position relative to Discussion
        Point2D mousePosRelativeToDiscussionOverlay;
        if (posRelativeToWorldview.contains(mousePosition))
            mousePosRelativeToDiscussionOverlay = new Point2D(mousePosition.getX() - screenPosition.getX(), mousePosition.getY() - screenPosition.getY());
        else mousePosRelativeToDiscussionOverlay = null;

        Integer hoveredElement = null;
        if (closeButton.contains(mousePosRelativeToDiscussionOverlay))
            hoveredElement = interfaceElements_list.indexOf(CLOSE_BUTTON_ID);

        if (debug)
            System.out.println(CLASSNAME + methodName + hoveredElement);

        if (GameWindow.getSingleton().isMouseMoved() && hoveredElement != null)//Set highlight if mouse moved
        {
            setHighlightedElement(hoveredElement);
            GameWindow.getSingleton().setMouseMoved(false);
        }

        if (isMouseClicked && hoveredElement != null)//To prevent click of not hovered
        {
            activateHighlightedOption(currentNanoTime);
        }
    }

    private void activateHighlightedOption(Long currentNanoTime)
    {
        String methodName = "activateHighlightedOption(Long) ";
        if (highlightedElement == null)
        {
            System.out.println(CLASSNAME + methodName + "nothing highlighted");
            return;
        }

        if (interfaceElements_list.get(highlightedElement).equals(CLOSE_BUTTON_ID))
        {
            daySummary.endDay();
            //Same level loads but next day
            String levelname = WorldView.getSingleton().getLevelName();
            String spawnId = "bed";
            WorldView.getSingleton().changeStage(levelname, spawnId, true);
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
        }

        WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
    }

    public void setHighlightedElement(Integer highlightedElement)
    {
        String methodName = "setHighlightedElement() ";
        boolean debug = false;
        if (debug && !this.highlightedElement.equals(highlightedElement))
            System.out.println(CLASSNAME + methodName + highlightedElement + " " + interfaceElements_list.get(highlightedElement));
        this.highlightedElement = highlightedElement;
    }

    public WritableImage getWritableImage()
    {
        render();
        return writableImage;
    }

}
