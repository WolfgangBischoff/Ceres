package Core.Menus.Email;

import Core.GameVariables;
import Core.Sprite.Sprite;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.EMAIL;

public class EmailButtonOverlay
{
    private static final String CLASSNAME = "EmailButtonOverlay/";
    private static final double WIDTH = 120, HEIGHT = 70;
    private static final Point2D SCREEN_POSITION = new Point2D(CAMERA_WIDTH - 150 - 50, 250);
    private static final Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    private static Sprite defaultButton;
    private static Sprite newMailButton;

public EmailButtonOverlay()
{
    defaultButton = new Sprite(IMAGE_DIRECTORY_PATH + "interface/email/default");
    newMailButton = new Sprite(IMAGE_DIRECTORY_PATH + "interface/email/newmail", 1d, 2, 2, 1, 150, 64);
    defaultButton.setPosition(SCREEN_POSITION);
    newMailButton.setPosition(SCREEN_POSITION);
}
    public static double getWIDTH()
    {
        return WIDTH;
    }

    public static double getHEIGHT()
    {
        return HEIGHT;
    }

    public static Rectangle2D getScreenArea()
    {
        return SCREEN_AREA;
    }

    public void render(GraphicsContext gc, Long renderTime)
    {
        Sprite visible;
        if (EmailManager.hasNewEmails())
            visible = newMailButton;
        else
            visible = defaultButton;
        visible.render(gc, renderTime);
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
