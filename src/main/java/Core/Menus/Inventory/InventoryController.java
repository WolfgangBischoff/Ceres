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
    Canvas canvas;
    GraphicsContext graphicsContext;
    static InventoryOverlay playerInventoryOverlay;
    static InventoryOverlay otherInventoryOverlay;
    static ShopOverlay shopOverlay;
    static IncubatorOverlay incubatorOverlay= new IncubatorOverlay(INCUBATOR_POSITION);
    WritableImage playerInventory;
    WritableImage interactionInventoryImage;
    static Actor exchangeInventoryActor;
    static Actor playerActor;
    private static int WIDTH = CAMERA_WIDTH;
    private static int HEIGHT = CAMERA_HEIGHT;
    Point2D playerInventoryPosition = Config.INVENTORY_POSITION;
    Point2D exchangeInventoryPosition = EXCHANGE_INVENTORY_POSITION;
    Point2D shopInterfacePosition = EXCHANGE_INVENTORY_POSITION;

    public InventoryController()
    {
        canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        playerInventoryOverlay = new InventoryOverlay(WorldView.getPlayer().getActor(), playerInventoryPosition);
        playerActor = WorldView.getPlayer().getActor();
        otherInventoryOverlay = new InventoryOverlay(null, exchangeInventoryPosition);
        shopOverlay = new ShopOverlay(null, shopInterfacePosition);
    }


    public WritableImage render(GraphicsContext gc)
    {
        String methodName = "getMenuImage() ";
        //gc.clearRect(0, 0, WIDTH, HEIGHT);
        playerInventory = playerInventoryOverlay.getMenuImage();
        gc.drawImage(playerInventory, playerInventoryPosition.getX(), playerInventoryPosition.getY());

        if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_EXCHANGE)
        {
            otherInventoryOverlay.setActor(exchangeInventoryActor);
            interactionInventoryImage = otherInventoryOverlay.getMenuImage();
            gc.drawImage(interactionInventoryImage, exchangeInventoryPosition.getX(), exchangeInventoryPosition.getY());
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

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        return canvas.snapshot(transparency, null);
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
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
        }else
            playerInventoryOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);

        if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_EXCHANGE)
        {
            if(isMouseClicked && !clickedIntoOverlayPlayer && !clickedIntoOverlayOther)
            {
                WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                playerActor.setLastInteraction(currentNanoTime);
            }else
            otherInventoryOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
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
