package Core.Menus.Personality;

import Core.Actor;
import Core.GameVariables;
import Core.GameWindow;
import Core.Menus.CoinGame.CharacterCoin;
import Core.Menus.CoinGame.CoinType;
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
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class PersonalityScreenController
{
    private static final String CLASSNAME = "PersonalityScreenController/";
    private static final String BACK_BUTTON_ID = "back";
    private static final int WIDTH = PERSONALITY_WIDTH;
    private static final int HEIGHT = PERSONALITY_HEIGHT;
    private static final Point2D SCREEN_POSITION = PERSONALITY_POSITION;
    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage writableImage;
    Point2D mousePosRelativeToDiscussionOverlay;
    private Integer highlightedElement;
    private List<String> interfaceElements_list = new ArrayList<>();
    private Actor otherPersonActor;
    private PersonalityContainer personalityContainer;
    int rhetoricButtonWidth = 280, rhetoricButtonHeight = 100;
    Rectangle2D exitButton = new Rectangle2D(WIDTH - rhetoricButtonWidth - 50, HEIGHT - rhetoricButtonHeight - 50, rhetoricButtonWidth, rhetoricButtonHeight);
    Image cornerBtmRight, cornerTopLeft, exitButtonImage;
    int backgroundOffsetX = 16;
    int backgroundOffsetYDecorationTop = 10;
    int backgroundOffsetYTalkIcon = 50;
    int backgroundOffsetYDecorationBtm = 10;
    private List<CoinType> personalityList = new ArrayList<>();
    int initTraitsOffsetX = 350;
    int initTraitsOffsetY = 100;
    int traitsYGap = 15;
    Font traitsFont = new Font(25);

    public PersonalityScreenController(Actor otherPersonActor)
    {
        canvas = new Canvas(COINGAME_WIDTH, COIN_AREA_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        this.otherPersonActor = otherPersonActor;
        highlightedElement = 0;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        exitButtonImage = Utilities.readImage(IMAGE_DIRECTORY_PATH + "interface/characteristicsInfo/info_exit.png");
        init();
    }

    private void init()
    {
        if (otherPersonActor.getPersonalityContainer() == null)
            throw new RuntimeException("Personality not defined in actorfile: " + otherPersonActor.getActorFileName());
        personalityContainer = otherPersonActor.getPersonalityContainer();
        personalityList = updateVisiblePersonality();
    }

    public List<CoinType> updateVisiblePersonality()
    {
        List<CoinType> visibleTraits = new ArrayList<>();
        personalityContainer.getTraits().forEach(trait ->
        {
            if (personalityContainer.getCooperation() >= trait.getCooperationVisibilityThreshold()
                    && trait.getCooperationVisibilityThreshold() >= 0
                    || GameVariables.getPlayerKnowledge().contains(trait.getKnowledgeVisibility())
            )
                visibleTraits.add(trait);
        });
        return visibleTraits;
    }

    private void draw() throws NullPointerException
    {
        String methodName = "draw() ";
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        interfaceElements_list.clear(); //Filled with each draw() Maybe better if filled just if elements change
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);

        //Background
        gc.setGlobalAlpha(0.9);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        gc.fillRect(backgroundOffsetX, backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetYDecorationTop - backgroundOffsetYTalkIcon - backgroundOffsetYDecorationBtm);

        gc.setGlobalAlpha(1);
        gc.setFont(traitsFont);
        gc.setFill(COLOR_FONT);
        int firstColumnOffset = 80;
        gc.fillText(otherPersonActor.getActorInGameName(), firstColumnOffset, 150);
        gc.fillText("Cooperation: " + otherPersonActor.getPersonalityContainer().getCooperation(), firstColumnOffset, 230);
        gc.fillText("Known Traits: " + otherPersonActor.getPersonalityContainer().getVisibleCoins().size() + " / " + otherPersonActor.getPersonalityContainer().getTraits().size(), firstColumnOffset, 260);

        int traitsOffsetX = initTraitsOffsetX;
        int traitsOffsetY = initTraitsOffsetY;
        for (int lineIdx = 0; lineIdx < personalityList.size(); lineIdx++)
        {
            CoinType coinType = personalityList.get(lineIdx);
            Image coinImage = CharacterCoin.findImage(coinType.getName());
            gc.setFill(COLOR_FONT);
            gc.fillText(
                    coinType.getName(),
                    Math.round(traitsOffsetX + coinImage.getWidth() + 20),
                    Math.round(traitsOffsetY + coinImage.getHeight() / 2)
            );
            gc.drawImage(coinImage, traitsOffsetX, traitsOffsetY);
            traitsOffsetY += coinImage.getHeight() + traitsYGap;
        }

        //Exit button
        interfaceElements_list.add(BACK_BUTTON_ID);
        if (highlightedElement == interfaceElements_list.indexOf(BACK_BUTTON_ID))//Useful if multiple markable buttons exist
            gc.drawImage(exitButtonImage,exitButton.getMinX(), exitButton.getMinY());

        gc.drawImage(cornerTopLeft, 0, backgroundOffsetYTalkIcon);
        gc.drawImage(cornerBtmRight, WIDTH - cornerBtmRight.getWidth(), HEIGHT - cornerBtmRight.getHeight());

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        writableImage = canvas.snapshot(transparency, null);

    }

    public void processKey(ArrayList<String> input, Long currentNanoTime)
    {
        String methodName = "processKey() ";
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
            draw();
        }

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        Point2D overlayPosition = SCREEN_POSITION;
        //Calculate Mouse Position relative to Overlay
        Rectangle2D posRelativeToWorldview = new Rectangle2D(overlayPosition.getX(), overlayPosition.getY(), WIDTH, HEIGHT);
        if (posRelativeToWorldview.contains(mousePosition))
            mousePosRelativeToDiscussionOverlay = new Point2D(mousePosition.getX() - overlayPosition.getX(), mousePosition.getY() - overlayPosition.getY());
        else mousePosRelativeToDiscussionOverlay = null;

        Integer hoveredElement = null;
        //Check if hovered over Rhetoric Button
        if (exitButton.contains(mousePosRelativeToDiscussionOverlay))
            hoveredElement = interfaceElements_list.indexOf(BACK_BUTTON_ID);

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

        if (interfaceElements_list.get(highlightedElement).equals(BACK_BUTTON_ID))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
        }

        personalityList = updateVisiblePersonality();
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
        draw();
        return writableImage;
    }

}
