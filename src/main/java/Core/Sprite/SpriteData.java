package Core.Sprite;


public class SpriteData
{
    //Sprites
    final static int tileCodeIdx = 0;
    final static int nameIdx = 1;
    final static int spriteNameIdx = 2;
    final static int blockingIdx = 3;
    final static int layerIdx = 4;
    final static int fpsIdx = 5;
    final static int totalFramesIdx = 6;
    final static int colsIdx = 7;
    final static int rowsIdx = 8;
    final static int frameWidthIdx = 9;
    final static int frameHeightIdx = 10;
    final static int hitboxOffsetXIdx = 11;
    final static int hitboxOffsetYIdx = 12;
    final static int hitboxWidthIdx = 13;
    final static int hitboxHeightIdx = 14;
    final static int lightningSpriteNameIdx = 15;
    final static int dialogueIDIdx = 16;
    final static int dialogueFileIdx = 17;
    final static int unused0 = 18;
    final static int unused1 = 19;
    final static int unused2 = 20;
    final static int unused3 = 21;
    final static int unused4 = 22;
    //Just Actor
    final static int animationDurationIdx = 23;
    final static int velocityIdx = 24;
    final static int animationEndsIdx = 25;

    public String name, spriteName, lightningSprite, dialogueID, dialogieFile;
    public Boolean blocking, animationEnds = false;
    public Integer totalFrames, cols, rows, frameWidth, frameHeight, renderLayer, hitboxOffsetX, hitboxOffsetY, hitboxWidth, hitboxHeight, velocity;
    public Double animationDuration, fps;


    public SpriteData(String name, String spriteName, Boolean blocking, Double fps,
                      Integer totalFrames, Integer cols, Integer rows, Integer frameWidth, Integer frameHeight, Integer renderLayer,
                      Integer hitboxOffsetX, Integer hitboxOffsetY, Integer hitboxWidth, Integer hitboxHeight, String lightningSprite
            , String dialogueID, String dialogieFile
    )
    {
        this.name = name;
        this.spriteName = spriteName;
        this.blocking = blocking;
        this.fps = fps;
        this.totalFrames = totalFrames;
        this.cols = cols;
        this.rows = rows;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.renderLayer = renderLayer;
        this.hitboxOffsetX = hitboxOffsetX;
        this.hitboxOffsetY = hitboxOffsetY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.lightningSprite = lightningSprite;
        this.dialogieFile = dialogieFile;
        this.dialogueID = dialogueID;
    }

    public static SpriteData tileDefinition(String[] lineData) throws IndexOutOfBoundsException
    {
        try
        {
            Boolean blocking = Boolean.parseBoolean(lineData[blockingIdx]);
            Integer priority = Integer.parseInt(lineData[layerIdx]);
            Double fps = Double.parseDouble(lineData[fpsIdx]);
            Integer totalFrames = Integer.parseInt(lineData[totalFramesIdx]);
            Integer cols = Integer.parseInt(lineData[colsIdx]);
            Integer rows = Integer.parseInt(lineData[rowsIdx]);
            Integer frameWidth = Integer.parseInt(lineData[frameWidthIdx]);
            Integer frameHeight = Integer.parseInt(lineData[frameHeightIdx]);
            Integer hitboxOffsetX = Integer.parseInt(lineData[hitboxOffsetXIdx]);
            Integer hitboxOffsetY = Integer.parseInt(lineData[hitboxOffsetYIdx]);
            Integer hitboxWidth = Integer.parseInt(lineData[hitboxWidthIdx]);
            Integer hitboxHeight = Integer.parseInt(lineData[hitboxHeightIdx]);
            String lightningSprite = lineData[lightningSpriteNameIdx];
            return new SpriteData(lineData[nameIdx], lineData[spriteNameIdx], blocking, fps, totalFrames, cols, rows, frameWidth, frameHeight, priority, hitboxOffsetX, hitboxOffsetY, hitboxWidth, hitboxHeight, lightningSprite, lineData[dialogueIDIdx], lineData[dialogueFileIdx]);
        }
        catch (IndexOutOfBoundsException e)
        {
            StringBuilder stringBuilder = new StringBuilder();
            for(String s : lineData)
            {
                stringBuilder.append(s);
                stringBuilder.append(", ");
            }
            throw new IndexOutOfBoundsException("\nTile Definition failed with line: " + stringBuilder.toString());
        }

    }

    @Override
    public String toString()
    {
        return "SpriteData{" +
                ", spriteName='" + spriteName + '\'' +
                ", fps=" + fps +
                ", totalFrames=" + totalFrames +
                ", animationDuration=" + animationDuration +
                '}';
    }

    public static int getTileCodeIdx()
    {
        return tileCodeIdx;
    }

    public static int getNameIdx()
    {
        return nameIdx;
    }

    public static int getSpriteNameIdx()
    {
        return spriteNameIdx;
    }

    public static int getBlockingIdx()
    {
        return blockingIdx;
    }

    public static int getLayerIdx()
    {
        return layerIdx;
    }

    public static int getFpsIdx()
    {
        return fpsIdx;
    }

    public static int getTotalFramesIdx()
    {
        return totalFramesIdx;
    }

    public static int getColsIdx()
    {
        return colsIdx;
    }

    public static int getRowsIdx()
    {
        return rowsIdx;
    }

    public static int getFrameWidthIdx()
    {
        return frameWidthIdx;
    }

    public static int getFrameHeightIdx()
    {
        return frameHeightIdx;
    }

    public static int getHitboxOffsetXIdx()
    {
        return hitboxOffsetXIdx;
    }

    public static int getHitboxOffsetYIdx()
    {
        return hitboxOffsetYIdx;
    }

    public static int getHitboxWidthIdx()
    {
        return hitboxWidthIdx;
    }

    public static int getHitboxHeightIdx()
    {
        return hitboxHeightIdx;
    }

    public static int getLightningSpriteNameIdx()
    {
        return lightningSpriteNameIdx;
    }

    public static int getDialogueIDIdx()
    {
        return dialogueIDIdx;
    }

    public static int getDialogueFileIdx()
    {
        return dialogueFileIdx;
    }

    public static int getUnused0()
    {
        return unused0;
    }

    public static int getUnused1()
    {
        return unused1;
    }

    public static int getUnused2()
    {
        return unused2;
    }

    public static int getUnused3()
    {
        return unused3;
    }

    public static int getUnused4()
    {
        return unused4;
    }

    public static int getAnimationDurationIdx()
    {
        return animationDurationIdx;
    }

    public static int getVelocityIdx()
    {
        return velocityIdx;
    }

    public static int getAnimationEndsIdx()
    {
        return animationEndsIdx;
    }
}
