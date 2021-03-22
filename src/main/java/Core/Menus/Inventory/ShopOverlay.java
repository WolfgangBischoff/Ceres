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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;

public class ShopOverlay
{
    private static final String CLASSNAME = "InventoryOverlay/";
    private Actor actor;
    private static final int WIDTH = INVENTORY_WIDTH;
    private static final int HEIGHT = INVENTORY_HEIGHT;
    private Point2D SCREEN_POSITION;
    private final Rectangle2D SCREEN_AREA;
    private MouseElementsContainer mouseElements = new MouseElementsContainer();
    private MouseElement highlightedElement = null;
    private final InventoryController controller;

    Image cornerTopLeft;
    Image cornerBtmRight;

    public ShopOverlay(Actor actor, Point2D SCREEN_POSITION, InventoryController controller)
    {
        this.actor = actor;
        this.controller = controller;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        init();
    }

    private void init()
    {
        //Item Slots
        Set<MouseInteractionType> click = new HashSet<>();
        click.add(CLICK);
        int itemTileWidth = 192;
        int itemTileHeight = 64;
        int numberColumns = 2;
        int numberRows = 5;
        int spaceBetweenTiles = 10;
        int initialOffsetX = (WIDTH - (numberColumns * itemTileWidth + (numberColumns - 1) * spaceBetweenTiles)) / 2; //Centered
        int initialOffsetY = 120 + 75;
        int slotNumber = 0;
        for (int y = 0; y < numberRows; y++)
        {
            int slotY = y * (itemTileHeight + spaceBetweenTiles) + initialOffsetY;
            for (int i = 0; i < numberColumns; i++)
            {
                int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
                Rectangle2D rectangle2D = new Rectangle2D(SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY, itemTileWidth, itemTileHeight);
                MouseElement slot = new MouseElement(rectangle2D, Integer.valueOf(slotNumber).toString(), click );
                mouseElements.add(slot);
                slotNumber++;
            }
        }
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";
        gc.setFont(FONT_ORBITRON_12);
        Color marking = COLOR_MARKING;
        Color font = COLOR_FONT;

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
                Rectangle2D currentRect = mouseElements.get(slotNumber).position;
                int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
                gc.setFill(font);
                gc.fillRect(currentRect.getMinX(), currentRect.getMinY(), currentRect.getWidth(), currentRect.getHeight());

                //Highlighting
                if (mouseElements.indexOf(highlightedElement) == slotNumber)
                    gc.setFill(font);
                else
                    gc.setFill(marking);
                gc.fillRect(currentRect.getMinX() +2, currentRect.getMinY() +2, currentRect.getWidth()-4, currentRect.getHeight()-4);

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
        MouseElement hoveredElement = null;
        for (int i = 0; i < mouseElements.size(); i++)
        {
            if (mouseElements.get(i).getPosition().contains(mousePosition))
            {
                hoveredElement = mouseElements.get(i);
                if (hoveredElement == highlightedElement)
                {
                    MouseElement tooltipElement = mouseElements.get(i);
                    controller.setTooltipElement(tooltipElement);
                    if (mouseElements.indexOf(tooltipElement) >= 0 && mouseElements.indexOf(tooltipElement) < actor.getInventory().itemsList.size())
                    {
                        controller.setTooltippedCollectible(actor.getInventory().itemsList.get(mouseElements.indexOf(tooltipElement)));
                    }
                }
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
        int itemIdx = mouseElements.indexOf(highlightedElement);
        if (actor.getInventory().size() > itemIdx && itemIdx >= 0)
            collectible = actor.getInventory().itemsList.get(itemIdx);

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

    public void setHighlightedElement(MouseElement highlightedElement)
    {
        String methodName = "setHighlightedElement() ";
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

    public void setActor(Actor actor)
    {
        this.actor = actor;
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
