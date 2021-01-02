package Core;

import Core.Configs.Config;
import Core.Enums.Direction;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.List;

import static Core.Configs.Config.*;
import static Core.Enums.TriggerType.NOTHING;
import static Core.GameVariables.getPlayer;
import static Core.GameVariables.player;

public class Sprite
{
    private static final String CLASSNAME = "Sprite/";
    Image baseimage;
    double basewidth; //width of whole sprite, in therms of animation multiple frames
    double baseheight;
    double positionX;//reference is upper left corner
    double positionY;
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
    private Boolean blockedByOtherSprite = false;
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

    private Rectangle2D calcInteractionRectangle()
    {
        String methodName = "calcInteractionRectangle() ";
        double interactionWidth = actor.getInteractionAreaWidth();
        double maxInteractionDistance = actor.getInteractionAreaDistance();
        double offsetX = actor.getInteractionAreaOffsetX();
        double offsetY = actor.getInteractionAreaOffsetY();

        switch (actor.getDirection()) {
            case NORTH:
                return new Rectangle2D(positionX + hitBoxOffsetX + hitBoxWidth / 2 - interactionWidth / 2 + offsetX, positionY + hitBoxOffsetY - maxInteractionDistance + offsetY, interactionWidth, maxInteractionDistance);
            case EAST:
                return new Rectangle2D(positionX + hitBoxOffsetX + hitBoxWidth + offsetX, positionY + hitBoxOffsetY + hitBoxHeight / 2 - interactionWidth / 2 + offsetY, maxInteractionDistance, interactionWidth);
            case SOUTH:
                return new Rectangle2D(positionX + hitBoxOffsetX + hitBoxWidth / 2 - interactionWidth / 2 + offsetX, positionY + hitBoxOffsetY + hitBoxHeight + offsetY, interactionWidth, maxInteractionDistance);
            case WEST:
                return new Rectangle2D(positionX + hitBoxOffsetX - maxInteractionDistance + offsetX, positionY + hitBoxOffsetY + hitBoxHeight / 2 - interactionWidth / 2 + offsetY, maxInteractionDistance, interactionWidth);
            case UNDEFINED:
                return null;
            default:
                throw new RuntimeException("calcInteractionRectangle: No Direction Set at " + name);
        }
    }

    public static Sprite createSprite(SpriteData tile, double x, double y)
    {
        String methodName = "createSprite() ";
        Sprite ca;
        try {
            if (tile.totalFrames > 1)
                ca = new Sprite(tile.spriteName, tile.fps, tile.totalFrames, tile.cols, tile.rows, tile.frameWidth, tile.frameHeight);
            else
                ca = new Sprite(tile.spriteName);
        } catch (IllegalArgumentException e) {
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

    public void update(Long currentNanoTime)
    {
        String methodName = "update() ";
        boolean debugMode = false;

        double time = (currentNanoTime - lastUpdated) / 1000000000.0;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - actor.getLastInteraction()) / 1000000000.0;
        Rectangle2D worldBorders = WorldView.getBorders();
        List<Sprite> activeSprites = WorldView.getPassiveCollisionRelevantSpritesLayer();
        double velocityX = actor.getVelocityX();
        double velocityY = actor.getVelocityY();
        Rectangle2D plannedPosition = new Rectangle2D(positionX + hitBoxOffsetX + velocityX * time, positionY + hitBoxOffsetY + velocityY * time, hitBoxWidth, hitBoxHeight);
        Pair<Double, Double> dodgeVelocities = new Pair<>(0d, 0d);

        String initGeneralStatusFrame = "";
        if (actor != null)
            initGeneralStatusFrame = actor.generalStatus;

        for (Sprite otherSprite : activeSprites) {
            if (otherSprite == this ||
                    otherSprite.actor == actor //if actor has multiple sprites they are usually congruent
            )
                continue;

            //Collision
            if
            (otherSprite.isBlocker && otherSprite.getBoundary().intersects(plannedPosition)
                    || !worldBorders.contains(positionX + velocityX * time, positionY + velocityY * time)
            ) {
                if (this == WorldView.getPlayer()) {
                    var delta = calculateCollisionType(plannedPosition, this.actor.getDirection(), otherSprite.getBoundary());
                    dodgeVelocities = new Pair<>(delta.getKey() + dodgeVelocities.getKey(), delta.getValue() + dodgeVelocities.getValue());
                }
                blockedByOtherSprite = true;
            }

            //Calculate Interaction Area
            if (interact || actor.sensorStatus.onInRange_TriggerSprite != NOTHING || getName().equalsIgnoreCase("player"))
                interactionArea = calcInteractionRectangle();

            //Interact within interaction area
            if (interact
                    && otherSprite.getBoundary().intersects(interactionArea)
                    && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS) {

                if (otherSprite.actor != null &&
                        (otherSprite.actor.sensorStatus.onInteraction_TriggerSprite != NOTHING
                                || otherSprite.actor.sensorStatus.onInteraction_TriggerSensor != NOTHING)) {
                    otherSprite.actor.onInteraction(this, currentNanoTime);
                    actor.setLastInteraction(currentNanoTime);
                    interact = false;
                }
                //dialogue_files/descriptions ???
                else if (!(otherSprite.dialogueFileName.equals("dialogueFile") || otherSprite.dialogueFileName.equals("none"))) {
                    WorldView.startConversation(otherSprite.dialogueFileName, otherSprite.initDialogueId, currentNanoTime);
                    interact = false;
                }
            }

            //In range
            if (otherSprite.actor != null
                    && actor.sensorStatus.onInRange_TriggerSprite != NOTHING
                    && otherSprite.getBoundary().intersects(interactionArea)) {
                actor.onInRange(otherSprite, currentNanoTime);
            }

            //Intersect
            if (intersects(otherSprite) && (actor.sensorStatus.onIntersection_TriggerSprite != NOTHING || actor.sensorStatus.onIntersection_TriggerSensor != NOTHING)) {
                actor.onIntersection(otherSprite, currentNanoTime);
            }
        }


        //check if status was changed from other triggers, just if not do OnUpdate
        if (actor != null && initGeneralStatusFrame.equals(actor.generalStatus)) {
            if (actor.sensorStatus.onUpdate_TriggerSprite != NOTHING && !actor.sensorStatus.onUpdateToStatusSprite.equals(actor.generalStatus))
                actor.onUpdate(currentNanoTime);
            if (actor.sensorStatus.onUpdate_TriggerSensor != NOTHING && !actor.sensorStatus.onUpdate_StatusSensor.equals(actor.sensorStatus.statusName))
                actor.onUpdate(currentNanoTime);
        }

        if (!blockedByOtherSprite || (DEBUG_NO_WALL && this == WorldView.getPlayer())) {
            positionX += velocityX * time;
            positionY += velocityY * time;
        }
        else {
            positionX += dodgeVelocities.getKey() * time;
            positionY += dodgeVelocities.getValue() * time;
        }

        interact = false;
        blockedByOtherSprite = false;
        lastUpdated = currentNanoTime;

    }

    private Pair<Double, Double> calculateCollisionType(Rectangle2D moving, Direction direction, Rectangle2D standing)
    {
        String methodName = "calculateCollisionType() ";
        double playerLeftEdge = 0;
        double playerRightEdge = 0;
        double otherSpriteLeftEdge = 0;
        double otherSpriteRightEdge = 0;
        double velocityDodge = DODGE_VELOCITY;

        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            playerLeftEdge = moving.getMinX();
            playerRightEdge = moving.getMaxX();
            otherSpriteLeftEdge = standing.getMinX();
            otherSpriteRightEdge = standing.getMaxX();
        }
        else if (direction == Direction.WEST || direction == Direction.EAST) {
            playerLeftEdge = moving.getMaxY();
            playerRightEdge = moving.getMinY();
            otherSpriteLeftEdge = standing.getMaxY();
            otherSpriteRightEdge = standing.getMinY();
        }
        if (playerLeftEdge < otherSpriteLeftEdge && playerRightEdge < otherSpriteRightEdge) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH)
                return new Pair<>(-velocityDodge, 0d);
            else//(direction == Direction.WEST || direction == Direction.EAST)
                return new Pair<>(0d, -velocityDodge);
        }
        else if (playerLeftEdge > otherSpriteLeftEdge && playerRightEdge > otherSpriteRightEdge) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH)
                return new Pair<>(velocityDodge, 0d);
            else//(direction == Direction.WEST || direction == Direction.EAST)
                return new Pair<>(0d, velocityDodge);

        }
        //Case Player hitbox totally covered
        else if (playerLeftEdge >= otherSpriteLeftEdge && playerRightEdge <= otherSpriteRightEdge) {
            //System.out.println(CLASSNAME + methodName + this.name + " totally covered" + " playerLeft:" + playerLeftEdge + " playerRight:" + playerRightEdge + " otherLeft:" + otherSpriteLeftEdge + " otherRight:" + otherSpriteRightEdge);
        }
        //Case Other Sprite totally covered
        else if (playerLeftEdge <= otherSpriteLeftEdge && playerRightEdge >= otherSpriteRightEdge) {
            //System.out.println(CLASSNAME + methodName + this.name + " obastacale small" + " playerLeft:" + playerLeftEdge + " playerRight:" + playerRightEdge + " otherLeft:" + otherSpriteLeftEdge + " otherRight:" + otherSpriteRightEdge);
        }
        else
            System.out.println(CLASSNAME + methodName + "uncated" + " playerLeft:" + playerLeftEdge + " playerRight:" + playerRightEdge + " otherLeft:" + otherSpriteLeftEdge + " otherRight:" + otherSpriteRightEdge);

        return new Pair<>(0d, 0d);
    }

    public void onClick(Long currentNanoTime)
    {
        String methodName = "onClick(Long) ";
        boolean debug = false;

        if (debug)
            System.out.println(CLASSNAME + methodName + name + " clicked: " + actor.actorInGameName);

        //Sprite is clicked by player and in Range
        Sprite player = WorldView.getPlayer();
        player.calcInteractionRectangle();
        double elapsedTimeSinceLastInteraction = (currentNanoTime - player.actor.getLastInteraction()) / 1000000000.0;
        if (getBoundary().intersects(player.interactionArea)
                && elapsedTimeSinceLastInteraction > Config.TIME_BETWEEN_INTERACTIONS

        ) {
            if (debug)
                System.out.println(CLASSNAME + methodName + player.getName() + " interact with " + getName() + " by mouseclick.");
            if (actor != null &&
                    (actor.sensorStatus.onInteraction_TriggerSprite != NOTHING || actor.sensorStatus.onInteraction_TriggerSensor != NOTHING)) {
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
        //Uses Hitbox not sprite image
        //Rectangle2D intersectionHitbox = new Rectangle2D(positionX + hitBoxOffsetX - WorldView.getCamX(), positionY + hitBoxOffsetY - WorldView.getCamY(), hitBoxWidth, hitBoxHeight);

        Rectangle2D intersectionHitbox = new Rectangle2D(positionX - WorldView.getCamX(), positionY - WorldView.getCamY(), frameWidth, frameHeight);
        return intersectionHitbox.contains(point);
    }

    public boolean intersects(Sprite s)
    {
        return s.getBoundary().intersects(this.getBoundary());
    }

    public void render(GraphicsContext gc, Long now)
    {
        String methodName = "render()";

        if (getAnimated()) {
            renderAnimated(gc, now);
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
        gc.strokeRect(positionX + hitBoxOffsetX, positionY + hitBoxOffsetY, hitBoxWidth, hitBoxHeight);
        if (interactionArea != null)
            gc.strokeRect(interactionArea.getMinX(), interactionArea.getMinY(), interactionArea.getWidth(), interactionArea.getHeight());
    }

    public void renderSimple(GraphicsContext gc)
    {
        String methodName = "renderSimple() ";
        gc.drawImage(baseimage, positionX, positionY);
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

        gc.drawImage(baseimage, currentCol * frameWidth, currentRow * frameHeight, frameWidth, frameHeight, positionX, positionY, frameWidth, frameHeight); //(img, srcX, srcY, srcWidht, srcHeight, TargetX, TargetY, TargetWidht, TargetHeight)

    }

    private boolean isAtLastFrame()
    {
        return (currentRow * cols) + currentCol >= totalFrames - 1;
    }

    public Rectangle2D getBoundary()
    {
        return new Rectangle2D(positionX + hitBoxOffsetX, positionY + hitBoxOffsetY, hitBoxWidth, hitBoxHeight);
    }

    public String toString()
    {
        return name + " Position: [" + positionX + "," + positionY + "]"
                ;
    }

    public void setBlocker(Boolean blocker)
    {
        isBlocker = blocker;
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
        positionX = x;
        positionY = y;
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

    public void setInteract(Boolean interact)
    {
        this.interact = interact;
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

    public double getHitBoxHeight()
    {
        return hitBoxHeight;
    }

    public String getLightningSpriteName()
    {
        return lightningSpriteName;
    }

    public void setLightningSpriteName(String lightningSpriteName)
    {
        this.lightningSpriteName = lightningSpriteName;
    }

    public void setAnimationEnds(Boolean animationEnds)
    {
        this.animationEnds = animationEnds;
    }

    public Actor getActor()
    {
        return actor;
    }

    public static String getClassname()
    {
        return CLASSNAME;
    }

    public double getBasewidth()
    {
        return basewidth;
    }

    public double getBaseheight()
    {
        return baseheight;
    }

    public double getPositionX()
    {
        return positionX;
    }

    public double getPositionY()
    {
        return positionY;
    }

    public double getFps()
    {
        return fps;
    }

    public int getTotalFrames()
    {
        return totalFrames;
    }

    public int getCols()
    {
        return cols;
    }

    public int getRows()
    {
        return rows;
    }

    public double getFrameWidth()
    {
        return frameWidth;
    }

    public double getFrameHeight()
    {
        return frameHeight;
    }

    public Boolean getBlocker()
    {
        return isBlocker;
    }

    public int getLayer()
    {
        return layer;
    }

    public void setLayer(int layer)
    {
        this.layer = layer;
    }

    public void setFps(double fps)
    {
        this.fps = fps;
    }

    public void setTotalFrames(int totalFrames)
    {
        this.totalFrames = totalFrames;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public void setFrameWidth(double frameWidth)
    {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(double frameHeight)
    {
        this.frameHeight = frameHeight;
    }

    public void setHitBoxWidth(double hitBoxWidth)
    {
        this.hitBoxWidth = hitBoxWidth;
    }

    public void setHitBoxHeight(double hitBoxHeight)
    {
        this.hitBoxHeight = hitBoxHeight;
    }

    public void setActor(Actor actor)
    {
        this.actor = actor;
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
}
