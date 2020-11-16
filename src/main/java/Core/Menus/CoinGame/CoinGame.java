package Core.Menus.CoinGame;

import Core.Actor;
import Core.GameVariables;
import Core.Utilities;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static Core.Configs.Config.*;

public class CoinGame
{
    private static String CLASSNAME = "CoinGame/";
    static CoinArea coinArea;
    private Canvas canvas;
    private GraphicsContext gc;
    private Image cornerTopLeft, cornerBtmRight, finishedButton;
    private Integer WIDTH = COINGAME_WIDTH, HEIGHT = COINGAME_HEIGHT;
    private static final Point2D SCREEN_POSITION = COINGAME_POSITION;
    private Circle exitButton = new Circle(COIN_AREA_WIDTH + 135, 400, 75);

    public CoinGame(String gameIdentifier, Actor actorOfDiscussion)
    {
        coinArea = new CoinArea(gameIdentifier, actorOfDiscussion);
        cornerTopLeft = new Image(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = new Image(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        finishedButton = new Image(IMAGE_DIRECTORY_PATH + "interface/coinGame/finished.png");
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
    }

    public WritableImage render(Long currentNanoTime)
    {
        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_GREY);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(backgroundOffsetX, backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        gc.setGlobalAlpha(1);
        gc.drawImage(coinArea.render(currentNanoTime), COIN_AREA_WIDTH_OFFSET, COIN_AREA_HEIGHT_OFFSET);
        gc.drawImage(cornerTopLeft, 0, 0);
        gc.drawImage(cornerBtmRight, WIDTH - cornerBtmRight.getWidth(), HEIGHT - cornerBtmRight.getHeight());

        gc.setFill(COLOR_FONT);
        int numberBuffs = 0;
        gc.fillText("Active Buffs", COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + 10,
                COIN_AREA_HEIGHT_OFFSET + 40);
        if (coinArea.getActiveBuffs().isEmpty())
            gc.fillText("None", COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + 10,
                    COIN_AREA_HEIGHT_OFFSET + 60);
        for (Map.Entry<String, CharacterCoinBuff> entry : coinArea.getActiveBuffs().entrySet())
        {
            String key = entry.getKey();
            CharacterCoinBuff value = entry.getValue();
            gc.fillText(value.getName(), COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + 10,
                    COIN_AREA_HEIGHT_OFFSET + 60 + numberBuffs * (gc.getFont().getSize() + 10));
            numberBuffs++;
        }

        List<CoinType> visibleTraits = coinArea.actorOfDiscussion.getPersonalityContainer().getVisibleCoins();
        int numberInvisibleTraits = coinArea.actorOfDiscussion.getPersonalityContainer().getTraits().size() - visibleTraits.size();
        gc.fillText("Invisible Traits: " + numberInvisibleTraits, COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + 10,
                COIN_AREA_HEIGHT_OFFSET + 150);

        double residualTime = coinArea.getMaxGameTime() - (currentNanoTime - coinArea.gameStartTime) / 1000000000.0;
        if (residualTime < 0)
            residualTime = 0;
        gc.fillText("Remaining Time: " + Utilities.roundTwoDigits(residualTime), COIN_AREA_WIDTH + COIN_AREA_WIDTH_OFFSET + 10,
                COIN_AREA_HEIGHT_OFFSET + 200);

        gc.setFill(Color.RED);
        gc.drawImage(finishedButton,exitButton.getCenterX() - exitButton.getRadius(), exitButton.getCenterY() - exitButton.getRadius());

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        return canvas.snapshot(transparency, null);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        Point2D overlayPosition = SCREEN_POSITION;
        Rectangle2D posRelativeToWorldview = new Rectangle2D(overlayPosition.getX(), overlayPosition.getY(), WIDTH, HEIGHT);
        Point2D mousePosRelativeToOverlay;
        if (posRelativeToWorldview.contains(mousePosition))
            mousePosRelativeToOverlay = new Point2D(mousePosition.getX() - overlayPosition.getX(), mousePosition.getY() - overlayPosition.getY());
        else mousePosRelativeToOverlay = null;

        if (CoinArea.getScreenArea().contains(mousePosition))
        {
            coinArea.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }
        else if (isMouseClicked && mousePosRelativeToOverlay != null && exitButton.contains(mousePosRelativeToOverlay))
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
