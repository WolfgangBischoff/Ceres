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

public class ShopOverlay
{
    private static final String CLASSNAME = "InventoryOverlay/";
    private Canvas menuCanvas;
    private GraphicsContext menuGc;
    private WritableImage menuImage;
    private Actor actor;
    private List<String> interfaceElements_list = new ArrayList<>();
    private List<Rectangle2D> interfaceElements_Rectangles = new ArrayList<>();
    private Integer highlightedElement = 0;
    private static final int WIDTH = INVENTORY_WIDTH;
    private static final int HEIGHT = INVENTORY_HEIGHT;
    private Point2D SCREEN_POSITION;
    private final Rectangle2D SCREEN_AREA;

    Image cornerTopLeft;
    Image cornerBtmRight;

    public ShopOverlay(Actor actor, Point2D SCREEN_POSITION, InventoryController controller)
    {
        menuCanvas = new Canvas(WIDTH, HEIGHT);
        menuGc = menuCanvas.getGraphicsContext2D();
        this.actor = actor;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";
        gc.setFont(FONT_ORBITRON_12);
        Color marking = COLOR_MARKING;
        Color font = COLOR_FONT;
        interfaceElements_Rectangles.clear();
        interfaceElements_list.clear();

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        //Item Slots
        gc.setGlobalAlpha(1);
        int itemTileWidth = 192;
        int itemTileHeight = 64;
        int numberColumns = 2;
        int numberRows = 5;
        int spaceBetweenTiles = 10;
        int initialOffsetX = (WIDTH - (numberColumns * itemTileWidth + (numberColumns - 1) * spaceBetweenTiles)) / 2; //Centered
        int initialOffsetY = 120 + 75;
        int itemSlotNumber = 0;
        int slotNumber = 0;
        for (int y = 0; y < numberRows; y++)
        {
            int slotY = y * (itemTileHeight + spaceBetweenTiles) + initialOffsetY;
            for (int i = 0; i < numberColumns; i++)
            {
                //Rectangle
                int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
                gc.setFill(font);
                gc.fillRect(SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY, itemTileWidth, itemTileHeight);
                gc.setFill(marking);
                Rectangle2D rectangle2D = new Rectangle2D(slotX + 2, slotY + 2, itemTileWidth - 4, itemTileHeight - 4);
                interfaceElements_Rectangles.add(rectangle2D);
                interfaceElements_list.add(Integer.valueOf(slotNumber).toString());

                //Highlighting
                if (highlightedElement == slotNumber)
                    gc.setFill(font);
                else
                    gc.setFill(marking);
                gc.fillRect(SCREEN_POSITION.getX() + rectangle2D.getMinX(), SCREEN_POSITION.getY() +  rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight());
                slotNumber++;

                //Item slot images
                Collectible current = null;
                if (itemSlotNumber < actor.getInventory().itemsList.size())
                    current = actor.getInventory().itemsList.get(itemSlotNumber);
                if (current != null)
                {
                    gc.drawImage(current.getImage(), SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY);
                    gc.setFill(COLOR_RED);
                    gc.fillText(current.getIngameName(), SCREEN_POSITION.getX() + slotX + 60, SCREEN_POSITION.getY() + slotY + 32);
                    gc.fillText( "GSC: " + current.getBaseValue(), SCREEN_POSITION.getX() + slotX + 60, SCREEN_POSITION.getY() + slotY + 32 + gc.getFont().getSize() + 3);
                }
                itemSlotNumber++;
            }
        }

        //Text
        gc.setFill(font);
        gc.setFont(FONT_ESTROG_20);
        gc.fillText(actor.getActorInGameName() + " Shop", SCREEN_POSITION.getX() + initialOffsetX, SCREEN_POSITION.getY() + 70);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY() );
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
        if (actor.getInventory().size() > highlightedElement && highlightedElement >= 0)
            collectible = actor.getInventory().itemsList.get(highlightedElement);
        if (player == actor)//Playerinventory
        {
            System.out.println(CLASSNAME + methodName + "should not happen");
            //Nothing, selling not possible
        }
        else if(collectible != null)
        {
            int price = collectible.getBaseValue();
            if (GameVariables.getPlayerMoney() >= price)
            {
                GameVariables.addPlayerMoney(-price);
                player.getInventory().addItemNextSlot(collectible);
            }
            else
                System.out.println(CLASSNAME + methodName + "You cannot afford this item.");
        }
        System.out.println(CLASSNAME + methodName + actor.getActorInGameName() + " inventory clicked " + collectible);

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

    public void setMenuCanvas(Canvas menuCanvas)
    {
        this.menuCanvas = menuCanvas;
    }

    public void setMenuGc(GraphicsContext menuGc)
    {
        this.menuGc = menuGc;
    }

    public void setMenuImage(WritableImage menuImage)
    {
        this.menuImage = menuImage;
    }

    public void setActor(Actor actor)
    {
        this.actor = actor;
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
