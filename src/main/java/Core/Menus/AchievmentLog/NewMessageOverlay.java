package Core.Menus.AchievmentLog;

import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import static Core.Configs.Config.*;


public class NewMessageOverlay
{
    final private static String CLASSNAME = "NewAchievmentOverlay/";
    private Integer WIDTH = MESSAGE_OVERLAY_WIDTH, HEIGHT = MESSAGE_OVERLAY_HEIGHT;
    private static final Point2D SCREEN_POSITION = SCREEN_CENTER;
    private static boolean visible = false;
    private static Text message = new Text("none");

    public NewMessageOverlay()
    {
        message.setFont(FONT_ESTROG);
    }

    public static void showMsg(String msg)
    {
        message.setText(msg);
        visible = true;
        PauseTransition delay = new PauseTransition(Duration.millis(TIME_MS_MESSAGE_VISIBLE));
        delay.setOnFinished(t ->
        {
            visible = false;
        });
        delay.play();
    }

    public void render(GraphicsContext gc, Long currentNanoTime)
    {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setGlobalAlpha(0.7);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int textOffset = 30;
        double textWidth = message.getBoundsInLocal().getWidth();
        gc.fillRect(SCREEN_POSITION.getX() - textWidth / 2f - textOffset, SCREEN_POSITION.getY() - HEIGHT / 2f, textWidth + textOffset * 2, HEIGHT);
        gc.setGlobalAlpha(1);
        gc.setFill(COLOR_RED);
        gc.fillText(message.getText(), SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
    }

    public boolean isVisible()
    {
        return visible;
    }
}
