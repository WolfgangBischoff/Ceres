package Core.Menus.Inventory;

import Core.Collectible;
import javafx.geometry.Point2D;

public interface DragAndDropOverlay
{
    public void dropCollectible(Collectible collectible);

    public void dragCollectible(Long currentNanoTime, Point2D mousePosition);

    public void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition);
}
