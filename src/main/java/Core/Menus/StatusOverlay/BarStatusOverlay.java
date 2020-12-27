package Core.Menus.StatusOverlay;

import Core.Utilities;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static Core.Configs.Config.*;

public class BarStatusOverlay
{
    private static final String CLASSNAME = "StatusBarOverlay/";
    private final int WIDTH;
    private final int HEIGHT;
    private final Image frameImage;
    Canvas canvas;
    GraphicsContext graphicsContext;
    WritableImage writableImage;

    int maxValue;
    int maxWidthBar = 202; //from picture
    int barOffset = 90;//from picture
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
                current = (int)newValue;
            }
        });
        this.WIDTH = config.WIDTH;
        this.HEIGHT = config.HEIGHT;
        this.maxValue = config.maxValue;
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        frameImage = Utilities.readImage(IMAGE_DIRECTORY_PATH + config.imagePath);
        marking = config.fillColor;
    }

    public WritableImage render() throws NullPointerException
    {
        boolean debug = false;
        String methodName = "draw() ";
        // graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);
//
        // //Background
        // int backgroundOffsetX = 16, backgroundOffsetY = 15;
        // graphicsContext.setGlobalAlpha(0.8);
        // graphicsContext.setFill(COLOR_BACKGROUND_BLUE);
        // graphicsContext.fillRect(barOffset, backgroundOffsetY, WIDTH - backgroundOffsetX, HEIGHT - backgroundOffsetY * 2);
//
        // //Fill bar
        // float fillPercentage= current / (float) maxValue * maxWidthBar;
        // graphicsContext.setFill(marking);
        // graphicsContext.fillRect(barOffset, backgroundOffsetY, fillPercentage, HEIGHT - backgroundOffsetY * 2);
//
//
        // graphicsContext.setGlobalAlpha(1);
        // graphicsContext.drawImage(frameImage,0,0);
//
        // if(debug)
        // {
        //     graphicsContext.setStroke(Color.RED);
        //     graphicsContext.strokeRect(0,0,WIDTH,HEIGHT);
        //     String msg = "Current: " + current + " max: " + maxValue + " Percent: " + current / (float) maxValue;
        //     graphicsContext.setFill(COLOR_FONT);
        //     graphicsContext.fillText(msg, barOffset, 20 + graphicsContext.getFont().getSize());
        // }

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        return canvas.snapshot(transparency, null);
    }

}
