package Core.Menus.Email;

import Core.WorldView.WorldViewController;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.TextAlignment;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.WORLD;

public class EmailOverlay
{
    private static final String CLASSNAME = "EmailOverlay/";
    private static final double WIDTH = 600, HEIGHT = 500;
    private static final Point2D SCREEN_POSITION = new Point2D(150, 150);
    private static Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);

    public static double getWIDTH()
    {
        return WIDTH;
    }

    public static double getHEIGHT()
    {
        return HEIGHT;
    }

    public void render(GraphicsContext gc)
    {
        gc.setFill(COLOR_BACKGROUND_BLUE);
        gc.fillRect(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        gc.setFill(COLOR_FONT);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(EmailManager.readFirstEmail().text, SCREEN_POSITION.getX() + 20, SCREEN_POSITION.getY() + 20);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked)
    {
        if (isMouseClicked)
        {
            WorldViewController.setWorldViewStatus(WORLD);
        }

    }
}
