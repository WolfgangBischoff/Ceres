package Core.Menus.Inventory;

import Core.Collectible;
import Core.CollectibleStack;
import javafx.geometry.Point2D;

public class DragAndDropItem
{
    public Point2D screenPosition;
    public CollectibleStack collectible;
    public Inventory previousInventory;
    public int previousIdx;

    public DragAndDropItem(double x, double y, CollectibleStack collectible, Inventory previousInventory, int previousIdx)
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
