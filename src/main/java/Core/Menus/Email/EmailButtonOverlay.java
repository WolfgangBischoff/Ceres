package Core.Menus.Email;

import Core.GameVariables;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

import static Core.Configs.Config.CAMERA_WIDTH;
import static Core.Configs.Config.COLOR_FONT;
import static Core.WorldView.WorldViewStatus.EMAIL;

public class EmailButtonOverlay
{
    private static final String CLASSNAME = "EmailButtonOverlay/";
    private static final double WIDTH = 120, HEIGHT = 70;
    private static final Point2D SCREEN_POSITION = new Point2D(CAMERA_WIDTH - 150 - 50, 250);
    private static final Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);

    public static double getWIDTH()
    {
        return WIDTH;
    }

    public static double getHEIGHT()
    {
        return HEIGHT;
    }

    public static Point2D getScreenPosition()
    {
        return SCREEN_POSITION;
    }

    public static Rectangle2D getScreenArea()
    {
        return SCREEN_AREA;
    }

    public void render(GraphicsContext gc)
    {
        //if (EmailManager.hasNewEmails())
        //    System.out.println("new mails");
        gc.setFill(COLOR_FONT);
        gc.fillRect(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked)
    {
        if(isMouseClicked)
        {
            WorldViewController.setWorldViewStatus(EMAIL);
            EmailManager.readNewEmail();
        }

    }
}
