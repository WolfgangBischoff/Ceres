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
import javafx.util.Pair;

import java.util.List;

import static Core.Configs.Config.*;
import static Core.Enums.Direction.NORTH;
import static Core.Enums.Direction.SOUTH;
import static Core.Enums.TriggerType.NOTHING;

public class Sprite
{
    private static final String CLASSNAME = "Sprite/";
    Image baseimage;
    double basewidth; //width of whole sprite, in therms of animation multiple frames
    double baseheight;
    Point2D position;
    //double position.getX();
    //double position.getY();
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
    //private Boolean blockedByOtherSprite = false;
    private double hitBoxOffsetX = 0, hitBoxOffsetY = 0, hitBoxWidth, hitBoxHeight;
    private String lightningSpriteName;
    private int layer = -1;
    private String dialogueFileName = null;
    private String initDialogueId = "none";


    public Sprite(String imagename)
    {
        animated = false;
        setImage(imagename);
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

    public static Sprite createSprite(SpriteData tile, double x, double y)
    {
        String methodName = "createSprite() ";
        Sprite ca;
        try
        {
            if (tile.totalFrames > 1)
                ca = new Sprite(tile.spriteName, tile.fps, tile.totalFrames, tile.cols, tile.rows, tile.frameWidth, tile.frameHeight);
            else
                ca = new Sprite(tile.spriteName);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            ca = new Sprite(IMAGE_DIRECTORY_PATH + "notfound_64_64" + CSV_POSTFIX);
        }

        ca.setName(tile.name);
        ca.setPosition(x, y);
        ca.setBlocker(tile.blocking);
        ca.setLightningSpriteName(tile.lightningSprite);

        ca.setAnimationEnds(tile.animationEnds);

        ca.setDialogueFileName(tile.dialogieFile);
        ca.setInitDialogueId(tile.dialogueID);

        //If Hitbox differs
        if (tile.hitboxOffsetX != 0 || tile.hitboxOffsetY != 0 || tile.hitboxWidth != 0 || tile.hitboxHeight != 0)
            ca.setHitBox(tile.hitboxOffsetX, tile.hitboxOffsetY, tile.hitboxWidth, tile.hitboxHeight);

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

        switch (actor.getDirection())
        {
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

    public void update(Long currentNanoTime)
    {
        String methodName = "update() ";

        double time = (currentNanoTime - lastUpdated) / 1000000000.0;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - actor.getLastInteraction()) / 1000000000.0;
        List<Sprite> activeSprites = WorldView.getPassiveCollisionRelevantSpritesLayer();
        double velocityX = actor.getVelocityX();
        double velocityY = actor.getVelocityY();
        Rectangle2D plannedPosition = new Rectangle2D(position.getX() + hitBoxOffsetX + velocityX * time, position.getY() + hitBoxOffsetY + velocityY * time, hitBoxWidth, hitBoxHeight);
        Pair<Double, Double> dodgeVelocities = new Pair<>(0d, 0d);

        String initGeneralStatusFrame = "";
        if (actor != null)
            initGeneralStatusFrame = actor.getGeneralStatus();

        for (Sprite otherSprite : activeSprites)
        {
            if (otherSprite == this ||
                    otherSprite.actor == actor //if actor has multiple sprites they are usually congruent
            )
                continue;

            //Calculate Interaction Area
            if (interact || actor.getSensorStatus().getOnInRange_TriggerSprite() != NOTHING || getName().equalsIgnoreCase("player"))
                interactionArea = calcInteractionRectangle();

            //Interact within interaction area
            if (interact
                    && otherSprite.getBoundary().intersects(interactionArea)
                    && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS)
            {

                if (otherSprite.actor != null &&
                        (otherSprite.actor.getSensorStatus().getOnInteraction_TriggerSprite() != NOTHING
                                || otherSprite.actor.getSensorStatus().getOnInteraction_TriggerSensor() != NOTHING))
                {
                    otherSprite.actor.onInteraction(this, currentNanoTime);
                    actor.setLastInteraction(currentNanoTime);
                    interact = false;
                }
                else if (otherSprite.actor == null &&//just use this for deco-sprites
                        !(otherSprite.dialogueFileName.equals("dialogueFile") || otherSprite.dialogueFileName.equals("none")))
                {
                    WorldView.startConversation(otherSprite.dialogueFileName, otherSprite.initDialogueId, currentNanoTime);
                    interact = false;
                }
            }

            //In range
            if (otherSprite.actor != null
                    && actor.getSensorStatus().getOnInRange_TriggerSprite() != NOTHING
                    && otherSprite.getBoundary().intersects(interactionArea))
            {
                actor.onInRange(otherSprite, currentNanoTime);
            }

            //Intersect
            if (intersects(otherSprite) && (actor.getSensorStatus().getOnIntersection_TriggerSprite() != NOTHING || actor.getSensorStatus().getOnIntersection_TriggerSensor() != NOTHING))
            {
                actor.onIntersection(otherSprite, currentNanoTime);
            }
        }


        //check if status was changed from other triggers, just if not do OnUpdate
        if (actor != null && initGeneralStatusFrame.equals(actor.getGeneralStatus()))
        {
            if (actor.getSensorStatus().getOnUpdate_TriggerSprite() != NOTHING && !actor.getSensorStatus().getOnUpdateToStatusSprite().equals(actor.getGeneralStatus()))
                actor.onUpdate(currentNanoTime);
            if (actor.getSensorStatus().getOnUpdate_TriggerSensor() != NOTHING && !actor.getSensorStatus().getOnUpdate_StatusSensor().equals(actor.getSensorStatus().getStatusName()))
                actor.onUpdate(currentNanoTime);
        }

        if (!isBlockedByOtherSprites(velocityX * time, velocityY * time) || (DEBUG_NO_WALL && this == WorldView.getPlayer()))
        {
            position = position.add(velocityX * time, velocityY * time);
        }
        else
        {
            if (this == WorldView.getPlayer())
            {
                Pair<Double, Double> delta = calculateDodge(plannedPosition, this.actor.getDirection());
                dodgeVelocities = new Pair<>(delta.getKey() + dodgeVelocities.getKey(), delta.getValue() + dodgeVelocities.getValue());
            }
            position = position.add(dodgeVelocities.getKey() * time, dodgeVelocities.getValue() * time);
        }

        interact = false;
        lastUpdated = currentNanoTime;

    }

    private boolean isBlockedBy(Sprite otherSprite, Rectangle2D plannedPosition)
    {
        if(otherSprite == this)
            return false;
        return otherSprite.isBlocker && otherSprite.getBoundary().intersects(plannedPosition);
        //           || !worldBorders.contains(position.getX() + velocityX * time, position.getY() + velocityY * time
    }

    private boolean isBlockedByOtherSprites(double deltaX, double deltaY)
    {
        Rectangle2D plannedPosition = new Rectangle2D(position.getX() + hitBoxOffsetX + deltaX, position.getY() + hitBoxOffsetY + deltaY, hitBoxWidth, hitBoxHeight);
        for (Sprite otherSprite : WorldView.getPassiveCollisionRelevantSpritesLayer())
            if (isBlockedBy(otherSprite, plannedPosition))
                return true;
        return false;
    }

    private Pair<Double, Double> calculateDodge(Rectangle2D plannedPosition, Direction direction)
    {
        String methodName = "calculateCollisionType() ";
        double playerLeftEdge = 0;
        double playerRightEdge = 0;
        double blockingSpriteLeftEdge = 0;
        double blockingSpriteRightEdge = 0;
        double velocityDodge = DODGE_VELOCITY;
        Rectangle2D blockingSprite = null;
        for (Sprite otherSprite : WorldView.getPassiveCollisionRelevantSpritesLayer())
            if (isBlockedBy(otherSprite, plannedPosition))
                blockingSprite = otherSprite.getBoundary();

        if (direction == NORTH || direction == SOUTH)
        {
            playerLeftEdge = plannedPosition.getMinX();
            playerRightEdge = plannedPosition.getMaxX();
            blockingSpriteLeftEdge = blockingSprite.getMinX();
            blockingSpriteRightEdge = blockingSprite.getMaxX();
        }
        else if (direction == Direction.WEST || direction == Direction.EAST)
        {
            playerLeftEdge = plannedPosition.getMaxY();
            playerRightEdge = plannedPosition.getMinY();
            blockingSpriteLeftEdge = blockingSprite.getMaxY();
            blockingSpriteRightEdge = blockingSprite.getMinY();
        }
        if (playerLeftEdge < blockingSpriteLeftEdge && playerRightEdge < blockingSpriteRightEdge)
        {
            if (direction == NORTH || direction == SOUTH)
                return new Pair<>(-velocityDodge, 0d);
            else
                return new Pair<>(0d, -velocityDodge);
        }
        else if (playerLeftEdge > blockingSpriteLeftEdge && playerRightEdge > blockingSpriteRightEdge)
        {
            if (direction == NORTH || direction == SOUTH)
                return new Pair<>(velocityDodge, 0d);
            else
                return new Pair<>(0d, velocityDodge);

        }
        return new Pair<>(0d, 0d);
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
        if (getBoundary().intersects(player.interactionArea)
                && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS

        )
        {
            if (debug)
                System.out.println(CLASSNAME + methodName + player.getName() + " interact with " + getName() + " by mouseclick.");
            if (actor != null &&
                    (actor.getSensorStatus().getOnInteraction_TriggerSprite() != NOTHING || actor.getSensorStatus().getOnInteraction_TriggerSensor() != NOTHING))
            {
                actor.onInteraction(player, currentNanoTime); //Passive reacts
                player.actor.setLastInteraction(currentNanoTime);
            }
            else if (!(getDialogueFileName().equals("dialogueFile") || getDialogueFileName().equals("none")))
            {
                WorldView.startConversation(dialogueFileName, initDialogueId, currentNanoTime);
                player.actor.setLastInteraction(currentNanoTime);
            }

        }

    }

    public boolean intersectsRelativeToWorldView(Point2D point)
    {
        String methodName = "intersectsRelativeToWorldView() ";
        //Uses Hitbox not sprite image
        //Rectangle2D intersectionHitbox = new Rectangle2D(position.getX() + hitBoxOffsetX - WorldView.getCamX(), position.getY() + hitBoxOffsetY - WorldView.getCamY(), hitBoxWidth, hitBoxHeight);

        Rectangle2D intersectionHitbox = new Rectangle2D(position.getX() - WorldView.getCamX(), position.getY() - WorldView.getCamY(), frameWidth, frameHeight);
        return intersectionHitbox.contains(point);
    }

    public boolean intersects(Sprite s)
    {
        return s.getBoundary().intersects(this.getBoundary());
    }

    public void render(GraphicsContext gc, Long now)
    {
        String methodName = "render()";

        if (getAnimated())
        {
            renderAnimated(gc, now);
        }
        else
        {
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
        if (frameJump >= 1 && !(isAtLastFrame() && animationEnds))
        {
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

    public Rectangle2D getBoundary()
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
