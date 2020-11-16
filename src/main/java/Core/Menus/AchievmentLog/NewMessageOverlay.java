package Core.Menus.AchievmentLog;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import static Core.Configs.Config.*;


public class NewMessageOverlay
{
    final private static String CLASSNAME = "NewAchievmentOverlay/";
    private Integer WIDTH = MESSAGE_OVERLAY_WIDTH, HEIGHT = MESSAGE_OVERLAY_HEIGHT;
    private static final Point2D SCREEN_POSITION = MESSAGE_OVERLAY_POSITION;
    private Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private static boolean visible = false;
    private static String message = "none";

    public static void showMsg(String msg)
    {
        message = msg;
        visible = true;
        PauseTransition delay = new PauseTransition(Duration.millis(4000));
        delay.setOnFinished(t ->
        {
            visible = false;
        });
        delay.play();
    }

    public WritableImage render(Long currentNanoTime)
    {
        gc.clearRect(0,0,WIDTH,HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.setGlobalAlpha(0.7);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        gc.fillRect(0,0, WIDTH, HEIGHT);
        gc.setGlobalAlpha(1);
        gc.setFill(COLOR_RED);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        gc.fillText(message,WIDTH/2,HEIGHT/2);
        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        return canvas.snapshot(transparency, null);
    }

    public boolean isVisible()
    {
        return visible;
    }
}
