package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.Enums.CollectableType;
import Core.GameWindow;
import Core.Utilities;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;
import static Core.Menus.Inventory.MouseInteractionType.DRAG;

public class IncubatorOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "IncubatorOverlay/";
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    Image cornerTopLeft;
    Image cornerBtmRight;
    private InventoryController controller;
    private MouseElementsContainer mouseElements = new MouseElementsContainer();
    private MouseElement highlightedElement = null;
    private Point2D SCREEN_POSITION;
    private Rectangle2D SCREEN_AREA;
    private Actor incubator;
    final String BASE_INPUT_SLOT = "base_input";
    final String BASE_OUTPUT_SLOT = "base_output";
    final String CONVERT_BUTTON_ID = "CONVERT";

    public IncubatorOverlay(Actor incubator, Point2D SCREEN_POSITION, InventoryController controller)
    {
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        this.incubator = incubator;
        this.controller = controller;
        init();
    }

    private void init()
    {
        MouseElement convertBtn = new MouseElement(new Rectangle2D(SCREEN_POSITION.getX() + 150,SCREEN_POSITION.getY() + 300, 150, 64), CONVERT_BUTTON_ID, CLICK);
        mouseElements.add(convertBtn);

        Rectangle2D base_input = new Rectangle2D(SCREEN_POSITION.getX() + 100, SCREEN_POSITION.getY() + 200, 64, 64);
        mouseElements.add(new MouseElement(base_input, BASE_INPUT_SLOT, DRAG));

        Rectangle2D base_output = new Rectangle2D(SCREEN_POSITION.getX() + 200, SCREEN_POSITION.getY() + 200, 64, 64);
        mouseElements.add(new MouseElement(base_output, BASE_OUTPUT_SLOT, DRAG));
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);
        gc.setGlobalAlpha(1);

        drawItemSlot(gc, BASE_INPUT_SLOT);
        drawItemSlot(gc, BASE_OUTPUT_SLOT);

        gc.setFill(COLOR_RED);
        Rectangle2D convertButton = mouseElements.get(CONVERT_BUTTON_ID).position;
        gc.fillRect(convertButton.getMinX(), convertButton.getMinY(), convertButton.getWidth(), convertButton.getHeight());

        //Text
        int offsetYFirstLine = 60;
        int dateLength = 200;
        gc.setFill(COLOR_FONT);
        gc.fillText("Incubator", SCREEN_POSITION.getX() + WIDTH - dateLength, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

    }

    private void drawItemSlot(GraphicsContext gc, String itemslotName)
    {
        MouseElement mouseElement = mouseElements.get(itemslotName);
        int inventoryIdx = mouseElements.indexOf(mouseElement);
        Rectangle2D rectangle2D = mouseElement.position;

        gc.setFill(COLOR_FONT);
        gc.fillRect(rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight());
        gc.setFill(highlightedElement == mouseElement ? COLOR_FONT : COLOR_MARKING);
        gc.fillRect(rectangle2D.getMinX() +2, rectangle2D.getMinY() +2, rectangle2D.getWidth()-4, rectangle2D.getHeight()-4);
        if (incubator.getInventory().itemsList.get(inventoryIdx) != null)
            gc.drawImage(incubator.getInventory().itemsList.get(inventoryIdx).getImage(), rectangle2D.getMinX(), rectangle2D.getMinY());
    }


    public void processMouse(Point2D mousePosition)
    {
        for (int i = 0; i < mouseElements.size(); i++) {
            if (mouseElements.get(i).getPosition().contains(mousePosition)) {
                highlightedElement = mouseElements.get(i);
            }
        }

        if(GameWindow.getSingleton().isMouseClicked())
        {
            if(highlightedElement.reactiveTypes.contains(CLICK))
                activateHighlightedOption();
        }

    }

    private void activateHighlightedOption()
    {
        String methodName = "activateHighlightedOption() ";
        switch (highlightedElement.identifier)
        {
            case CONVERT_BUTTON_ID:
                convertItem();
            default:
        }

    }

    private void convertItem()
    {
        Collectible inputSlotItem = incubator.getInventory().getItem(mouseElements.indexOf(mouseElements.get(BASE_INPUT_SLOT)));
        Collectible outputSlotItem = incubator.getInventory().getItem(mouseElements.indexOf(mouseElements.get(BASE_OUTPUT_SLOT)));

        Collectible converted = Collectible.createCollectible("actorData/collectibles/bacteria/bacteria_crafted", "Electric Bacteria", "electric");

        if(inputSlotItem != null && inputSlotItem.getType() == CollectableType.FOOD && outputSlotItem == null)
        {
            incubator.getInventory().addItemIdx(converted, mouseElements.indexOf(mouseElements.get(BASE_OUTPUT_SLOT)));
            incubator.getInventory().removeItem(inputSlotItem);
        }
    }

    public void setSCREEN_POSITION(Point2D SCREEN_POSITION)
    {
        this.SCREEN_POSITION = SCREEN_POSITION;
    }

    public Rectangle2D getSCREEN_AREA()
    {
        return SCREEN_AREA;
    }

    @Override
    public void dropCollectible(DragAndDropItem dropped)
    {
        if(highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG))
        {
            Collectible collectibleToDrop = dropped.collectible;
            dropped.previousInventory.addItemIdx(//Swap item if exists
                    incubator.getInventory().getItem(mouseElements.indexOf(highlightedElement)),
                    dropped.previousIdx);
            incubator.getInventory().addItemIdx(collectibleToDrop, mouseElements.indexOf(highlightedElement));
        }
        else
        {
            dropped.previousInventory.addItemIdx(//Back to previous Inventory
                    dropped.collectible,
                    dropped.previousIdx);
        }
        controller.setDragAndDropItem(null);
    }

    @Override
    public void dragCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if(highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG))
        {
        if (incubator.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement)) != null && controller.getDragAndDropItem() == null) {
            Collectible collectible;
            collectible = incubator.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement));
            incubator.getInventory().removeItem(collectible);
            controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible, incubator.getInventory(), mouseElements.indexOf(highlightedElement)));
        }
        }
    }

    @Override
    public void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (controller.getDragAndDropItem() != null) {
            controller.getDragAndDropItem().setPosition(new Point2D(mousePosition.getX(), mousePosition.getY()));
        }
    }

    public void setIncubator(Actor incubator)
    {
        this.incubator = incubator;
    }
}
