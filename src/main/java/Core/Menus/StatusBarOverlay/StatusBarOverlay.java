package Core.Menus.StatusBarOverlay;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static Core.Configs.Config.IMAGE_DIRECTORY_PATH;

public class StatusBarOverlay
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

    //public StatusBarOverlay(int WIDTH, int HEIGHT, IntegerProperty integerProperty, int maxValue)
    public StatusBarOverlay(StatusBarConfig config)
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
        frameImage = new Image(IMAGE_DIRECTORY_PATH + config.imagePath);
        marking = config.fillColor;
    }

    private void draw() throws NullPointerException
    {
        boolean debug = false;
        String methodName = "draw() ";
        graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);
        //Color background = Color.rgb(60, 90, 85);
        Color background = Color.rgb(20, 30, 30);
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        //Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);
        //Color red = Color.hsb(0, 0.33, 0.90);
        //Color green = Color.hsb(140, 0.33, 0.90);

        //Background
        graphicsContext.setGlobalAlpha(0.8);
        graphicsContext.setFill(background);
        int backgroundOffsetX = 16, backgroundOffsetY = 15;
        graphicsContext.fillRect(barOffset, backgroundOffsetY, WIDTH - backgroundOffsetX, HEIGHT - backgroundOffsetY * 2);

        //Fill bar
        float fillPercentage= current / (float) maxValue * maxWidthBar;
        graphicsContext.setFill(marking);
        graphicsContext.fillRect(barOffset, backgroundOffsetY, fillPercentage, HEIGHT - backgroundOffsetY * 2);


        graphicsContext.setGlobalAlpha(1);
        graphicsContext.drawImage(frameImage,0,0);

        if(debug)
        {
            graphicsContext.setStroke(Color.RED);
            graphicsContext.strokeRect(0,0,WIDTH,HEIGHT);
            String msg = "Current: " + current + " max: " + maxValue + " Percent: " + current / (float) maxValue;
            graphicsContext.setFill(font);
            graphicsContext.fillText(msg, barOffset, 20 + graphicsContext.getFont().getSize());
        }

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
