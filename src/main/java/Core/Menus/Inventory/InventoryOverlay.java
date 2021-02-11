package Core.Menus.Inventory;

import Core.*;
import Core.Enums.CollectableType;
import Core.WorldView.WorldViewController;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.INVENTORY_EXCHANGE;
import static Core.WorldView.WorldViewStatus.INVENTORY_SHOP;

public class InventoryOverlay
{
    private static final String CLASSNAME = "InventoryOverlay ";
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    Image cornerTopLeft;
    Image cornerBtmRight;
    private InventoryController controller;
    private Actor actor;
    private List<String> interfaceElements_list = new ArrayList<>();
    private List<Rectangle2D> interfaceElements_Rectangles = new ArrayList<>();
    private Integer highlightedElement = 0;
    private Point2D SCREEN_POSITION;
    private Rectangle2D SCREEN_AREA;

    public InventoryOverlay(Actor actor, Point2D SCREEN_POSITION, InventoryController controller)
    {
        this.actor = actor;
        this.controller = controller;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    }

    public static void setWIDTH(int WIDTH)
    {
        InventoryOverlay.WIDTH = WIDTH;
    }

    public static void setHEIGHT(int HEIGHT)
    {
        InventoryOverlay.HEIGHT = HEIGHT;
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";
        Color marking = COLOR_MARKING;
        Color font = COLOR_FONT;
        Color darkRed = Color.hsb(0, 0.23, 0.70);
        interfaceElements_Rectangles.clear();
        interfaceElements_list.clear();

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(backgroundOffsetX + SCREEN_POSITION.getX(), SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        //Item Slots
        gc.setGlobalAlpha(1);
        int itemTileWidth = 64;
        int numberColumns = 5;
        int numberRows = 6;
        int spaceBetweenTiles = 10;
        int initialOffsetX = (WIDTH - (numberColumns * itemTileWidth + (numberColumns - 1) * spaceBetweenTiles)) / 2; //Centered
        int initialOffsetY = 75;
        int itemSlotNumber = 0;
        int slotNumber = 0;
        for (int y = 0; y < numberRows; y++)
        {
            int slotY = y * (itemTileWidth + spaceBetweenTiles) + initialOffsetY;
            for (int i = 0; i < numberColumns; i++)
            {
                //Rectangle
                int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
                gc.setFill(font);
                gc.fillRect(SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY, itemTileWidth, itemTileWidth);
                gc.setFill(marking);
                Rectangle2D rectangle2D = new Rectangle2D(SCREEN_POSITION.getX() + slotX + 2, SCREEN_POSITION.getY() + slotY + 2, itemTileWidth - 4, itemTileWidth - 4);
                interfaceElements_Rectangles.add(rectangle2D);
                interfaceElements_list.add(Integer.valueOf(slotNumber).toString());

                //Highlighting
                if (highlightedElement == slotNumber)
                    gc.setFill(font);
                else
                    gc.setFill(marking);
                gc.fillRect(rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight());
                slotNumber++;

                //Item slot images
                Collectible current = null;
                if (itemSlotNumber < actor.getInventory().itemsList.size())
                    current = actor.getInventory().itemsList.get(itemSlotNumber);
                if (current != null)
                {
                    gc.drawImage(current.getImage(), SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY);
                    //Stolen sign
                    if (GameVariables.getStolenCollectibles().contains(current))
                    {
                        gc.setFill(darkRed);
                        gc.fillOval(SCREEN_POSITION.getX() + slotX + 44, SCREEN_POSITION.getY() + slotY + 44, 16, 16);
                        gc.setFill(COLOR_RED);
                        gc.fillOval(SCREEN_POSITION.getX() + slotX + 46, SCREEN_POSITION.getY() + slotY + 46, 12, 12);
                    }
                }
                itemSlotNumber++;
            }
        }

        //Text
        int offsetYFirstLine = 60;
        gc.setFill(font);
        gc.setFont(FONT_ESTROG_20);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(actor.getActorInGameName().toUpperCase(), SCREEN_POSITION.getX() + initialOffsetX, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, boolean isMouseDragged, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";

        Integer hoveredElement = null;
        for (int i = 0; i < interfaceElements_Rectangles.size(); i++)
        {
            if (interfaceElements_Rectangles.get(i).contains(mousePosition))
            {
                hoveredElement = interfaceElements_list.indexOf(Integer.toString(i));
            }
        }
        //System.out.println(CLASSNAME + actor.getActorInGameName() + " " + hoveredElement + " " + isMouseDragged);

        if ((GameWindow.getSingleton().isMouseMoved() || isMouseDragged) && hoveredElement != null)//Set highlight if mouse moved
        {
            setHighlightedElement(hoveredElement);
            GameWindow.getSingleton().setMouseMoved(false);
        }


        if (isMouseClicked && hoveredElement != null)//To prevent click of not hovered
        {
            activateHighlightedOption(currentNanoTime);
        }
        else if (isMouseDragged)
        {
            dragCollectible(currentNanoTime, mousePosition);
        }
        else if (controller.getDragAndDropItem() != null)
        {
            controller.setDragAndDropItem(null);
        }
    }

    private void dragCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        Collectible collectible;
        if (actor.getInventory().itemsList.size() > highlightedElement && highlightedElement >= 0 && controller.getDragAndDropItem() == null)
        {
            collectible = actor.getInventory().itemsList.get(highlightedElement);
            controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible));
        }
        else if (controller.getDragAndDropItem() != null)
        {
            controller.getDragAndDropItem().setPosition(new Point2D(mousePosition.getX(), mousePosition.getY()));
        }
    }

    private void activateHighlightedOption(Long currentNanoTime)
    {
        String methodName = "activateHighlightedOption() ";
        Collectible collectible = null;
        if (actor.getInventory().itemsList.size() > highlightedElement && highlightedElement >= 0)
            collectible = actor.getInventory().itemsList.get(highlightedElement);

        System.out.println(CLASSNAME + methodName + actor.getActorInGameName() + " inventory clicked " + collectible);

        if (collectible != null && WorldViewController.getWorldViewStatus() == INVENTORY_EXCHANGE)
        {
            //check from which inventory to which inventory we exchange
            if (InventoryController.playerInventoryOverlay == this)
            {
                InventoryController.exchangeInventoryActor.getInventory().addItem(collectible);
                InventoryController.playerActor.getInventory().removeItem(collectible);
            }
            else if (InventoryController.otherInventoryOverlay == this)
            {
                InventoryController.playerActor.getInventory().addItem(collectible);
                InventoryController.exchangeInventoryActor.getInventory().removeItem(collectible);
            }
        }
        else if (WorldViewController.getWorldViewStatus() == INVENTORY_SHOP)
        {
            System.out.println(CLASSNAME + methodName + "Clicked in item, shopmode, nothing happens");
        }
        else if (collectible != null && collectible.getType() == CollectableType.FOOD)
        {
            System.out.println(CLASSNAME + methodName + "You ate " + collectible.getIngameName());
            GameVariables.addHunger(collectible.getBaseValue());
            actor.getInventory().itemsList.remove(collectible);
            GameVariables.getStolenCollectibles().remove(collectible);
        }

    }

    public void setHighlightedElement(Integer highlightedElement)
    {
        String methodName = "setHighlightedElement() ";
        boolean debug = false;
        if (debug && !this.highlightedElement.equals(highlightedElement))
            System.out.println(CLASSNAME + methodName + highlightedElement + " " + interfaceElements_list.get(highlightedElement));
        this.highlightedElement = highlightedElement;
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
