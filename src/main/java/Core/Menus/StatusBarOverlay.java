package Core.Menus;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static Core.Config.IMAGE_DIRECTORY_PATH;

public class StatusBarOverlay
{
    private static final String CLASSNAME = "StatusBarOverlay ";
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

    public StatusBarOverlay(int WIDTH, int HEIGHT, IntegerProperty integerProperty, int maxValue)
    {
        this.baseValue = integerProperty;
        current = baseValue.getValue();
        baseValue.addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int)newValue;
            }
        });
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.maxValue = maxValue;
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        frameImage = new Image(IMAGE_DIRECTORY_PATH + "interface/bars/MaM_bar_400x64.png");
    }

    private void draw() throws NullPointerException
    {
        String methodName = "draw() ";
        graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);
        Color background = Color.rgb(60, 90, 85);
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);
        Color red = Color.hsb(0, 0.33, 0.90);
        Color green = Color.hsb(140, 0.33, 0.90);

        //Background
        graphicsContext.setGlobalAlpha(0.8);
        graphicsContext.setFill(background);
        int backgroundOffsetX = 16, backgroundOffsetY = 0;
        graphicsContext.fillRect(barOffset, backgroundOffsetY, WIDTH - backgroundOffsetX, HEIGHT - backgroundOffsetY * 2);

        //Fill bar
        float fillPercentage= current / (float) maxValue * maxWidthBar;
        graphicsContext.setFill(marking);
        graphicsContext.fillRect(barOffset, backgroundOffsetY, fillPercentage, HEIGHT - backgroundOffsetY * 2);
        String msg = "Current: " + current + " max: " + maxValue +  " Percent: " + current / (float) maxValue;
        graphicsContext.setFill(font);
        graphicsContext.fillText(msg,  barOffset, 20 + graphicsContext.getFont().getSize());

        graphicsContext.setGlobalAlpha(1);
        graphicsContext.drawImage(frameImage,0,0);

        graphicsContext.setStroke(Color.RED);
        //graphicsContext.strokeRect(0,0,WIDTH,HEIGHT);

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        writableImage = canvas.snapshot(transparency, null);
    }

    public WritableImage getWritableImage()
    {
        draw();
        return writableImage;
    }

}
