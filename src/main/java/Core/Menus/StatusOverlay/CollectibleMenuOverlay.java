package Core.Menus.StatusOverlay;

import Core.CollectibleStack;
import Core.Menus.Inventory.InventoryController;
import Core.Menus.Inventory.MouseElement;
import Core.Menus.Inventory.MouseElementsContainer;
import Core.Menus.Inventory.MouseInteractionType;
import Core.Utilities;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.MouseInteractionType.CLICK;

public class CollectibleMenuOverlay
{
    private static final String CLASSNAME = "CollectibleMenuOverlay/";
    private static final String USE_ID = "USE";
    private static final String WORLD_ID = "WORLD";
    private static final String BACK_ID = "BACK";
    private final MouseElementsContainer mouseElements = new MouseElementsContainer();
    private final InventoryController controller;
    Image frameImage;
    int numberButtons = 3;
    double buttonwith;
    private Rectangle SCREEN_AREA;
    private MouseElement highlightedElement = null;

    public CollectibleMenuOverlay(InventoryController controller)
    {
        this.controller = controller;
    }

    public void render(GraphicsContext gc)
    {
        List<String> lines = new ArrayList<>();
        lines.add("USE");
        lines.add("WORLD");
        lines.add("BACK");
        Rectangle menu = SCREEN_AREA;
        gc.setFill(COLOR_GREEN);
        gc.fillRect(menu.getX(), menu.getY(), menu.getWidth(), menu.getHeight());
        gc.setFill(COLOR_BACKGROUND_GREY);
        gc.fillRect(menu.getX() + 2, menu.getY() + 2, menu.getWidth() - 4, menu.getHeight() - 4);
        gc.setFill(COLOR_FONT);
        gc.setFont(FONT_ORBITRON_12);
        for (int l = 0; l < lines.size(); l++)
        {
            gc.setLineWidth(5);
            gc.setStroke(COLOR_FONT);
            if (l > 0)
                gc.strokeLine(menu.getX() + buttonwith * l, menu.getY(), menu.getX() + buttonwith * l, menu.getY() + menu.getHeight());
            double txtlength = Utilities.calcStringWidth(gc.getFont(), lines.get(l));
            gc.fillText(lines.get(l),
                    menu.getX() + buttonwith * l + (buttonwith - txtlength) / 2,
                    menu.getY() + 20 + FONT_ORBITRON_12.getSize());
        }
    }

    public void setSCREEN_AREA(Rectangle SCREEN_AREA)
    {
        this.SCREEN_AREA = SCREEN_AREA;
        buttonwith = SCREEN_AREA.getWidth() / numberButtons;
        Rectangle leftButton = new Rectangle(SCREEN_AREA.getX(), SCREEN_AREA.getY(), buttonwith, SCREEN_AREA.getHeight());
        Rectangle middleButton = new Rectangle(SCREEN_AREA.getX() + buttonwith, SCREEN_AREA.getY(), buttonwith, SCREEN_AREA.getHeight());
        Rectangle rightButton = new Rectangle(SCREEN_AREA.getX() + buttonwith * 2, SCREEN_AREA.getY(), buttonwith, SCREEN_AREA.getHeight());
        Set<MouseInteractionType> click = new HashSet<>();
        click.add(CLICK);
        mouseElements.add(new MouseElement(leftButton, USE_ID, click));
        mouseElements.add(new MouseElement(middleButton, WORLD_ID, click));
        mouseElements.add(new MouseElement(rightButton, BACK_ID, click));

    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked)
    {
        for (int i = 0; i < mouseElements.size(); i++)
        {
            if (mouseElements.get(i).getPosition().contains(mousePosition))
            {
                highlightedElement = mouseElements.get(i);
            }
        }

        if (isMouseClicked && highlightedElement.identifier.equals(USE_ID))
        {
            System.out.println("use hovering menu");
            controller.useMenuCollectible();
        }
        else if (isMouseClicked && highlightedElement.identifier.equals(WORLD_ID))
        {
            System.out.println("world hovering menu");
            controller.setMenuCollectible(CollectibleStack.empty());
        }
        else if (isMouseClicked && highlightedElement.identifier.equals(BACK_ID))
        {
            System.out.println("back hovering menu");
            controller.setMenuCollectible(CollectibleStack.empty());
        }

    }

}
