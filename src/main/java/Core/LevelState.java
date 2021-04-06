package Core;

import Core.GameTime.TimeMode;
import Core.Sprite.Sprite;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelState
{
    final private static String CLASSNAME = "LevelState/";
    String levelName;
    long day;
    private Rectangle2D borders;
    private List<Actor> actorList;
    private List<Sprite> actorSpritesLayer;
    private List<Sprite> passiveSpritesLayer;
    private List<Sprite> bottomLayer;
    private List<Sprite> middleLayer;
    private List<Sprite> topLayer;
    private Color shadowColor;
    private Map<String, WorldLoader.SpawnData> spawnPointsMap;
    private TimeMode timeMode;
    private boolean isValid = true;

    public LevelState(String levelName, long day, Rectangle2D borders, List<Sprite> activeSpritesLayer, List<Sprite> passiveSpritesLayer, List<Sprite> bottomLayer, List<Sprite> middleLayer, List<Sprite> topLayer, Color shadowColor, Map<String, WorldLoader.SpawnData> spawnPointsMap, TimeMode timeMode, List<Actor> activeActors)
    {
        this.levelName = levelName;
        this.day = day;
        this.borders = borders;
        this.actorSpritesLayer = activeSpritesLayer;
        this.passiveSpritesLayer = passiveSpritesLayer;
        this.bottomLayer = bottomLayer;
        this.middleLayer = middleLayer;
        this.topLayer = topLayer;
        this.shadowColor = shadowColor;
        this.spawnPointsMap = new HashMap<>(spawnPointsMap);
        this.timeMode = timeMode;
        this.actorList = activeActors;
    }

    @Override
    public String toString()
    {
        return levelName
                + " Day: " + day
                + " SpawnPoints: " + spawnPointsMap.toString()
          //      + " active Sprites: " + activeSpritesLayer.size()
          //      + " passive Sprites: " + passiveSpritesLayer.size()
                ;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public long getDay()
    {
        return day;
    }

    public void setDay(long day)
    {
        this.day = day;
    }

    public Rectangle2D getBorders()
    {
        return borders;
    }

    public List<Sprite> getActorSpritesLayer()
    {
        return actorSpritesLayer;
    }


    public List<Sprite> getPassiveSpritesLayer()
    {
        return passiveSpritesLayer;
    }

    public List<Sprite> getBottomLayer()
    {
        return bottomLayer;
    }

    public List<Sprite> getMiddleLayer()
    {
        return middleLayer;
    }

    public List<Sprite> getTopLayer()
    {
        return topLayer;
    }

    public Color getShadowColor()
    {
        return shadowColor;
    }

    public Map<String, WorldLoader.SpawnData> getSpawnPointsMap()
    {
        return spawnPointsMap;
    }

    public TimeMode getTimeMode()
    {
        return timeMode;
    }

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(boolean valid)
    {
        isValid = valid;
    }

    public List<Actor> getactorList()
    {
        return actorList;
    }
}
