package Core.Menus.Inventory;

import Core.*;
import Core.Enums.CollectableType;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;
import static Core.Menus.Inventory.MouseInteractionType.DRAG;
import static Core.WorldView.WorldViewStatus.*;

public class InventoryOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "InventoryOverlay/";
    private static int WIDTH = INVENTORY_WIDTH;
    private static int HEIGHT = INVENTORY_HEIGHT;
    final String CANCEL_BUTTON_ID = "CANCEL";
    private final InventoryController controller;
    private final Rectangle2D SCREEN_AREA;
    Image cornerTopLeft;
    Image cornerBtmRight;
    Image cancelButton;
    private Actor actor;
    private MouseElementsContainer mouseElements = new MouseElementsContainer();
    private MouseElement highlightedElement = null;
    private MouseElement tooltipElement = null;
    private Point2D SCREEN_POSITION;

    public InventoryOverlay(Actor actor, Point2D SCREEN_POSITION, InventoryController controller)
    {
        this.actor = actor;
        this.controller = controller;
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        cancelButton = Utilities.readImage(IMAGE_DIRECTORY_PATH + "interface/cancelButton.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        init();
    }

    public static void setWIDTH(int WIDTH)
    {
        InventoryOverlay.WIDTH = WIDTH;
    }

    public static void setHEIGHT(int HEIGHT)
    {
        InventoryOverlay.HEIGHT = HEIGHT;
    }

    private void init()
    {
        Set<MouseInteractionType> clickAndDrag = new HashSet<>();
        clickAndDrag.add(CLICK);
        clickAndDrag.add(DRAG);
        int itemTileWidth = 64;
        int numberColumns = 5;
        int numberRows = 6;
        int spaceBetweenTiles = 10;
        int initialOffsetX = (WIDTH - (numberColumns * itemTileWidth + (numberColumns - 1) * spaceBetweenTiles)) / 2; //Centered
        int initialOffsetY = 75;
        int slotNumber = 0;
        for (int y = 0; y < numberRows; y++) {
            int slotY = y * (itemTileWidth + spaceBetweenTiles) + initialOffsetY;
            for (int i = 0; i < numberColumns; i++) {
                //Rectangle
                int slotX = i * (itemTileWidth + spaceBetweenTiles) + initialOffsetX;
                Rectangle2D rectangle2D = new Rectangle2D(SCREEN_POSITION.getX() + slotX + 2, SCREEN_POSITION.getY() + slotY + 2, itemTileWidth - 4, itemTileWidth - 4);
                MouseElement convertBtn = new MouseElement(rectangle2D, Integer.valueOf(slotNumber).toString(), clickAndDrag);
                mouseElements.add(convertBtn);
                slotNumber++;
            }
        }

        Rectangle2D cancelButtonRect = new Rectangle2D(SCREEN_POSITION.getX() + WIDTH - cancelButton.getWidth(), SCREEN_POSITION.getY(), 64, 64);
        mouseElements.add(new MouseElement(cancelButtonRect, CANCEL_BUTTON_ID, CLICK));
    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";
        Color marking = COLOR_MARKING;
        Color font = COLOR_FONT;
        Color darkRed = Color.hsb(0, 0.23, 0.70);


        //Background
        gc.setGlobalAlpha(0.8);
        gc.setFill(COLOR_BACKGROUND_BLUE);
        int backgroundOffsetX = 16, backgroundOffsetY = 10;
        gc.fillRect(backgroundOffsetX + SCREEN_POSITION.getX(), SCREEN_POSITION.getY() + backgroundOffsetY, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetY * 2);

        //Item Slots
        gc.setGlobalAlpha(1);
        int itemTileWidth = 64;
        int numberColumns = 5;
        int numberRows = 6;
        int spaceBetweenTiles = 10;
        int initialOffsetX = (WIDTH - (numberColumns * itemTileWidth + (numberColumns - 1) * spaceBetweenTiles)) / 2; //Centered
        int itemSlotNumber = 0;
        int slotNumber = 0;
        for (int y = 0; y < numberRows; y++) {
            for (int i = 0; i < numberColumns; i++) {
                Rectangle2D currentRect = mouseElements.get(slotNumber).position;
                gc.setFill(font);
                gc.fillRect(currentRect.getMinX(), currentRect.getMinY(), currentRect.getWidth(), currentRect.getHeight());
                gc.setFill(marking);
                Rectangle2D rectangle2D = new Rectangle2D(currentRect.getMinX() + 2, currentRect.getMinY() + 2, currentRect.getWidth() - 4, currentRect.getHeight() - 4);

                //Highlighting
                if (mouseElements.indexOf(highlightedElement) == slotNumber)
                    gc.setFill(font);
                else
                    gc.setFill(marking);
                gc.fillRect(rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight());
                slotNumber++;

                //Item slot images
                Collectible current = null;
                if (itemSlotNumber < actor.getInventory().itemsList.size())
                    current = actor.getInventory().itemsList.get(itemSlotNumber);
                if (current != null) {
                    gc.drawImage(current.getImage(), currentRect.getMinX() -2, currentRect.getMinY()-2);
                    //Stolen sign
                    if (GameVariables.getStolenCollectibles().contains(current)) {
                        gc.setFill(darkRed);
                        gc.fillOval(currentRect.getMinX() + 44, currentRect.getMinY() + 44, 16, 16);
                        gc.setFill(COLOR_RED);
                        gc.fillOval(currentRect.getMinX() + 46, currentRect.getMinY() + 46, 12, 12);
                    }
                }
                itemSlotNumber++;
            }
        }

        //Text
        int offsetYFirstLine = 60;
        gc.setFill(font);
        gc.setFont(FONT_ESTROG_20);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(actor.getActorInGameName().toUpperCase(), SCREEN_POSITION.getX() + initialOffsetX, SCREEN_POSITION.getY() + offsetYFirstLine);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());
        Rectangle2D cancelButtonRect = mouseElements.get(CANCEL_BUTTON_ID).position;
        gc.drawImage(cancelButton, cancelButtonRect.getMinX(), cancelButtonRect.getMinY());

        //Tooltip
        Collectible tooltippedCollectible = null;
        if (mouseElements.indexOf(tooltipElement) >= 0 && mouseElements.indexOf(tooltipElement) < actor.getInventory().itemsList.size())
            tooltippedCollectible = actor.getInventory().itemsList.get(mouseElements.indexOf(tooltipElement));
        if (tooltipElement != null && tooltippedCollectible != null) {
            int tooltipWidth = 300;
            List<String> lines = Utilities.wrapText(tooltippedCollectible.getDescription(), FONT_ORBITRON_12, tooltipWidth);

            double collectibleHeadlineHeight = (FONT_ORBITRON_20.getSize() * 1.5);
            double tooltipHeight = collectibleHeadlineHeight + (FONT_ORBITRON_12.getSize() * lines.size() + 3);
            gc.setFill(COLOR_GREEN);
            gc.fillRect(tooltipElement.position.getMinX() + 50, tooltipElement.position.getMinY() + 50, tooltipWidth, tooltipHeight);
            gc.setFill(COLOR_BACKGROUND_GREY);
            gc.fillRect(tooltipElement.position.getMinX() + 50 + 2, tooltipElement.position.getMinY() + 50 + 2, tooltipWidth - 4, tooltipHeight - 4);

            gc.setFill(font);
            gc.setFont(FONT_ORBITRON_20);
            gc.fillText(tooltippedCollectible.getIngameName(),
                    tooltipElement.position.getMinX() + 50 + 5,
                    tooltipElement.position.getMinY() + 50 + gc.getFont().getSize() + 3);
            gc.setFont(FONT_ORBITRON_12);

            for (int l = 0; l < lines.size(); l++) {
                gc.fillText(lines.get(l),
                        tooltipElement.position.getMinX() + 50 + 5,
                        tooltipElement.position.getMinY() + 55 + collectibleHeadlineHeight + FONT_ORBITRON_12.getSize() * l + 3);
            }

        }

        if(false)
        {//Mouse visible
            Point2D m = GameWindow.getSingleton().getMousePosition();
            Circle mouseClickSpace = new Circle(m.getX(), m.getY(), 10);
            gc.fillOval( mouseClickSpace.getCenterX() - mouseClickSpace.getRadius(), mouseClickSpace.getCenterY() - mouseClickSpace.getRadius(), mouseClickSpace.getRadius() * 2, mouseClickSpace.getRadius() * 2);
        }

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked, boolean isMouseDragged, Long currentNanoTime)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        MouseElement hoveredElement = null;
        tooltipElement = null;
        for (int i = 0; i < mouseElements.size(); i++) {
            if (mouseElements.get(i).getPosition().contains(mousePosition)) {
                hoveredElement = mouseElements.get(i);
                if (hoveredElement == highlightedElement)
                    tooltipElement = mouseElements.get(i);
            }
        }
        //System.out.println(CLASSNAME + mousePosition.getX() + " " + mousePosition.getY());


        if ((GameWindow.getSingleton().isMouseMoved()) && hoveredElement != null)//Set highlight if mouse moved
        {
            setHighlightedElement(hoveredElement);
            GameWindow.getSingleton().setMouseMoved(false);
        }

        if (isMouseClicked && hoveredElement != null)//To prevent click of not hovered
        {
            activateHighlightedOption();
        }

    }

    public void removeItem(Collectible collectible)
    {
        actor.getInventory().removeItem(collectible);
    }

    public void dropCollectible(DragAndDropItem dropped)
    {
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG)) {
            Collectible collectibleToDrop = dropped.collectible;
            Collectible collectibleAtTargetSlot = actor.getInventory().getItem(mouseElements.indexOf(highlightedElement));
            dropped.previousInventory.addItemIdx(//Swap item if exists
                    collectibleAtTargetSlot,
                    dropped.previousIdx);
            actor.getInventory().addItemIdx(collectibleToDrop, mouseElements.indexOf(highlightedElement));
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
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG)) {
            if (actor.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement)) != null && controller.getDragAndDropItem() == null) {
                Collectible collectible;
                collectible = actor.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement));
                actor.getInventory().removeItem(collectible);
                controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible, actor.getInventory(), mouseElements.indexOf(highlightedElement)));
            }
        }
    }

    public void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (controller.getDragAndDropItem() != null) {
            controller.getDragAndDropItem().setPosition(new Point2D(mousePosition.getX(), mousePosition.getY()));
        }
    }

    private void activateHighlightedOption()
    {
        String methodName = "activateHighlightedOption() ";
        Collectible collectible = null;
        int itemIdx = mouseElements.indexOf(highlightedElement);
        if (actor.getInventory().itemsList.size() > itemIdx && itemIdx >= 0)
            collectible = actor.getInventory().itemsList.get(itemIdx);


        if (WorldViewController.getWorldViewStatus() == INVENTORY_EXCHANGE) {
            switch (highlightedElement.identifier) {
                case CANCEL_BUTTON_ID:
                    if (InventoryController.playerInventoryOverlay == this)
                        WorldViewController.setWorldViewStatus(WORLD);
                    else
                        WorldViewController.setWorldViewStatus(INVENTORY);
                    break;
                default:
            }

            if (collectible != null) {
                //check from which inventory to which inventory we exchange
                if (InventoryController.playerInventoryOverlay == this) {
                    InventoryController.exchangeInventoryActor.getInventory().addItemNextSlot(collectible);
                    InventoryController.playerActor.getInventory().removeItem(collectible);
                }
                else if (InventoryController.otherInventoryOverlay == this) {
                    InventoryController.playerActor.getInventory().addItemNextSlot(collectible);
                    InventoryController.exchangeInventoryActor.getInventory().removeItem(collectible);
                }
            }

        }
        else if (WorldViewController.getWorldViewStatus() == INVENTORY_SHOP || WorldViewController.getWorldViewStatus() == INCUBATOR) {
            switch (highlightedElement.identifier) {
                case CANCEL_BUTTON_ID:
                    WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                    break;
                default:
            }
        }
        else if (WorldViewController.getWorldViewStatus() == INVENTORY) {
            switch (highlightedElement.identifier) {
                case CANCEL_BUTTON_ID:
                    WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                    break;
                default:
            }

            if (collectible != null
                    && collectible.getType() == CollectableType.FOOD) {
                System.out.println(CLASSNAME + methodName + "You ate " + collectible.getIngameName());
                GameVariables.addHunger(collectible.getBaseValue());
                actor.getInventory().removeItem(collectible);
                GameVariables.getStolenCollectibles().remove(collectible);
            }
        }

    }

    public void setHighlightedElement(MouseElement highlightedElement)
    {
        String methodName = "setHighlightedElement() ";
        this.highlightedElement = highlightedElement;
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
