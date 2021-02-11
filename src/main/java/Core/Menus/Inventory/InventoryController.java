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
        boolean hoversOverlayPlayer = playerInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayOther = otherInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayShop = shopOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayIncubator = incubatorOverlay.getSCREEN_AREA().contains(mousePosition);
        WorldViewStatus worldViewStatus = WorldViewController.getWorldViewStatus();

        if (//Check if a inventory is hovered
                (hoversOverlayPlayer ||
                        (worldViewStatus == INVENTORY_EXCHANGE && hoversOverlayOther) ||
                        (worldViewStatus == INVENTORY_SHOP && hoversOverlayShop) ||
                        (worldViewStatus == INCUBATOR && hoversOverlayIncubator)))
        {
            if (hoversOverlayPlayer)
                playerInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
            else if (worldViewStatus == INVENTORY_EXCHANGE)
                otherInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
            else if (worldViewStatus == INVENTORY_SHOP)
                shopOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
            else if (worldViewStatus == INCUBATOR)
                incubatorOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }
        else if (isMouseClicked)//if no inventory is hovered and clicked, close inventory
        {
            WorldViewController.setWorldViewStatus(WORLD);
            playerActor.setLastInteraction(currentNanoTime);
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
