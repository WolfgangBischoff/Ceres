package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import javafx.geometry.Point2D;

public class DragAndDropItem
{
    Point2D screenPosition;
    Collectible collectible;
    Inventory origin;
    int originIdx;

    public DragAndDropItem(double x, double y, Collectible collectible, Inventory origin, int originIdx)
    {
        this.screenPosition = new Point2D(x, y);
        this.collectible = collectible;
        this.origin = origin;
        this.originIdx = originIdx;
    }

    public void setPosition(Point2D position)
    {
        screenPosition = position;
    }
}
