package Core.Menus.CoinGame;

import Core.Actor;
import Core.Menus.Inventory.MouseElement;
import Core.Menus.Inventory.MouseElementsContainer;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;

public class CoinGame
{
    private static String CLASSNAME = "CoinGame/";
    static CoinArea coinArea;
    private final Image cornerTopLeft, cornerBtmRight, finishedButton;
    private final Integer WIDTH = COINGAME_WIDTH, HEIGHT = COINGAME_HEIGHT;
    private final Point2D SCREEN_POSITION = COINGAME_POSITION;
    private final MouseElementsContainer mouseElements = new MouseElementsContainer();
    private MouseElement highlightedElement = null;
    final String CHECKMARK_BUTTON_ID = "CHECKMARK";
    //private Circle checkMarkButton = new Circle(COIN_AREA_WIDTH + 135, 400, 75);

    public CoinGame(String gameIdentifier, Actor actorOfDiscussion)
    {
        coinArea = new CoinArea(gameIdentifier, actorOfDiscussion);
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        finishedButton = Utilities.readImage(IMAGE_DIRECTORY_PATH + "interface/coinGame/finished.png");
        init();
    }

    private void init()
    {
        Circle checkMarkButton = new Circle(SCREEN_POSITION.getX() + COIN_AREA_WIDTH +135, SCREEN_POSITION.getY() + 400, 75);
        mouseElements.add(new MouseElement(checkMarkButton, CHECKMARK_BUTTON_ID, CLICK));
    }

    public void render(GraphicsContext gc, Long currentNanoTime)
    {
        //Background
        gc.setFill(COLOR_BACKGROUND_GREY);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        coinArea.render(gc, currentNanoTime);
        gc.setFont(FONT_ESTROG_20);
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

        //Infos
        gc.setFill(COLOR_FONT);
        int textCenterOffset = 100;
        int numberBuffs = 0;
        gc.fillText("Active Buffs", SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 40);
        if (coinArea.getActiveBuffs().isEmpty())
            gc.fillText("None", SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                    SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 60);
        for (Map.Entry<String, CharacterCoinBuff> entry : coinArea.getActiveBuffs().entrySet()) {
            String key = entry.getKey();
            CharacterCoinBuff value = entry.getValue();
            gc.fillText(value.getName(), SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                    SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 65 + numberBuffs * (gc.getFont().getSize() + 5));
            numberBuffs++;
        }

        List<CoinType> visibleTraits = coinArea.actorOfDiscussion.getPersonalityContainer().getVisibleCoins();
        int numberInvisibleTraits = coinArea.actorOfDiscussion.getPersonalityContainer().getTraits().size() - visibleTraits.size();
        gc.fillText("Unknown Traits: " + numberInvisibleTraits, SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 150);

        double residualTime = coinArea.getMaxGameTime() - (currentNanoTime - coinArea.gameStartTime) / 1000000000.0;
        if (residualTime < 4)
            gc.setFill(COLOR_RED);
        if (residualTime < 0)
            residualTime = 0;
        gc.fillText("Time:", SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 210);
        gc.fillText(Utilities.roundTwoDigits(residualTime), SCREEN_POSITION.getX() + COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + textCenterOffset,
                SCREEN_POSITION.getY() + COIN_AREA_HEIGHT_OFFSET + 230);

        Circle checkMarkButton = (Circle) mouseElements.get(CHECKMARK_BUTTON_ID).getPosition();
        gc.drawImage(finishedButton, checkMarkButton.getCenterX() - checkMarkButton.getRadius(), checkMarkButton.getCenterY() - checkMarkButton.getRadius());
        gc.setFont(FONT_ESTROG_30_DEFAULT);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        Circle checkMarkButton = (Circle) mouseElements.get(CHECKMARK_BUTTON_ID).getPosition();

        //End CoinGame
        if (isMouseClicked && coinArea.isFinished &&
                ( checkMarkButton.contains(mousePosition)
                        || CoinArea.getScreenArea().contains(mousePosition)))
        {
            WorldView.getTextbox().nextMessage(currentNanoTime);
            WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
        }
        else if (CoinArea.getScreenArea().contains(mousePosition))
        {
            coinArea.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }
        else if (isMouseClicked && checkMarkButton.contains(mousePosition))
        {
                getCoinArea().coinsList.forEach(
                        coin ->
                        {
                            if (!getCoinArea().removedCoinsList.contains(coin))
                                getCoinArea().removedCoinsList.add(coin);
                        });
                getCoinArea().visibleCoinsList.clear();
                getCoinArea().setMaxGameTime(-1);
        }

    }

    public static CoinArea getCoinArea()
    {
        return coinArea;
    }
}
