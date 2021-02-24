package Core.Menus.Inventory;

import Core.Collectible;
import javafx.geometry.Point2D;

public interface DragAndDropOverlay
{
    void dropCollectible(DragAndDropItem collectible);

    void dragCollectible(Long currentNanoTime, Point2D mousePosition);

    void updateDraggedCollectible(Long currentNanoTime, Point2D mousePosition);
}
