package Core.Menus.StatusOverlay;

import Core.Actor;
import Core.CollectibleStack;
import Core.GameTime.Clock;
import Core.GameVariables;
import Core.GameWindow;
import Core.Menus.Inventory.*;
import Core.WorldView.WorldView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.Set;

import static Core.Configs.Config.COLOR_FONT;
import static Core.Configs.Config.IMAGE_DIRECTORY_PATH;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;
import static Core.Menus.Inventory.MouseInteractionType.DRAG;

public class CollectibleSlotOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "CollectibleSlotOverlay/";
    private final int WIDTH = 100;
    private final int HEIGHT = 100;
    private final Point2D SCREENPOSITION;
    private final Rectangle2D SCREEN_AREA;
    private Inventory inventory;
    private final MouseElementsContainer mouseElements = new MouseElementsContainer();
    Image frameImage;
    private MouseElement highlightedElement = null;
    private final InventoryController controller;

    public CollectibleSlotOverlay(Point2D position, InventoryController controller)
    {
        SCREENPOSITION = position;
        SCREEN_AREA = new Rectangle2D(SCREENPOSITION.getX(), SCREENPOSITION.getY(), WIDTH, HEIGHT);
        this.controller = controller;
        inventory = new Inventory(WorldView.getPlayer().getActor());
        Set<MouseInteractionType> clickAndDrag = new HashSet<>();
        clickAndDrag.add(CLICK);
        clickAndDrag.add(DRAG);
        int numberColumns = 1;
        int itemTileWidth = 64;
        int spaceBetweenTiles = 0;
        int initialOffsetX = 0;
        int slotNumber = 0;
        for (int i = 0; i < numberColumns; i++) {
            //Rectangle
            int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
            Rectangle rectangle2D = new Rectangle(SCREENPOSITION.getX() + slotX + 2, SCREENPOSITION.getY(), itemTileWidth - 4, itemTileWidth - 4);
            MouseElement slot = new MouseElement(rectangle2D, Integer.valueOf(slotNumber).toString(), clickAndDrag);
            mouseElements.add(slot);
            slotNumber++;
        }
    }

    public void render(GraphicsContext gc)
    {
        String methodName = "render() ";
        //gc.drawImage(frameImage, SCREENPOSITION.getX(), SCREENPOSITION.getY());
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_FONT);
        gc.fillRect(SCREENPOSITION.getX(), SCREENPOSITION.getY(), WIDTH, HEIGHT);
        gc.setGlobalAlpha(1);
    }

    public void dropCollectible(DragAndDropItem dropped)
    {
        Actor player = GameVariables.getPlayer().getActor();
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG)) {
            CollectibleStack collectibleToDrop = dropped.collectible;
            CollectibleStack collectibleAtTargetSlot = player.getInventory().getItem(mouseElements.indexOf(highlightedElement));
            dropped.previousInventory.addItemIdx(//Swap item if exists
                    collectibleAtTargetSlot,
                    dropped.previousIdx);
            player.getInventory().addItemIdx(collectibleToDrop, mouseElements.indexOf(highlightedElement));
        }
        else {
            dropped.previousInventory.addItemIdx(//Back to previous Inventory
                    dropped.collectible,
                    dropped.previousIdx);
        }
        controller.setDragAndDropItem(null);
    }

    @Override
    public void dragCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        Actor player = GameVariables.getPlayer().getActor();
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG)) {
            CollectibleStack tocheck = player.getInventory().getItem(mouseElements.indexOf(highlightedElement));
            if (!tocheck.isEmpty() && controller.getDragAndDropItem() == null) {
                CollectibleStack collectible = player.getInventory().getItem(mouseElements.indexOf(highlightedElement));
                player.getInventory().removeItem(collectible);
                controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible, player.getInventory(), mouseElements.indexOf(highlightedElement)));
            }
        }
    }

    public void processMouse(Point2D mousePosition)
    {
        for (int i = 0; i < mouseElements.size(); i++)
        {
            if (mouseElements.get(i).getPosition().contains(mousePosition))
            {
                highlightedElement = mouseElements.get(i);
            }
        }

        if (GameWindow.getSingleton().isMouseClicked())
        {
           // if (highlightedElement.reactiveTypes.contains(CLICK))
           //     activateHighlightedOption();
        }

    }

    public void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (controller.getDragAndDropItem() != null) {
            controller.getDragAndDropItem().setPosition(new Point2D(mousePosition.getX(), mousePosition.getY()));
        }
    }

    public Rectangle2D getSCREEN_AREA()
    {
        return SCREEN_AREA;
    }
}
