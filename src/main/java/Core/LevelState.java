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
    private List<Actor> actorList= new ArrayList<>();
    private Set<String> loadedIncludes = new HashSet<>();

    public static LevelState empty(String levelName)
    {
        return new LevelState(levelName);
    }

    private LevelState(String levelName)
    {
        this.levelName = levelName;
        //this.day = GameVariables.gameDateTime().getDays();
    }

    @Override
    public String toString()
    {
        return "LevelState{" +
                "levelName='" + levelName + '\'' +
                ", actorList=" + actorList +
                ", loadedIncludes=" + loadedIncludes +
                '}';
    }

    public List<Actor> getactorList()
    {
        return actorList;
    }

    public Set<String> getLoadedIncludes()
    {
        return loadedIncludes;
    }

    public void setActorList(List<Actor> actorList)
    {
        this.actorList = actorList;
    }

    public void addLoadedIncludes(String loadedInclude)
    {
        loadedIncludes.add(loadedInclude);
    }
}
