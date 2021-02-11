package Core.Menus.Inventory;

import Core.Actor;
import Core.Configs.Config;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.*;

public class InventoryController
{
    static Actor exchangeInventoryActor;
    static Actor playerActor;
    static InventoryOverlay playerInventoryOverlay;
    static InventoryOverlay otherInventoryOverlay;
    static ShopOverlay shopOverlay;
    static IncubatorOverlay incubatorOverlay;
    private static String CLASSNAME = "InventoryController/";
    private static int WIDTH = CAMERA_WIDTH;
    private static int HEIGHT = CAMERA_HEIGHT;
    WritableImage interactionInventoryImage;
    Point2D playerInventoryPosition = Config.INVENTORY_POSITION;
    Point2D exchangeInventoryPosition = EXCHANGE_INVENTORY_POSITION;
    Point2D shopInterfacePosition = EXCHANGE_INVENTORY_POSITION;
    private DragAndDropItem dragAndDropItem;

    public InventoryController()
    {
        playerActor = WorldView.getPlayer().getActor();
        playerInventoryOverlay = new InventoryOverlay(WorldView.getPlayer().getActor(), playerInventoryPosition, this);
        otherInventoryOverlay = new InventoryOverlay(null, exchangeInventoryPosition, this);
        incubatorOverlay = new IncubatorOverlay(exchangeInventoryActor, INCUBATOR_POSITION, this);
        shopOverlay = new ShopOverlay(null, shopInterfacePosition, this);
    }

    public static void setExchangeInventoryActor(Actor exchangeInventoryActor)
    {
        String methodName = "setExchangeInventoryActor() ";
        InventoryController.exchangeInventoryActor = exchangeInventoryActor;
    }

    public void render(GraphicsContext gc)
    {
        String methodName = "render() ";
        playerInventoryOverlay.render(gc);


        if (WorldViewController.getWorldViewStatus() == INVENTORY_EXCHANGE)
        {
            otherInventoryOverlay.setActor(exchangeInventoryActor);
            otherInventoryOverlay.render(gc);

        }
        else if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_SHOP)
        {
            shopOverlay.setActor(exchangeInventoryActor);
            interactionInventoryImage = shopOverlay.getMenuImage();
            gc.drawImage(interactionInventoryImage, shopInterfacePosition.getX(), shopInterfacePosition.getY());
        }
        else if (WorldViewController.getWorldViewStatus() == INCUBATOR)
        {
            incubatorOverlay.render(gc);
        }
        if (dragAndDropItem != null)
            gc.drawImage(dragAndDropItem.collectible.getImage(), dragAndDropItem.screenPosition.getX(), dragAndDropItem.screenPosition.getY());

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, boolean isMouseDragged, Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        boolean clickedIntoOverlayPlayer = playerInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayOther = otherInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayShop = shopOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayIncubator = incubatorOverlay.getSCREEN_AREA().contains(mousePosition);

        //Check if player clicked outside the inventory to exit
        if (isMouseClicked && !clickedIntoOverlayPlayer && WorldViewController.getWorldViewStatus() == INVENTORY)
        {
            WorldViewController.setWorldViewStatus(WORLD);
            playerActor.setLastInteraction(currentNanoTime);
        }
        else if (clickedIntoOverlayPlayer)
            playerInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);

        if (WorldViewController.getWorldViewStatus() == INVENTORY_EXCHANGE)
        {
            if (isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayOther)
            {
                WorldViewController.setWorldViewStatus(WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }
            else
                otherInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
        }
        else if (WorldViewController.getWorldViewStatus() == INVENTORY_SHOP)
        {
            if (isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayShop)
            {
                WorldViewController.setWorldViewStatus(WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }
            else
                shopOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }
        else if (WorldViewController.getWorldViewStatus() == INCUBATOR)
        {
            if (isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayIncubator)
            {
                WorldViewController.setWorldViewStatus(WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }
            else
                incubatorOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }

    }

    public DragAndDropItem getDragAndDropItem()
    {
        return dragAndDropItem;
    }

    public void setDragAndDropItem(DragAndDropItem dragAndDropItem)
    {
        this.dragAndDropItem = dragAndDropItem;
    }
}
