package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.GameWindow;
import Core.Utilities;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class IncubatorOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "IncubatorOverlay/";
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    private InventoryController controller;
    Image cornerTopLeft;
    Image cornerBtmRight;
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
        String BASE_INPUT_SLOT = "base_input";
        String BASE_OUTPUT_SLOT = "base_output";

        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);
        gc.setGlobalAlpha(1);

        int inputItemFieldX = 100, inputItemFieldY = 200;
        Rectangle2D base_input = new Rectangle2D(SCREEN_POSITION.getX() + inputItemFieldX, SCREEN_POSITION.getY() + inputItemFieldY, 64, 64);
        interfaceElements_list.add(BASE_INPUT_SLOT);
        interfaceElements_Rectangles.add(base_input);
        gc.setFill(COLOR_FONT);
        gc.fillRect(SCREEN_POSITION.getX() + inputItemFieldX, SCREEN_POSITION.getY() + inputItemFieldY, 64, 64);
        gc.setFill(highlightedElement.equals(BASE_INPUT_SLOT) ? COLOR_FONT : COLOR_MARKING);
        gc.fillRect(SCREEN_POSITION.getX() + inputItemFieldX + 2, SCREEN_POSITION.getY() + inputItemFieldY + 2, 64 - 4, 64 - 4);
        if (incubator.getInventory().itemsList.get(0) != null)
            gc.drawImage(incubator.getInventory().itemsList.get(0).getImage(), SCREEN_POSITION.getX() + inputItemFieldX, SCREEN_POSITION.getY() + inputItemFieldY);

        interfaceElements_list.add(BASE_OUTPUT_SLOT);
        int outputItemFieldX = 200, outputItemFieldY = 200;
        Rectangle2D base_output = new Rectangle2D(SCREEN_POSITION.getX() + outputItemFieldX, SCREEN_POSITION.getY() + outputItemFieldY, 64, 64);
        interfaceElements_Rectangles.add(base_output);
        gc.setFill(COLOR_FONT);
        gc.fillRect(SCREEN_POSITION.getX() + outputItemFieldX, SCREEN_POSITION.getY() + outputItemFieldY, 64, 64);
        gc.setFill(highlightedElement.equals(BASE_OUTPUT_SLOT) ? COLOR_FONT : COLOR_MARKING);
        gc.fillRect(SCREEN_POSITION.getX() + outputItemFieldX + 2, SCREEN_POSITION.getY() + outputItemFieldY + 2, 64 - 4, 64 - 4);
        if (incubator.getInventory().itemsList.get(1) != null)
            gc.drawImage(incubator.getInventory().itemsList.get(1).getImage(), SCREEN_POSITION.getX() + outputItemFieldX, SCREEN_POSITION.getY() + outputItemFieldY);


        //Text
        int offsetYFirstLine = 60;
        int dateLength = 200;
        gc.setFill(COLOR_FONT);
        gc.fillText("Incubator", SCREEN_POSITION.getX() + WIDTH - dateLength, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

    }



    public void processMouse(Point2D mousePosition, boolean isMouseClicked, Long currentNanoTime)
    {
        Integer hoveredElement = null;
        for (int i = 0; i < interfaceElements_Rectangles.size(); i++) {
            if (interfaceElements_Rectangles.get(i).contains(mousePosition)) {
                highlightedElement = interfaceElements_list.get(i);
            }
        }

        //if (GameWindow.getSingleton().isMouseMoved() && hoveredElement != null)//Set highlight if mouse moved
        //{
        //    setHighlightedElement(hoveredElement);
        //    GameWindow.getSingleton().setMouseMoved(false);
        //}

        // if (isMouseClicked && hoveredElement != null)//To prevent click of not hovered
        // {
        //     activateHighlightedOption(currentNanoTime);
        // }
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
        Collectible collectibleOnTargetSlot = incubator.getInventory().getItem(interfaceElements_list.indexOf(highlightedElement));
        if (collectibleOnTargetSlot != null) {//swap
            dropped.origin.addItemIdx(collectibleOnTargetSlot, dropped.originIdx);
        }
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

    public Actor getIncubator()
    {
        return incubator;
    }

    public void setIncubator(Actor incubator)
    {
        this.incubator = incubator;
    }
}
