package Core.Sprite;

import Core.Actor;
import Core.Configs.Config;
import Core.Enums.Direction;
import Core.GameWindow;
import Core.Utilities;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.util.List;

import static Core.Configs.Config.*;
import static Core.Enums.ActorTag.NO_COLLISION;
import static Core.Enums.Direction.*;
import static Core.Enums.TriggerType.NOTHING;
import static java.lang.Math.min;

public class Sprite
{
    private static final String CLASSNAME = "Sprite/";
    private static final double DEBUG_LAG_TIME_MAX = 0.05;
    Image baseimage;
    double basewidth; //width of whole sprite, in therms of animation multiple frames
    double baseheight;
    Point2D position;
    Long lastFrame = 0L;
    Long lastUpdated = 0L;
    Rectangle2D interactionArea;
    Actor actor; //Logic for sprite
    private String name = "notSet";
    private double fps; //frames per second I.E. 24
    private int totalFrames; //Total number of frames in the sequence
    private int cols; //Number of columns on the sprite sheet
    private int rows; //Number of rows on the sprite sheet
    private double frameWidth; //Width of an individual frame
    private double frameHeight; //Height of an individual frame
    private int currentCol = 0; //last used frame in case of animation
    private int currentRow = 0;
    private Boolean isBlocker = false;
    private Boolean animated;
    private Boolean animationEnds = false;
    private Boolean interact = false;
    private double hitBoxOffsetX = 0, hitBoxOffsetY = 0, hitBoxWidth, hitBoxHeight;
    private String lightningSpriteName;
    private int layer = -1;
    private String dialogueFileName = null;
    private String initDialogueId = "none";


    public Sprite(String path)
    {
        animated = false;
        setImage(path);
        frameWidth = basewidth;
        frameHeight = baseheight;
        hitBoxWidth = frameWidth;
        hitBoxHeight = frameHeight;
    }

    public Sprite(String imagename, Double fps, int totalFrames, int cols, int rows, int frameWidth, int frameHeight)
    {
        animated = true;
        setImage(imagename);
        this.fps = fps;
        this.totalFrames = totalFrames;
        this.cols = cols;
        this.rows = rows;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        hitBoxWidth = frameWidth;
        hitBoxHeight = frameHeight;
    }

    public static Sprite createSprite(SpriteData spriteData, double x, double y)
    {
        Sprite ca;
        try {
            if (spriteData.totalFrames > 1)
                ca = new Sprite(spriteData.spriteName, spriteData.fps, spriteData.totalFrames, spriteData.cols, spriteData.rows, spriteData.frameWidth, spriteData.frameHeight);
            else
                ca = new Sprite(spriteData.spriteName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            ca = new Sprite(IMAGE_DIRECTORY_PATH + "notfound_64_64" + CSV_POSTFIX);
        }

        ca.setName(spriteData.name);
        ca.setPosition(x, y);
        ca.setBlocker(spriteData.blocking);
        ca.setLightningSpriteName(spriteData.lightningSprite);
        ca.setLayer(spriteData.renderLayer);
        ca.setAnimationEnds(spriteData.animationEnds);

        ca.setDialogueFileName(spriteData.dialogieFile);
        ca.setInitDialogueId(spriteData.dialogueID);

        //If Hitbox differs
        if (spriteData.hitboxOffsetX != 0 || spriteData.hitboxOffsetY != 0 || spriteData.hitboxWidth != 0 || spriteData.hitboxHeight != 0)
            ca.setHitBox(spriteData.hitboxOffsetX, spriteData.hitboxOffsetY, spriteData.hitboxWidth, spriteData.hitboxHeight);

        return ca;
    }

    public static String getClassname()
    {
        return CLASSNAME;
    }

    private Rectangle2D calcInteractionRectangle()
    {
        String methodName = "calcInteractionRectangle() ";
        double interactionWidth = actor.getInteractionAreaWidth();
        double maxInteractionDistance = actor.getInteractionAreaDistance();
        double offsetX = actor.getInteractionAreaOffsetX();
        double offsetY = actor.getInteractionAreaOffsetY();

        switch (actor.getDirection()) {
            case NORTH:
                return new Rectangle2D(position.getX() + hitBoxOffsetX + hitBoxWidth / 2 - interactionWidth / 2 + offsetX, position.getY() + hitBoxOffsetY - maxInteractionDistance + offsetY, interactionWidth, maxInteractionDistance);
            case EAST:
                return new Rectangle2D(position.getX() + hitBoxOffsetX + hitBoxWidth + offsetX, position.getY() + hitBoxOffsetY + hitBoxHeight / 2 - interactionWidth / 2 + offsetY, maxInteractionDistance, interactionWidth);
            case SOUTH:
                return new Rectangle2D(position.getX() + hitBoxOffsetX + hitBoxWidth / 2 - interactionWidth / 2 + offsetX, position.getY() + hitBoxOffsetY + hitBoxHeight + offsetY, interactionWidth, maxInteractionDistance);
            case WEST:
                return new Rectangle2D(position.getX() + hitBoxOffsetX - maxInteractionDistance + offsetX, position.getY() + hitBoxOffsetY + hitBoxHeight / 2 - interactionWidth / 2 + offsetY, maxInteractionDistance, interactionWidth);
            case UNDEFINED:
                return null;
            default:
                throw new RuntimeException("calcInteractionRectangle: No Direction Set at " + name);
        }
    }

    public void update(Long updateTime)
    {
        long methodStartTime = System.nanoTime();
        double time = min(((updateTime - lastUpdated) / 1000000000.0), DEBUG_LAG_TIME_MAX);
        double elapsedTimeSinceLastInteraction = (updateTime - actor.getLastInteraction()) / 1000000000.0;
        List<Sprite> passiveCollisionRelevantSpritesLayer = WorldView.getPassiveCollisionRelevantSpritesLayer();
        double velocityX = actor.getCurrentVelocityX();
        double velocityY = actor.getCurrentVelocityY();
        Rectangle2D plannedPosition = new Rectangle2D(position.getX() + hitBoxOffsetX + velocityX * time, position.getY() + hitBoxOffsetY + velocityY * time, hitBoxWidth, hitBoxHeight);

        String initGeneralStatusFrame = "";
        if (actor != null)
        {
            initGeneralStatusFrame = actor.getGeneralStatus();
            //Calculate Interaction Area
            if (interact || actor.getSensorStatus().getOnInRange_TriggerSprite() != NOTHING || WorldView.getPlayer() == this)
                interactionArea = calcInteractionRectangle();
        }

        for (Sprite otherSprite : passiveCollisionRelevantSpritesLayer) {
            if (otherSprite == this ||
                    otherSprite.actor == actor
            )
                continue;

            //Interact within interaction area
            if (interact
                    && otherSprite.getHitbox().intersects(interactionArea)
                    && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS) {

                if (otherSprite.actor != null &&
                        (otherSprite.actor.getSensorStatus().getOnInteraction_TriggerSprite() != NOTHING
                                || otherSprite.actor.getSensorStatus().getOnInteraction_TriggerSensor() != NOTHING)) {
                    otherSprite.actor.onInteraction(this, updateTime);
                    actor.setLastInteraction(updateTime);
                    interact = false;
                }
                else if (otherSprite.actor == null &&//just use this for deco-sprites
                        !(otherSprite.dialogueFileName.equals("dialogueFile") || otherSprite.dialogueFileName.equals("none"))) {
                    WorldView.startConversation(otherSprite.dialogueFileName, otherSprite.initDialogueId, updateTime);
                    interact = false;
                }
            }

            //In range
             if (otherSprite.actor != null
                     && actor.getSensorStatus().getOnInRange_TriggerSprite() != NOTHING
                     && otherSprite.getHitbox().intersects(interactionArea)) {
                 actor.onInRange(otherSprite, updateTime);
             }
             //Intersect
             if ((actor.getSensorStatus().getOnIntersection_TriggerSprite() != NOTHING || actor.getSensorStatus().getOnIntersection_TriggerSensor() != NOTHING) && intersects(otherSprite)) {
                 actor.onIntersection(otherSprite, updateTime);
             }
        }

        //check if status was changed from other triggers, just if not do OnUpdate
        if (actor != null && initGeneralStatusFrame.equals(actor.getGeneralStatus())) {
            if (actor.getSensorStatus().getOnUpdate_TriggerSprite() != NOTHING && !actor.getSensorStatus().getOnUpdateToStatusSprite().equals(actor.getGeneralStatus()))
                actor.onUpdate(updateTime);
            if (actor.getSensorStatus().getOnUpdate_TriggerSensor() != NOTHING && !actor.getSensorStatus().getOnUpdate_StatusSensor().equals(actor.getSensorStatus().getStatusName()))
                actor.onUpdate(updateTime);
        }

        if (!((DEBUG_NO_WALL && this == WorldView.getPlayer()) ||
                (actor != null && actor.tags.contains(NO_COLLISION)))) {
            if (this == WorldView.getPlayer() && isBlockedByOtherSprites(velocityX * time, velocityY * time)) {
                Pair<Double, Double> dodgeVelocity = calculateDodgeVelocity(plannedPosition, this.actor.getDirection(), time);
                velocityX += dodgeVelocity.getKey();
                velocityY += dodgeVelocity.getValue();
            }

            //Set blocked velocity to Zero
            if (isBlockedByOtherSprites(0, velocityY * time))
                velocityY = 0;
            if (isBlockedByOtherSprites(velocityX * time, 0))
                velocityX = 0;
        }
        position = position.add(velocityX * time, velocityY * time);
        interact = false;
        lastUpdated = updateTime;
        //long totalTime = System.nanoTime() - methodStartTime;
        //FXUtils.addData(getName() + ":" + totalTime / 1000);
    }

    private boolean isBlockedBy(Sprite otherSprite, Rectangle2D plannedPosition)
    {
        if (otherSprite == this)
            return false;
        return otherSprite.isBlocker && otherSprite.getHitbox().intersects(plannedPosition);
    }

    private boolean isBlockedByOtherSprites(double deltaX, double deltaY)
    {
        Rectangle2D plannedPosition = new Rectangle2D(position.getX() + hitBoxOffsetX + deltaX, position.getY() + hitBoxOffsetY + deltaY, hitBoxWidth, hitBoxHeight);
        for (Sprite otherSprite : WorldView.getPassiveCollisionRelevantSpritesLayer())
            if (isBlockedBy(otherSprite, plannedPosition))
                return true;
        return false;
    }

    public boolean isBlockedByOtherSprites(Direction direction, double delta)
    {
        switch (direction) {
            case NORTH:
                return isBlockedByOtherSprites(0, -delta);
            case SOUTH:
                return isBlockedByOtherSprites(0, delta);
            case WEST:
                return isBlockedByOtherSprites(-delta, 0);
            case EAST:
                return isBlockedByOtherSprites(delta, 0);
        }
        return false;
    }

    private Pair<Double, Double> calculateDodgeVelocity(Rectangle2D plannedPosition, Direction direction, double time)
    {
        String methodName = "calculateDodge() ";
        double playerLeftEdge = 0;
        double playerRightEdge = 0;
        double blockingSpriteLeftEdge = 0;
        double blockingSpriteRightEdge = 0;
        double velocityDodge = DODGE_VELOCITY;
        Rectangle2D blockingSprite = null;
        Pair<Double, Double> ret = new Pair<>(0d, 0d);
        for (Sprite otherSprite : WorldView.getPassiveCollisionRelevantSpritesLayer())
            if (isBlockedBy(otherSprite, plannedPosition))
                blockingSprite = otherSprite.getHitbox();

        if (direction == NORTH || direction == SOUTH) {
            playerLeftEdge = plannedPosition.getMinX();
            playerRightEdge = plannedPosition.getMaxX();
            blockingSpriteLeftEdge = blockingSprite.getMinX();
            blockingSpriteRightEdge = blockingSprite.getMaxX();
        }
        else if (direction == WEST || direction == Direction.EAST) {
            playerLeftEdge = plannedPosition.getMaxY();
            playerRightEdge = plannedPosition.getMinY();
            blockingSpriteLeftEdge = blockingSprite.getMaxY();
            blockingSpriteRightEdge = blockingSprite.getMinY();
        }

        if (playerLeftEdge < blockingSpriteLeftEdge && playerRightEdge < blockingSpriteRightEdge)//at west border
        {
            if ((direction == NORTH || direction == SOUTH) && !isBlockedByOtherSprites(-velocityDodge * time, 0))
                ret = new Pair<>(-velocityDodge, 0d);//west edge => go west
            else if (!isBlockedByOtherSprites(0d, -velocityDodge))
                ret = new Pair<>(0d, -velocityDodge);//south edge => go south
        }
        else if (playerLeftEdge > blockingSpriteLeftEdge && playerRightEdge > blockingSpriteRightEdge) {
            if ((direction == NORTH || direction == SOUTH) && !isBlockedByOtherSprites(velocityDodge, 0d))
                ret = new Pair<>(velocityDodge, 0d);
            else if (!isBlockedByOtherSprites(0d, velocityDodge))
                ret = new Pair<>(0d, velocityDodge);

        }
        return ret;
    }

    public void onClick(Long currentNanoTime)
    {
        String methodName = "onClick(Long) ";
        boolean debug = false;

        if (debug)
            System.out.println(CLASSNAME + methodName + name + " clicked: " + actor.getActorInGameName());

        //Sprite is clicked by player and in Range
        Sprite player = WorldView.getPlayer();
        player.calcInteractionRectangle();
        double elapsedTimeSinceLastInteraction = (currentNanoTime - player.actor.getLastInteraction()) / 1000000000.0;
        if (getHitbox().intersects(player.interactionArea)
                && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS

        ) {
            if (debug)
                System.out.println(CLASSNAME + methodName + player.getName() + " interact with " + getName() + " by mouseclick.");
            if (actor != null &&
                    (actor.getSensorStatus().getOnInteraction_TriggerSprite() != NOTHING || actor.getSensorStatus().getOnInteraction_TriggerSensor() != NOTHING)) {
                actor.onInteraction(player, currentNanoTime); //Passive reacts
                player.actor.setLastInteraction(currentNanoTime);
            }
            else if (!(getDialogueFileName().equals("dialogueFile") || getDialogueFileName().equals("none"))) {
                WorldView.startConversation(dialogueFileName, initDialogueId, currentNanoTime);
                player.actor.setLastInteraction(currentNanoTime);
            }

        }

    }

    public boolean intersectsRelativeToWorldView(Point2D point)
    {
        String methodName = "intersectsRelativeToWorldView() ";
        Rectangle2D intersectionHitbox = new Rectangle2D(position.getX() - WorldView.getCamX(), position.getY() - WorldView.getCamY(), frameWidth, frameHeight);
        return intersectionHitbox.contains(point);
    }

    public boolean intersects(Sprite s)
    {
        return s.getHitbox().intersects(this.getHitbox());
    }

    public void render(GraphicsContext gc, Long renderTime)
    {
        String methodName = "render()";

        if (getAnimated()) {
            renderAnimated(gc, renderTime);
        }
        else {
            renderSimple(gc);
        }

        if ((DEBUG_BLOCKER && isBlocker) || (DEBUG_ACTORS && actor != null))
            drawDebugFrame(gc);
    }

    private void drawDebugFrame(GraphicsContext gc)
    {
        gc.setStroke(Color.BLUE);
        gc.strokeRect(position.getX() + hitBoxOffsetX, position.getY() + hitBoxOffsetY, hitBoxWidth, hitBoxHeight);
        if (interactionArea != null)
            gc.strokeRect(interactionArea.getMinX(), interactionArea.getMinY(), interactionArea.getWidth(), interactionArea.getHeight());
    }

    public void renderSimple(GraphicsContext gc)
    {
        String methodName = "renderSimple() ";
        gc.drawImage(baseimage, position.getX(), position.getY());
    }

    public void renderAnimated(GraphicsContext gc, Long now)
    {
        String methodName = "renderAnimated() ";
        int frameJump = (int) Math.floor((now - lastFrame) / (1000000000 / fps)); //Determine how many frames we need to advance to maintain frame rate independence

        //Do a bunch of math to determine where the viewport needs to be positioned on the sprite sheet
        if (frameJump >= 1 && !(isAtLastFrame() && animationEnds)) {
            lastFrame = now;

            int addRows = (int) Math.floor((float) frameJump / (float) cols);
            int frameAdd = frameJump - (addRows * cols);

            if (currentCol + frameAdd >= cols)//column finished, move no next row
            {
                currentRow += addRows + 1;
                currentCol = frameAdd - (cols - currentCol);
            }
            else//add FrameJump To current row
            {
                currentRow += addRows;
                currentCol += frameAdd;
            }
            currentRow = (currentRow >= rows) ? currentRow - ((int) Math.floor((float) currentRow / rows) * rows) : currentRow;

            //The last row may or may not contain the full number of columns
            if (isAtLastFrame())//if last frame considering rows
            {
                currentRow = 0;
                currentCol = Math.abs(currentCol - (totalFrames - (int) (Math.floor((float) totalFrames / cols) * cols)));
            }
        }

        gc.drawImage(baseimage, currentCol * frameWidth, currentRow * frameHeight, frameWidth, frameHeight, position.getX(), position.getY(), frameWidth, frameHeight); //(img, srcX, srcY, srcWidht, srcHeight, TargetX, TargetY, TargetWidht, TargetHeight)

    }

    private boolean isAtLastFrame()
    {
        return (currentRow * cols) + currentCol >= totalFrames - 1;
    }

    public Rectangle2D getHitbox()
    {
        return new Rectangle2D(position.getX() + hitBoxOffsetX, position.getY() + hitBoxOffsetY, hitBoxWidth, hitBoxHeight);
    }

    public String toString()
    {
        return name + " Position: [" + position.getX() + "," + position.getY() + "]"
                ;
    }

    public void setImage(String filename)
    {
        String methodName = "setImage(String) ";
        baseimage = Utilities.readImage(filename + PNG_POSTFIX);
        basewidth = baseimage.getWidth();
        baseheight = baseimage.getHeight();
        currentCol = currentRow = 0;
    }

    public void setImage(String filename, double fps, int totalFrames, int cols, int row, int frameWidth, int frameHeight)
    {
        if (totalFrames > 1)
            setAnimated(true);
        else
            setAnimated(false);
        setImage(filename);
        this.fps = fps;
        this.totalFrames = totalFrames;
        this.cols = cols;
        this.rows = row;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public void setHitBox(double hitBoxOffsetX, double hitBoxOffsetY, double hitBoxWidth, double hitBoxHeight)
    {
        this.hitBoxOffsetX = hitBoxOffsetX;
        this.hitBoxOffsetY = hitBoxOffsetY;
        this.hitBoxWidth = hitBoxWidth;
        this.hitBoxHeight = hitBoxHeight;
    }

    public void setPosition(double x, double y)
    {
        position = new Point2D(x, y);
    }

    public Boolean getAnimated()
    {
        return animated;
    }

    public void setAnimated(Boolean animated)
    {
        lastFrame = GameWindow.getSingleton().getRenderTime(); //To set frameJump to first image, if Animation starts later with interaction
        this.animated = animated;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getHitBoxOffsetX()
    {
        return hitBoxOffsetX;
    }

    public double getHitBoxOffsetY()
    {
        return hitBoxOffsetY;
    }

    public double getHitBoxWidth()
    {
        return hitBoxWidth;
    }

    public void setHitBoxWidth(double hitBoxWidth)
    {
        this.hitBoxWidth = hitBoxWidth;
    }

    public double getHitBoxHeight()
    {
        return hitBoxHeight;
    }

    public void setHitBoxHeight(double hitBoxHeight)
    {
        this.hitBoxHeight = hitBoxHeight;
    }

    public String getLightningSpriteName()
    {
        return lightningSpriteName;
    }

    public void setLightningSpriteName(String lightningSpriteName)
    {
        this.lightningSpriteName = lightningSpriteName;
    }

    public Actor getActor()
    {
        return actor;
    }

    public void setActor(Actor actor)
    {
        this.actor = actor;
    }

    public double getBasewidth()
    {
        return basewidth;
    }

    public double getBaseheight()
    {
        return baseheight;
    }

    public double getX()
    {
        return position.getX();
    }

    public double getY()
    {
        return position.getY();
    }

    public double getFps()
    {
        return fps;
    }

    public void setFps(double fps)
    {
        this.fps = fps;
    }

    public int getTotalFrames()
    {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames)
    {
        this.totalFrames = totalFrames;
    }

    public int getCols()
    {
        return cols;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public int getRows()
    {
        return rows;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public double getFrameWidth()
    {
        return frameWidth;
    }

    public void setFrameWidth(double frameWidth)
    {
        this.frameWidth = frameWidth;
    }

    public double getFrameHeight()
    {
        return frameHeight;
    }

    public void setFrameHeight(double frameHeight)
    {
        this.frameHeight = frameHeight;
    }

    public Boolean getBlocker()
    {
        return isBlocker;
    }

    public void setBlocker(Boolean blocker)
    {
        isBlocker = blocker;
    }

    public int getLayer()
    {
        return layer;
    }

    public void setLayer(int layer)
    {
        this.layer = layer;
    }

    public String getDialogueFileName()
    {
        return dialogueFileName;
    }

    public void setDialogueFileName(String dialogueFileName)
    {
        this.dialogueFileName = dialogueFileName;
    }

    public String getInitDialogueId()
    {
        return initDialogueId;
    }

    public void setInitDialogueId(String initDialogueId)
    {
        this.initDialogueId = initDialogueId;
    }

    public int getCurrentCol()
    {
        return currentCol;
    }

    public int getCurrentRow()
    {
        return currentRow;
    }

    public Boolean getAnimationEnds()
    {
        return animationEnds;
    }

    public void setAnimationEnds(Boolean animationEnds)
    {
        this.animationEnds = animationEnds;
    }

    public Boolean getInteract()
    {
        return interact;
    }

    public void setInteract(Boolean interact)
    {
        this.interact = interact;
    }

    //public Boolean getBlockedByOtherSprite()
    //{
    //    return blockedByOtherSprite;
    //}

    public Image getBaseimage()
    {
        return baseimage;
    }

    public Long getLastFrame()
    {
        return lastFrame;
    }

    public Long getLastUpdated()
    {
        return lastUpdated;
    }

    public Rectangle2D getInteractionArea()
    {
        return interactionArea;
    }


}
