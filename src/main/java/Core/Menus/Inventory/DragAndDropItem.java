package Core.Menus.Inventory;

import Core.Collectible;
import javafx.geometry.Point2D;

public class DragAndDropItem
{
    Point2D screenPosition;
    Collectible collectible;
    Inventory previousInventory;
    int previousIdx;

    public DragAndDropItem(double x, double y, Collectible collectible, Inventory previousInventory, int previousIdx)
    {
        this.screenPosition = new Point2D(x, y);
        this.collectible = collectible;
        this.previousInventory = previousInventory;
        this.previousIdx = previousIdx;
    }

    public void setPosition(Point2D position)
    {
        screenPosition = position;
    }
}
