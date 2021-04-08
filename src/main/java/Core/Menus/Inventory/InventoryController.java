package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.Configs.Config;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static Core.Configs.Config.*;
import static Core.Configs.Config.FONT_ORBITRON_12;
import static Core.WorldView.WorldViewStatus.*;
import static java.lang.Math.max;

public class InventoryController
{
    static Actor exchangeInventoryActor;
    static Actor playerActor;
    static InventoryOverlay playerInventoryOverlay;
    static InventoryOverlay otherInventoryOverlay;
    static ShopOverlay shopOverlay;
    static IncubatorOverlay incubatorOverlay;
    private static final String CLASSNAME = "InventoryController/";
    WritableImage interactionInventoryImage;
    Point2D playerInventoryPosition = Config.INVENTORY_POSITION;
    Point2D exchangeInventoryPosition = EXCHANGE_INVENTORY_POSITION;
    Point2D shopInterfacePosition = EXCHANGE_INVENTORY_POSITION;
    private DragAndDropItem dragAndDropItem;
    MouseElement tooltipElement = null;
    Collectible tooltippedCollectible = null;

    public InventoryController()
    {
        playerActor = WorldView.getPlayer().getActor();
        playerInventoryOverlay = new InventoryOverlay(WorldView.getPlayer().getActor(), playerInventoryPosition, this);
        otherInventoryOverlay = new InventoryOverlay(null, exchangeInventoryPosition, this);
        incubatorOverlay = new IncubatorOverlay(null, INCUBATOR_POSITION, this);
        shopOverlay = new ShopOverlay(null, shopInterfacePosition, this);
    }

    public static void setExchangeInventoryActor(Actor exchangeInventoryActor)
    {
        String methodName = "setExchangeInventoryActor() ";
        InventoryController.exchangeInventoryActor = exchangeInventoryActor;
    }

    public void render(GraphicsContext gc, long currentRenderTime)
    {
        String methodName = "render() ";
        playerInventoryOverlay.render(gc);


        if (WorldViewController.getWorldViewStatus() == INVENTORY_EXCHANGE) {
            otherInventoryOverlay.setActor(exchangeInventoryActor);
            otherInventoryOverlay.render(gc);

        }
        else if (WorldViewController.getWorldViewStatus() == WorldViewStatus.INVENTORY_SHOP) {
            shopOverlay.setActor(exchangeInventoryActor);
            shopOverlay.render(gc);
        }
        else if (WorldViewController.getWorldViewStatus() == INCUBATOR) {
            incubatorOverlay.setActor(exchangeInventoryActor);
            incubatorOverlay.render(gc, currentRenderTime);
        }
        if (dragAndDropItem != null)
            gc.drawImage(dragAndDropItem.collectible.getImage(), dragAndDropItem.screenPosition.getX(), dragAndDropItem.screenPosition.getY());

        if (tooltipElement != null && tooltippedCollectible != null) {
            drawTooltip(gc);
        }

    }

    private void drawTooltip(GraphicsContext gc)
    {
        double tooltipWidth = max(300, Utilities.calcStringWidth(FONT_ORBITRON_20, tooltippedCollectible.getIngameName() + 2));
        List<String> lines = Utilities.wrapText(tooltippedCollectible.getDescription(), FONT_ORBITRON_12, tooltipWidth);

        double collectibleHeadlineHeight = (FONT_ORBITRON_20.getSize() * 1.5);
        double tooltipHeight = collectibleHeadlineHeight + (FONT_ORBITRON_12.getSize() * lines.size() + 3);
        Rectangle tooltipRect = (Rectangle) tooltipElement.getPosition();
        gc.setFill(COLOR_GREEN);
        gc.fillRect(tooltipRect.getX() + 50, tooltipRect.getY() + 50, tooltipWidth, tooltipHeight);
        gc.setFill(COLOR_BACKGROUND_GREY);
        gc.fillRect(tooltipRect.getX()  + 50 + 2, tooltipRect.getY() + 50 + 2, tooltipWidth - 4, tooltipHeight - 4);

        gc.setFill(COLOR_FONT);
        gc.setFont(FONT_ORBITRON_20);
        gc.fillText(tooltippedCollectible.getIngameName(),
                tooltipRect.getX()  + 50 + 5,
                tooltipRect.getY() + 50 + gc.getFont().getSize() + 3);
        gc.setFont(FONT_ORBITRON_12);
        for (int l = 0; l < lines.size(); l++) {
            gc.fillText(lines.get(l),
                    tooltipRect.getX()  + 50 + 5,
                    tooltipRect.getY() + 55 + collectibleHeadlineHeight + FONT_ORBITRON_12.getSize() * l + 3);
        }
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, boolean isMouseDragged, Long currentNanoTime)
    {
        String methodName = "processMouse() ";
        tooltipElement = null;
        tooltippedCollectible = null;


        boolean hoversOverlayPlayer = playerInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayOther = otherInventoryOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayShop = shopOverlay.getSCREEN_AREA().contains(mousePosition);
        boolean hoversOverlayIncubator = incubatorOverlay.getSCREEN_AREA().contains(mousePosition);
        DragAndDropOverlay hoveredOverlay = null;
        WorldViewStatus worldViewStatus = WorldViewController.getWorldViewStatus();
        if (playerInventoryOverlay.getSCREEN_AREA().contains(mousePosition))
            hoveredOverlay = playerInventoryOverlay;
        else if (hoversOverlayOther && worldViewStatus == INVENTORY_EXCHANGE)
            hoveredOverlay = otherInventoryOverlay;
        else if (hoversOverlayIncubator && worldViewStatus == INCUBATOR)
            hoveredOverlay = incubatorOverlay;

        if (//Check if a inventory is hovered
                (hoversOverlayPlayer ||
                        (worldViewStatus == INVENTORY_EXCHANGE && hoversOverlayOther) ||
                        (worldViewStatus == INVENTORY_SHOP && hoversOverlayShop) ||
                        (worldViewStatus == INCUBATOR && hoversOverlayIncubator))) {

            if (hoversOverlayPlayer) {
                playerInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
            }
            else if (worldViewStatus == INVENTORY_EXCHANGE) {
                otherInventoryOverlay.processMouse(mousePosition, isMouseClicked, isMouseDragged, currentNanoTime);
            }
            else if (worldViewStatus == INVENTORY_SHOP)
                shopOverlay.processMouse(mousePosition, isMouseClicked, currentNanoTime);
            else if (worldViewStatus == INCUBATOR)
                incubatorOverlay.processMouse(mousePosition);

            if (hoveredOverlay != null) {
                if (isMouseDragged && getDragAndDropItem() == null)//Drag Item
                {
                    hoveredOverlay.dragCollectible(currentNanoTime, mousePosition);
                }
                else if (isMouseDragged && getDragAndDropItem() != null)//Update Dragged Item
                {
                    hoveredOverlay.updateDraggedCollectible(currentNanoTime, mousePosition);
                }
                else if (getDragAndDropItem() != null)//Drop Item
                {
                    hoveredOverlay.dropCollectible(getDragAndDropItem());
                }
            }
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

    public Collectible getTooltippedCollectible()
    {
        return tooltippedCollectible;
    }

    public void setTooltippedCollectible(Collectible tooltippedCollectible)
    {
        this.tooltippedCollectible = tooltippedCollectible;
    }

    public MouseElement getTooltipElement()
    {
        return tooltipElement;
    }

    public void setTooltipElement(MouseElement tooltipElement)
    {
        this.tooltipElement = tooltipElement;
    }
}
