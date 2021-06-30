package Core.WorldView;

import Core.Actor;
import Core.CollectibleStack;
import Core.Enums.ActorTag;
import Core.Sprite.Sprite;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static Core.Configs.Config.*;
import static Core.WorldView.WorldView.*;

public class GridManager
{
    static Set<ActorTag> itemInteractionPossibleTags = new HashSet<>();
    Rectangle2D hoveredGrid = new Rectangle2D(0, 0, 64, 64);
    CollectibleStack collectibleToPlace = CollectibleStack.empty();
    Sprite collectibeSprite;
    Actor blockingActor;
    Text returnMsg = new Text("Press Esc to return");
    private boolean isGridBlocked;

    public GridManager()
    {
        returnMsg.setFont(FONT_ESTROG_30_DEFAULT);
        itemInteractionPossibleTags.add(ActorTag.GROWPLACE);
    }

    public Rectangle2D collectibleOccupiedRect()
    {
        if (collectibeSprite != null)
            return collectibeSprite.getHitbox();
        else
            return new Rectangle2D(0, 0, 0, 0);
    }

    public void drawGrid(GraphicsContext gc)
    {
        gc.setGlobalAlpha(0.5);
        gc.setFill(isGridBlocked() ?
                ((getBlockingActor() != null
                        && !Collections.disjoint(getBlockingActor().getTags(),itemInteractionPossibleTags)) ? COLOR_GOLD : COLOR_RED)
                : COLOR_GREEN);
        gc.fillRect(hoveredGrid.getMinX(), hoveredGrid.getMinY(), hoveredGrid.getWidth(), hoveredGrid.getHeight());
        if (collectibleToPlace.isDefined())
            gc.drawImage(getCollectibeSprite().getBaseimage(), hoveredGrid.getMinX(), hoveredGrid.getMinY());
        gc.setGlobalAlpha(1);
        gc.setStroke(COLOR_MARKING);
        gc.setLineWidth(1);
        for (int x = (int) getCamX() - (int) getCamX() % 64; x < getCamX() + CAMERA_WIDTH; x += 64)
            for (int y = (int) getCamY() - (int) getCamY() % 64; y < getCamY() + CAMERA_HEIGHT; y += 64)
                gc.strokeRect(x, y, 64, 64);
        if (collectibleToPlace.isDefined())
        {
            gc.setStroke(COLOR_RED);
            gc.strokeRect(collectibleOccupiedRect().getMinX(), collectibleOccupiedRect().getMinY(), collectibleOccupiedRect().getWidth(), collectibleOccupiedRect().getHeight());
        }

        double textWidth = returnMsg.getBoundsInLocal().getWidth();
        gc.setFill(COLOR_GREEN);
        gc.fillText(returnMsg.getText(), getCamX() + CAMERA_WIDTH / 2 - textWidth / 2, getCamY() + CAMERA_HEIGHT / 2 - 200);
    }


    public boolean isGridBlocked()
    {
        return isGridBlocked;
    }

    public void setHoveredGrid(Rectangle2D hoveredGrid)
    {
        this.hoveredGrid = hoveredGrid;
    }

    public void setCollectibleToPlace(CollectibleStack collectibleToPlace)
    {
        this.collectibleToPlace = collectibleToPlace;
        collectibeSprite = collectibleToPlace.createSprite((int) hoveredGrid.getMinX(), (int) hoveredGrid.getMinY());
        isGridBlocked = WorldView.isSpriteAtPosition(passiveCollisionRelevantSpritesLayer, collectibeSprite.getHitbox());
        if (isGridBlocked)
            blockingActor = WorldView.getSpriteAtPosition(passiveCollisionRelevantSpritesLayer, collectibeSprite.getHitbox());
        else
            blockingActor = null;
    }

    public Sprite getCollectibeSprite()
    {
        return collectibeSprite;
    }

    public Actor getBlockingActor()
    {
        return blockingActor;
    }
}
