package Core.Menus.StatusOverlay;

import Core.Utilities;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static Core.Configs.Config.*;

public class BarStatusOverlay
{
    private static final String CLASSNAME = "StatusBarOverlay/";
    private final int WIDTH;
    private final int HEIGHT;
    private final Point2D SCREEN_POSITION;
    private final Image frameImage;
    int maxValue;
    int maxWidthBar = 202;
    int barOffset = 90;
    int current;
    IntegerProperty baseValue;
    Color marking;

    public BarStatusOverlay(BarStatusConfig config)
    {
        this.baseValue = config.integerProperty;
        current = baseValue.getValue();
        baseValue.addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int) newValue;
            }
        });
        this.WIDTH = config.WIDTH;
        this.HEIGHT = config.HEIGHT;
        this.maxValue = config.maxValue;
        this.SCREEN_POSITION = config.SCREEN_POSITION;
        frameImage = Utilities.readImage(IMAGE_DIRECTORY_PATH + config.imagePath);
        marking = config.fillColor;
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";

        //Background
        int backgroundOffsetY = 15;
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        gc.fillRect(SCREEN_POSITION.getX() + barOffset, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - barOffset, HEIGHT - backgroundOffsetY * 2);

        //Fill bar
        float fillPercentage = current / (float) maxValue * maxWidthBar;
        gc.setFill(marking);
        gc.fillRect(SCREEN_POSITION.getX() + barOffset, SCREEN_POSITION.getY() + backgroundOffsetY, fillPercentage, HEIGHT - backgroundOffsetY * 2);
        gc.setGlobalAlpha(1);
        gc.drawImage(frameImage, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
    }

}
