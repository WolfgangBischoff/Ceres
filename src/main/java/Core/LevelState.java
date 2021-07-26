package Core;

import Core.GameTime.ClockMode;
import Core.Sprite.Sprite;
import Core.WorldView.MapTimeData;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.logging.Level;

public class LevelState
{
    final private static String CLASSNAME = "LevelState/";
    String levelName;
    long day;
    private Rectangle2D borders;
    private List<Actor> actorList;
    private List<Sprite> actorSpritesLayer = new ArrayList<>();
    private List<Sprite> passiveSpritesLayer;
    private List<Sprite> bottomLayer;
    private List<Sprite> middleLayer;
    private List<Sprite> topLayer;
    private Color shadowColor;
    private Map<String, WorldLoader.SpawnData> spawnPointsMap;
    private ClockMode clockMode;
    private MapTimeData mapTimeData;
    private Set<String> loadedIncludes = new HashSet<>();

    public static LevelState empty(String levelName)
    {
        return new LevelState(levelName);
    }

    private LevelState(String levelName)
    {
        this.levelName = levelName;
        this.day = GameVariables.gameDateTime().getDays();
    }

    public LevelState(String levelName,  Rectangle2D borders, List<Sprite> activeSpritesLayer, List<Sprite> passiveSpritesLayer, List<Sprite> bottomLayer, List<Sprite> middleLayer, List<Sprite> topLayer, Color shadowColor, Map<String, WorldLoader.SpawnData> spawnPointsMap, ClockMode clockMode, List<Actor> activeActors, MapTimeData mapTimeData)
    {
        this.levelName = levelName;
        this.day = GameVariables.gameDateTime().getDays();
        this.borders = borders;
        this.actorSpritesLayer = activeSpritesLayer;
        this.passiveSpritesLayer = passiveSpritesLayer;
        this.bottomLayer = bottomLayer;
        this.middleLayer = middleLayer;
        this.topLayer = topLayer;
        this.shadowColor = shadowColor;
        this.spawnPointsMap = new HashMap<>(spawnPointsMap);
        this.clockMode = clockMode;
        this.actorList = activeActors;
        this.mapTimeData = mapTimeData;
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

    public ClockMode getClockMode()
    {
        return clockMode;
    }

    public List<Actor> getactorList()
    {
        return actorList;
    }

    public List<Actor> getActorList()
    {
        return actorList;
    }

    public MapTimeData getMapTimeData()
    {
        return mapTimeData;
    }

    public Set<String> getLoadedIncludes()
    {
        return loadedIncludes;
    }

    public void setBorders(Rectangle2D borders)
    {
        this.borders = borders;
    }

    public void setActorList(List<Actor> actorList)
    {
        this.actorList = actorList;
    }

    public void setActorSpritesLayer(List<Sprite> actorSpritesLayer)
    {
        this.actorSpritesLayer = actorSpritesLayer;
    }

    public void setPassiveSpritesLayer(List<Sprite> passiveSpritesLayer)
    {
        this.passiveSpritesLayer = passiveSpritesLayer;
    }

    public void setBottomLayer(List<Sprite> bottomLayer)
    {
        this.bottomLayer = bottomLayer;
    }

    public void setMiddleLayer(List<Sprite> middleLayer)
    {
        this.middleLayer = middleLayer;
    }

    public void setTopLayer(List<Sprite> topLayer)
    {
        this.topLayer = topLayer;
    }

    public void setShadowColor(Color shadowColor)
    {
        this.shadowColor = shadowColor;
    }

    public void setSpawnPointsMap(Map<String, WorldLoader.SpawnData> spawnPointsMap)
    {
        this.spawnPointsMap = spawnPointsMap;
    }

    public void setClockMode(ClockMode clockMode)
    {
        this.clockMode = clockMode;
    }

    public void setMapTimeData(MapTimeData mapTimeData)
    {
        this.mapTimeData = mapTimeData;
    }

    public void addLoadedIncludes(String loadedInclude)
    {
        loadedIncludes.add(loadedInclude);



    }
}
