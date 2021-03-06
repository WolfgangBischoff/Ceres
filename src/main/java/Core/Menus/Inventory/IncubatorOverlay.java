package Core.Menus.Inventory;

import Core.*;
import Core.Enums.CollectableType;
import Core.Sprite.Sprite;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;
import static Core.Menus.Inventory.MouseInteractionType.DRAG;

public class IncubatorOverlay implements DragAndDropOverlay
{
    private static final String CLASSNAME = "IncubatorOverlay/";
    private static final int WIDTH = INVENTORY_WIDTH;
    private static final int HEIGHT = INVENTORY_HEIGHT;
    final String BASE_INPUT_SLOT = "base_input";
    final String BASE_OUTPUT_SLOT = "base_output";
    final String CONVERT_BUTTON_ID = "CONVERT";
    final String CANCEL_BUTTON_ID = "CANCEL";
    Image cornerTopLeft;
    Image cornerBtmRight;
    Sprite convertButton;
    Image cancelButton;
    private final InventoryController controller;
    private final MouseElementsContainer mouseElements = new MouseElementsContainer();
    private MouseElement highlightedElement = null;
    private Point2D SCREEN_POSITION;
    private final Rectangle2D SCREEN_AREA;
    private Actor actor;

    public IncubatorOverlay(Actor incubator, Point2D SCREEN_POSITION, InventoryController controller)
    {
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        cancelButton = Utilities.readImage(IMAGE_DIRECTORY_PATH + "interface/cancelButton.png");
        this.SCREEN_POSITION = SCREEN_POSITION;
        SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
        this.actor = incubator;
        this.controller = controller;
        init();
    }

    private void init()
    {
        Rectangle convertButtonRect = new Rectangle(SCREEN_POSITION.getX() + 110, SCREEN_POSITION.getY() + 300, 150, 64);
        MouseElement convertBtn = new MouseElement(convertButtonRect, CONVERT_BUTTON_ID, CLICK);
        mouseElements.add(convertBtn);
        convertButton = new Sprite(IMAGE_DIRECTORY_PATH + "interface/incubator/convert_button", 6.0, 4, 4, 1, 150,64);
        convertButton.setPosition(convertButtonRect.getX(), convertButtonRect.getY());

        Rectangle base_input = new Rectangle(SCREEN_POSITION.getX() + 100, SCREEN_POSITION.getY() + 200, 64, 64);
        mouseElements.add(new MouseElement(base_input, BASE_INPUT_SLOT, DRAG));

        Rectangle base_output = new Rectangle(SCREEN_POSITION.getX() + 200, SCREEN_POSITION.getY() + 200, 64, 64);
        mouseElements.add(new MouseElement(base_output, BASE_OUTPUT_SLOT, DRAG));

        Rectangle cancelButtonRect = new Rectangle(SCREEN_POSITION.getX() + WIDTH - cancelButton.getWidth(), SCREEN_POSITION.getY(), 64, 64);
        mouseElements.add(new MouseElement(cancelButtonRect, CANCEL_BUTTON_ID, CLICK));
    }

    public void render(GraphicsContext gc, long currentRenderTime) throws NullPointerException
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

        Rectangle convertButtonRect = (Rectangle)mouseElements.get(CONVERT_BUTTON_ID).position;
        convertButton.render(gc, currentRenderTime);

        //Text
        int offsetYFirstLine = 60;
        int dateLength = 200;
        gc.setFont(FONT_ESTROG_20);
        gc.setFill(COLOR_FONT);
        gc.fillText("Incubator", SCREEN_POSITION.getX() + WIDTH - dateLength, SCREEN_POSITION.getY() + offsetYFirstLine);
        gc.fillText("Input", SCREEN_POSITION.getX() + 100, SCREEN_POSITION.getY() + 190);
        gc.fillText("Output", SCREEN_POSITION.getX() + 200, SCREEN_POSITION.getY() + 190);

        //Decoration
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY());
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());
        Rectangle cancelButtonRect = (Rectangle) mouseElements.get(CANCEL_BUTTON_ID).position;
        gc.drawImage(cancelButton, cancelButtonRect.getX(), cancelButtonRect.getY());

    }

    private void drawItemSlot(GraphicsContext gc, String itemslotName)
    {
        MouseElement mouseElement = mouseElements.get(itemslotName);
        int inventoryIdx = mouseElements.indexOf(mouseElement);
        Rectangle rectangle2D = (Rectangle) mouseElement.position;

        gc.setFill(COLOR_FONT);
        gc.fillRect(rectangle2D.getX(), rectangle2D.getY(), rectangle2D.getWidth(), rectangle2D.getHeight());
        gc.setFill(highlightedElement == mouseElement ? COLOR_FONT : COLOR_MARKING);
        gc.fillRect(rectangle2D.getX() + 2, rectangle2D.getY() + 2, rectangle2D.getWidth() - 4, rectangle2D.getHeight() - 4);
        if (actor.getInventory().itemsList.get(inventoryIdx) != null)
            gc.drawImage(actor.getInventory().itemsList.get(inventoryIdx).getImage(), rectangle2D.getX(), rectangle2D.getY());
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
            if (highlightedElement.reactiveTypes.contains(CLICK))
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
                break;
            case CANCEL_BUTTON_ID:
                WorldViewController.setWorldViewStatus(WorldViewStatus.INVENTORY);
                break;
            default:
        }

    }

    private void convertItem()
    {
        CollectibleStack inputSlotItem = actor.getInventory().getCollectibeStack(mouseElements.indexOf(mouseElements.get(BASE_INPUT_SLOT)));
        CollectibleStack outputSlotItem = actor.getInventory().getCollectibeStack(mouseElements.indexOf(mouseElements.get(BASE_OUTPUT_SLOT)));

        if (inputSlotItem.isDefined() && inputSlotItem.getTypes().contains(CollectableType.BACTERIA_CULTURE) && outputSlotItem.isEmpty())
        {
            Collectible converted = Collectible.createCollectible("actorData/collectibles/key/keycard", "electric_lockpic");
            actor.getInventory().addCollectibleStackIdx(new CollectibleStack(converted), mouseElements.indexOf(mouseElements.get(BASE_OUTPUT_SLOT)));
            actor.getInventory().removeCollectibleStack(inputSlotItem);
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
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG))
        {

            dropped.previousInventory.addCollectibleStackIdx(//Swap item if exists
                    actor.getInventory().getCollectibeStack(mouseElements.indexOf(highlightedElement)),
                    dropped.previousIdx);
            actor.getInventory().addCollectibleStackIdx(dropped.collectible, mouseElements.indexOf(highlightedElement));
        }
        else
        {
            dropped.previousInventory.addCollectibleStackIdx(//Back to previous Inventory
                    dropped.collectible,
                    dropped.previousIdx);
        }
        controller.setDragAndDropItem(null);
    }

    @Override
    public void dragCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (highlightedElement != null && highlightedElement.reactiveTypes.contains(DRAG))
        {
            if (actor.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement)) != null && controller.getDragAndDropItem() == null)
            {
                CollectibleStack collectible;
                collectible = actor.getInventory().itemsList.get(mouseElements.indexOf(highlightedElement));
                actor.getInventory().removeCollectibleStack(collectible);
                controller.setDragAndDropItem(new DragAndDropItem(mousePosition.getX(), mousePosition.getY(), collectible, actor.getInventory(), mouseElements.indexOf(highlightedElement)));
            }
        }
    }

    @Override
    public void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition)
    {
        if (controller.getDragAndDropItem() != null)
        {
            controller.getDragAndDropItem().setPosition(new Point2D(mousePosition.getX(), mousePosition.getY()));
        }
    }

    public void setActor(Actor actor)
    {
        this.actor = actor;
    }
}
