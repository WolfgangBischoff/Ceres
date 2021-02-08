package Core.Menus.Inventory;

import Core.Collectible;
import javafx.geometry.Point2D;

public class DragAndDropItem
{
    Point2D screenPosition;
    Collectible collectible;

    public DragAndDropItem(Point2D screenPosition, Collectible collectible)
    {
        this.screenPosition = screenPosition;
        this.collectible = collectible;
    }
    public DragAndDropItem(double x, double y, Collectible collectible)
    {
        this.screenPosition = new Point2D(x,y);
        this.collectible = collectible;
    }

    public void setPosition(Point2D position)
    {
        screenPosition = position;
    }
}
