package Core.Menus.Inventory;

import Core.*;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class IncubatorOverlay
{
    private static final String CLASSNAME = "IncubatorOverlay/";
    private List<String> interfaceElements_list = new ArrayList<>();
    private List<Rectangle2D> interfaceElements_Rectangles = new ArrayList<>();
    private Integer highlightedElement = 0;
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    private Point2D SCREEN_POSITION;
    private Rectangle2D SCREEN_AREA;

    Image cornerTopLeft;
    Image cornerBtmRight;

    public IncubatorOverlay(Point2D SCREEN_POSITION)
    {
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        //Text
        int offsetYFirstLine = 60;
        int dateLength = 200;
        gc.setFill(COLOR_FONT);
        gc.fillText("Incubator", SCREEN_POSITION.getX() + WIDTH - dateLength, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        Rectangle2D posRelativeToWorldview = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);

        //Calculate Mouse Position relative to Discussion
        Point2D relativeMousePosition;
        if (posRelativeToWorldview.contains(mousePosition))
            relativeMousePosition = new Point2D(mousePosition.getX() - SCREEN_POSITION.getX(), mousePosition.getY() - SCREEN_POSITION.getY());
        else relativeMousePosition = null;

        Integer hoveredElement = null;
        for (int i = 0; i < interfaceElements_Rectangles.size(); i++)
        {
            if (interfaceElements_Rectangles.get(i).contains(relativeMousePosition))
            {
                hoveredElement = interfaceElements_list.indexOf(Integer.toString(i));
            }
        }

        if (GameWindow.getSingleton().isMouseMoved() && hoveredElement != null)//Set highlight if mouse moved
        {
            setHighlightedElement(hoveredElement);
            GameWindow.getSingleton().setMouseMoved(false);
        }


        if (isMouseClicked && hoveredElement != null)//To prevent click of not hovered
        {
            activateHighlightedOption(currentNanoTime);
        }
    }

    private void activateHighlightedOption(Long currentNanoTime)
    {
        String methodName = "activateHighlightedOption() ";
        Collectible collectible = null;
        Actor player = WorldView.getPlayer().getActor();

    }

    public void setHighlightedElement(Integer highlightedElement)
    {
        String methodName = "setHighlightedElement() ";
        boolean debug = false;
        if (debug && !this.highlightedElement.equals(highlightedElement))
            System.out.println(CLASSNAME + methodName + highlightedElement + " " + interfaceElements_list.get(highlightedElement));
        this.highlightedElement = highlightedElement;
    }

    public static int getMenuWidth()
    {
        return WIDTH;
    }

    public static int getMenuHeight()
    {
        return HEIGHT;
    }


    public void setInterfaceElements_list(List<String> interfaceElements_list)
    {
        this.interfaceElements_list = interfaceElements_list;
    }

    public void setInterfaceElements_Rectangles(List<Rectangle2D> interfaceElements_Rectangles)
    {
        this.interfaceElements_Rectangles = interfaceElements_Rectangles;
    }

    public void setSCREEN_POSITION(Point2D SCREEN_POSITION)
    {
        this.SCREEN_POSITION = SCREEN_POSITION;
    }

    public void setCornerTopLeft(Image cornerTopLeft)
    {
        this.cornerTopLeft = cornerTopLeft;
    }

    public void setCornerBtmRight(Image cornerBtmRight)
    {
        this.cornerBtmRight = cornerBtmRight;
    }

    public Rectangle2D getSCREEN_AREA()
    {
        return SCREEN_AREA;
    }
}
