package Core.Menus.StatusBarOverlay;

import Core.Clock;
import Core.Configs.Config;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static Core.Configs.Config.COLOR_BACKGROUND_BLUE;
import static Core.Configs.Config.IMAGE_DIRECTORY_PATH;

public class ClockOverlay
{
    private static final String CLASSNAME = "ClockOverlay ";
    private final int WIDTH;
    private final int HEIGHT;
    Canvas canvas;
    GraphicsContext graphicsContext;
    WritableImage writableImage;
    int current;
    Clock clock;
    Image frameImage;


    public ClockOverlay(StatusBarConfig config, Clock clock)
    {
        this.clock = clock;
        clock.timeProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int)newValue;
            }
        });
        current = clock.getTime();
        this.WIDTH = config.WIDTH;
        this.HEIGHT = config.HEIGHT;
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        frameImage = new Image(IMAGE_DIRECTORY_PATH + config.imagePath);
    }

    private void draw() throws NullPointerException
    {
        String methodName = "draw() ";
        graphicsContext.clearRect(0, 0, WIDTH, HEIGHT);
        Color background = COLOR_BACKGROUND_BLUE;
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);

        graphicsContext.setGlobalAlpha(1);
        graphicsContext.drawImage(frameImage,0,0);

        //Background
        graphicsContext.setGlobalAlpha(0.8);
       //graphicsContext.setFill(background);
       //graphicsContext.fillRect(30, 20, WIDTH-60, HEIGHT-40);

        //Fill bar
        graphicsContext.setFill(marking);
        String msg = "" + clock.getFormattedTime();
        graphicsContext.setFill(font);
        graphicsContext.setFont(Font.loadFont(getClass().getResource("../../../../../../build/resources/main/font/estrog__.ttf").toExternalForm(), 30));
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.fillText(msg,   WIDTH/2,  HEIGHT/2+Config.FONT_Y_OFFSET_ESTROG__SIZE30);



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
