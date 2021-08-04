package Core;

import java.util.*;

public class LevelState
{
    final private static String CLASSNAME = "LevelState/";
    String levelName;
    private List<Actor> actorList = new ArrayList<>();
    private Set<String> loadedIncludes = new HashSet<>();
    private Map<String, String> levelStringVariables = new HashMap<>();

    private LevelState(String levelName)
    {
        this.levelName = levelName;
    }

    public static LevelState empty(String levelName)
    {
        return new LevelState(levelName);
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
