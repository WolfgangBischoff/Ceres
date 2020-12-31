package Core.Menus.StatusOverlay;

import Core.Clock;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.TextAlignment;

import static Core.Configs.Config.*;

public class ClockOverlay
{
    private static final String CLASSNAME = "ClockOverlay/";
    private final int WIDTH;
    private final int HEIGHT;
    private final Point2D SCREENPOSITION;
    int current;
    Clock clock;
    Image frameImage;


    public ClockOverlay(BarStatusConfig config, Clock clock)
    {
        this.clock = clock;
        clock.timeProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                current = (int) newValue;
            }
        });
        current = clock.getTime();
        this.WIDTH = config.WIDTH;
        this.HEIGHT = config.HEIGHT;
        this.SCREENPOSITION = config.SCREEN_POSITION;
        frameImage = new Image(IMAGE_DIRECTORY_PATH + config.imagePath);
    }

    public void render(GraphicsContext gc)
    {
        String methodName = "render() ";
        gc.drawImage(frameImage, SCREENPOSITION.getX(), SCREENPOSITION.getY());

        //Background
        gc.setGlobalAlpha(0.8);
        String msg = "" + clock.getFormattedTime();
        gc.setFill(COLOR_FONT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(msg, SCREENPOSITION.getX() + WIDTH / 2f, SCREENPOSITION.getY() + HEIGHT / 2f);
        gc.setGlobalAlpha(1);
    }

}
