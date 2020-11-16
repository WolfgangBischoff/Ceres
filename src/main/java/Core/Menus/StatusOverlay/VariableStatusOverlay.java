package Core.Menus.StatusOverlay;

import Core.Configs.Config;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static Core.Configs.Config.*;

public class VariableStatusOverlay
{
    private static final String CLASSNAME = "VariableStatusOverlay/";
    private final int WIDTH;
    private final int HEIGHT;
    Canvas canvas;
    GraphicsContext graphicsContext;
    WritableImage writableImage;
    Image field;

    int current;
    IntegerProperty baseValue;

    public VariableStatusOverlay(int WIDTH, int HEIGHT, IntegerProperty integerProperty, String imagepath)
    {
        this.baseValue = integerProperty;
        baseValue.addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int)newValue;
            }
        });
        current = baseValue.getValue();
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        field = new Image(IMAGE_DIRECTORY_PATH + imagepath);
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

        int backgroundOffsetX = 55, backgroundOffsetY = 32;
        graphicsContext.drawImage(field, 0,0);
        graphicsContext.setFill(marking);
        graphicsContext.setFill(font);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFont(Font.loadFont(getClass().getResource("../../../../../../build/resources/main/font/estrog__.ttf").toExternalForm(), 30));
        graphicsContext.fillText(""+current,  backgroundOffsetX, backgroundOffsetY);

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
