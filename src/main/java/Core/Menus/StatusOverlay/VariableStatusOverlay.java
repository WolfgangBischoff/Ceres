package Core.Menus.StatusOverlay;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;

import static Core.Configs.Config.*;

public class VariableStatusOverlay
{
    private static final String CLASSNAME = "VariableStatusOverlay/";
    private final int WIDTH;
    private final int HEIGHT;
    private final Point2D SCREEN_POSITION;
    Image field;

    int current;
    IntegerProperty baseValue;

    public VariableStatusOverlay(int WIDTH, int HEIGHT, IntegerProperty integerProperty, String imagePath, Point2D screenPosition)
    {
        this.baseValue = integerProperty;
        baseValue.addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int) newValue;
            }
        });
        current = baseValue.getValue();
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        field = new Image(IMAGE_DIRECTORY_PATH + imagePath);
        SCREEN_POSITION = screenPosition;
    }

    public void render(GraphicsContext gc)
    {
        String methodName = "render() ";
        int backgroundOffsetX = 80;
        gc.drawImage(field, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.setFill(COLOR_FONT);
        gc.setFont(FONT_ESTROG_30_DEFAULT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("" + current, SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + HEIGHT / 2f);
    }

}
