package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.Utilities;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class IncubatorOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "IncubatorOverlay/";
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    Image cornerTopLeft;
    Image cornerBtmRight;
    private InventoryController controller;
    private List<String> interfaceElements_list = new ArrayList<>();
    private List<Rectangle2D> interfaceElements_Rectangles = new ArrayList<>();
    private String highlightedElement = "";
    private Point2D SCREEN_POSITION;
    private Rectangle2D SCREEN_AREA;
    private Actor incubator;

    public IncubatorOverlay(Actor incubator, Point2D SCREEN_POSITION, InventoryController controller)
    {
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        this.incubator = incubator;
        this.controller = controller;
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

        String BASE_INPUT_SLOT = "base_input";
        String BASE_OUTPUT_SLOT = "base_output";
        drawItemSlot(gc, BASE_INPUT_SLOT, 100, 200);
        drawItemSlot(gc, BASE_OUTPUT_SLOT, 200, 200);

        //Text
        int offsetYFirstLine = 60;
        int dateLength = 200;
        gc.setFill(COLOR_FONT);
        gc.fillText("Incubator", SCREEN_POSITION.getX() + WIDTH - dateLength, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

    }

    private void drawItemSlot(GraphicsContext gc, String itemslotName, int slotX, int slotY)
    {
        Rectangle2D base_input = new Rectangle2D(SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY, 64, 64);
        interfaceElements_list.add(itemslotName);
        interfaceElements_Rectangles.add(base_input);
        int inventoryIdx = interfaceElements_list.indexOf(itemslotName);

        gc.setFill(COLOR_FONT);
        gc.fillRect(SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY, 64, 64);
        gc.setFill(highlightedElement.equals(itemslotName) ? COLOR_FONT : COLOR_MARKING);
        gc.fillRect(SCREEN_POSITION.getX() + slotX + 2, SCREEN_POSITION.getY() + slotY + 2, 64 - 4, 64 - 4);
        if (incubator.getInventory().itemsList.get(inventoryIdx) != null)
            gc.drawImage(incubator.getInventory().itemsList.get(inventoryIdx).getImage(), SCREEN_POSITION.getX() + slotX, SCREEN_POSITION.getY() + slotY);
    }


    public void processMouse(Point2D mousePosition)
    {
        for (int i = 0; i < interfaceElements_Rectangles.size(); i++) {
            if (interfaceElements_Rectangles.get(i).contains(mousePosition)) {
                highlightedElement = interfaceElements_list.get(i);
            }
        }

        //TODO click events

    }

    private void activateHighlightedOption(Long currentNanoTime)
    {
        String methodName = "activateHighlightedOption() ";
        Collectible collectible = null;
        Actor player = WorldView.getPlayer().getActor();

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
        Collectible collectibleToDrop = dropped.collectible;
        dropped.previousInventory.addItemIdx(//Swap item if existse
                incubator.getInventory().getItem(interfaceElements_list.indexOf(highlightedElement)),
                dropped.previousIdx);
        incubator.getInventory().addItemIdx(collectibleToDrop, interfaceElements_list.indexOf(highlightedElement));
        controller.setDragAndDropItem(null);
    }

    @Override
    public void dragCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (incubator.getInventory().itemsList.get(interfaceElements_list.indexOf(highlightedElement)) != null && controller.getDragAndDropItem() == null) {
            Collectible collectible;
            collectible = incubator.getInventory().itemsList.get(interfaceElements_list.indexOf(highlightedElement));
            incubator.getInventory().removeItem(collectible);
            controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible, incubator.getInventory(), interfaceElements_list.indexOf(highlightedElement)));
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
