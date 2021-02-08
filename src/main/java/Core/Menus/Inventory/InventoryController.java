package Core.Menus.Inventory;

import Core.Actor;
import Core.Configs.Config;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import static Core.Configs.Config.*;

public class InventoryController
{
    private static String CLASSNAME = "InventoryController/";
    static Actor exchangeInventoryActor;
    static Actor playerActor;
    static InventoryOverlay playerInventoryOverlay;
    static InventoryOverlay otherInventoryOverlay;
    static ShopOverlay shopOverlay;
    static IncubatorOverlay incubatorOverlay;
    WritableImage interactionInventoryImage;

    private static int WIDTH = CAMERA_WIDTH;
    private static int HEIGHT = CAMERA_HEIGHT;
    Point2D playerInventoryPosition = Config.INVENTORY_POSITION;
    Point2D exchangeInventoryPosition = EXCHANGE_INVENTORY_POSITION;
    Point2D shopInterfacePosition = EXCHANGE_INVENTORY_POSITION;

    public InventoryController()
    {
        playerActor = WorldView.getPlayer().getActor();
        playerInventoryOverlay = new InventoryOverlay(WorldView.getPlayer().getActor(), playerInventoryPosition);
        otherInventoryOverlay = new InventoryOverlay(null, exchangeInventoryPosition);
        incubatorOverlay = new IncubatorOverlay(exchangeInventoryActor, INCUBATOR_POSITION);
        shopOverlay = new ShopOverlay(null, shopInterfacePosition);
    }


    public void render(GraphicsContext gc)
    {
        String methodName = "render() ";
        playerInventoryOverlay.render(gc);


        if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_EXCHANGE)
        {
            otherInventoryOverlay.setActor(exchangeInventoryActor);
                    otherInventoryOverlay.render(gc);

        }
        else if(WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_SHOP)
        {
            shopOverlay.setActor(exchangeInventoryActor);
            interactionInventoryImage = shopOverlay.getMenuImage();
            gc.drawImage(interactionInventoryImage, shopInterfacePosition.getX(), shopInterfacePosition.getY());
        }
        else if(WorldViewController.getWorldViewStatus() == WorldViewStatus.INCUBATOR)
        {
            incubatorOverlay.render(gc);
        }

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, boolean isMouseDragged, Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        boolean clickedIntoOverlayPlayer = playerInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayOther = otherInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayShop = shopOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean clickedIntoOverlayIncubator = incubatorOverlay.getSCREEN_AREA().contains(mousePosition);

        //Check if player clicked outside the inventory to exit
        if(isMouseClicked && !clickedIntoOverlayPlayer && WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY)
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
            playerActor.setLastInteraction(currentNanoTime);
        }
        else
            playerInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);

        if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_EXCHANGE)
        {
            if(isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayOther)
            {
                WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }else
            otherInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged,  currentNanoTime);
        }
        else if(WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_SHOP)
        {
            if(isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayShop)
            {
                WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }else
            shopOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }else if(WorldViewController.getWorldViewStatus() == WorldViewStatus.INCUBATOR)
        {
            if(isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayIncubator)
            {
                WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }else
            incubatorOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
        }




    }

    public static void setExchangeInventoryActor(Actor exchangeInventoryActor)
    {
        String methodName = "setExchangeInventoryActor() ";
        InventoryController.exchangeInventoryActor = exchangeInventoryActor;
    }
}
