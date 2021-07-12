package Core;

import Core.ActorSystem.ActorMonitor;
import Core.ActorSystem.GlobalActorsManager;
import Core.Configs.Config;
import Core.Enums.Direction;
import Core.GameTime.ClockMode;
import Core.Sprite.Sprite;
import Core.Sprite.SpriteData;
import Core.WorldView.WorldView;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static Core.Configs.Config.*;

public class WorldLoader
{
    private static final String CLASSNAME = "WorldLoader/";
    private static final Set<String> keywords = new HashSet<>();

    String levelName;
    String spawnId;
    Sprite player;
    Color shadowColor;
    List<Sprite> passivLayer = new ArrayList<>();
    List<Sprite> actorSprites = new ArrayList<>();
    List<Actor> actorsList = new ArrayList<>();
    List<Sprite> bttmLayer = new ArrayList<>();
    List<Sprite> mediumLayer = new ArrayList<>();
    List<Sprite> upperLayer = new ArrayList<>();
    List<Sprite> topLayer = new ArrayList<>();
    Set<String> loadedTileIdsSet = new HashSet<>();
    Map<String, SpriteData> tileDataMap = new HashMap<>();
    Map<String, ActorData> actorDataMap = new HashMap<>();
    Map<String, Actor> globalActorsMap = new HashMap<>();
    Map<String, SpawnData> spawnPointsMap = new HashMap<>();
    ActorMonitor actorMonitor = new ActorMonitor();
    Map<String, ActorGroupData> actorGroupDataMap = new HashMap<>();
    String readMode;
    int maxVerticalTile = 0;
    int currentVerticalTile = 0;
    int currentHorizontalTile = 0;
    int maxHorizontalTile = 0;
    ClockMode clockMode = ClockMode.RUNNING;
    private Rectangle2D borders;

    public WorldLoader()
    {
        if (keywords.isEmpty())
        {
            keywords.add(KEYWORD_NEW_LAYER);
            keywords.add(KEYWORD_ACTORS);
            keywords.add(KEYWORD_TILEDEF);
            keywords.add(KEYWORD_PASSIV_LAYER);
            keywords.add(KEYWORD_WORLDSHADOW);
            keywords.add(KEYWORD_GROUPS);
            keywords.add(KEYWORD_SPAWNPOINTS);
            keywords.add(KEYWORD_INCLUDE);
            keywords.add(KEYWORD_POSITION);
            keywords.add(KEYWORD_GLOBAL_SYSTEM_ACTOR);
            keywords.add(KEYWORD_TIME_MODE);
            keywords.add(KEYWORD_LOG);
        }

    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static Set<String> getKeywords()
    {
        return keywords;
    }

    private void readFile(String fileName)
    {
        String methodName = "readFile() ";
        boolean debug = false;
        List<String[]> leveldata = Utilities.readAllLineFromTxt(STAGE_FILE_PATH + fileName + CSV_POSTFIX);

        readMode = null;
        if (debug)
            System.out.println(CLASSNAME + methodName + "begin read file: " + fileName);
        for (int i = 0; i < leveldata.size(); i++)
        {
            String[] lineData = leveldata.get(i);
            try
            {
                readLine(lineData);
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : lineData)
                    stringBuilder.append(s).append("; ");
                throw new RuntimeException(e.getMessage() + "\nRead Mode: " + readMode + "\nat\t" + stringBuilder.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            }
        }

        if (debug)
            System.out.println(CLASSNAME + methodName + "finished read file: " + fileName);
    }

    public void load(String levelName, String spawnId)
    {
        String methodName = "load() ";
        this.levelName = levelName;
        this.spawnId = spawnId;
        readFile(this.levelName);
        borders = new Rectangle2D(0, 0, (maxHorizontalTile + 1) * 64, (maxVerticalTile) * 64);

        if (loadedTileIdsSet.size() > 0)
            System.out.println(CLASSNAME + methodName + " found unused tile or actor definitions " + loadedTileIdsSet + " while loading: " + levelName);
        loadedTileIdsSet.clear();

    }

    private void readLine(String[] lineData)
    {
        String methodName = "readLine() ";

        if (keywords.contains(lineData[0].toLowerCase()))
        {
            readMode = lineData[0].toLowerCase();
            currentVerticalTile = 0;
            return;
        }

        //process line according to keyword
        switch (readMode)
        {
            case KEYWORD_TILEDEF:
                tileDataMap.put(lineData[SpriteData.getTileCodeIdx()], SpriteData.tileDefinition(lineData));
                loadedTileIdsSet.add(lineData[SpriteData.getTileCodeIdx()]);
                break;
            case KEYWORD_NEW_LAYER:
                readLineOfTiles(lineData, false);
                break;
            case KEYWORD_PASSIV_LAYER:
                readLineOfTiles(lineData, true);
                break;
            case KEYWORD_ACTORS:
                readActorData(lineData);
                break;
            case KEYWORD_WORLDSHADOW:
                shadowColor = readWorldShadow(lineData);
                break;
            case KEYWORD_GROUPS:
                readActorGroups(lineData);
                break;
            case KEYWORD_SPAWNPOINTS:
                readSpawnPoint(lineData);
                break;
            case KEYWORD_INCLUDE:
                String readModeTmp = readMode;
                readInclude(lineData);
                readMode = readModeTmp;
                break;
            case KEYWORD_POSITION:
                readPosition(lineData);
                break;
            case KEYWORD_GLOBAL_SYSTEM_ACTOR:
                getGlobalSystemActor(lineData);
                break;
            case KEYWORD_TIME_MODE:
                clockMode = readTimeMode(lineData);
                break;
            case KEYWORD_LOG:
                log(lineData);
                break;
            default:
                throw new RuntimeException(CLASSNAME + methodName + "readMode unknown: " + readMode);
        }

    }

    private void log(String[] linedate)
    {
        System.out.print(CLASSNAME + "loaded: ");
        for (String s : linedate)
            System.out.print(s);
        System.out.println();
    }

    private ClockMode readTimeMode(String[] linedata)
    {
        return ClockMode.of(linedata[0]);
    }

    private void getGlobalSystemActor(String[] linedata)
    {
        String methodName = "getGlobalSystemActor() ";
        GlobalActorsManager.loadGlobalSystem(linedata[0]);
        List<String> actorIds = Arrays.asList(linedata).subList(1, linedata.length);
        globalActorsMap.putAll(GlobalActorsManager.getGlobalActorsWithStatus(actorIds));
        loadedTileIdsSet.addAll(actorIds.stream().map(string -> string.split(",")[0].trim()).collect(Collectors.toList()));//remove additional status data. eg: medic_,windo
    }

    private void readPosition(String[] lineData)
    {
        String methodName = "readPosition() ";
        String actorId = lineData[0];
        int xPos = Integer.parseInt(lineData[1]);
        int yPos = Integer.parseInt(lineData[2]);
        Actor actor = createActor(actorId, 0, 0);

        actorSprites.addAll(actor.spriteList);
        actorsList.add(actor);

        List<SpriteData> spriteDataList = actor.spriteDataMap.get(actor.compoundStatus);

        for (int j = 0; j < spriteDataList.size(); j++)
        {
            actor.spriteList.get(j).setPosition(xPos, yPos);
            addToCollisionLayer(actor.spriteList.get(j), spriteDataList.get(j).renderLayer);
            loadedTileIdsSet.remove(actorId);//Check for ununsed Definitions
            //System.out.println(CLASSNAME + methodName + actor.spriteList.get(j).getPositionX() +" "+ actor.spriteList.get(j).getPositionY());
        }
    }

    private void readInclude(String[] lineData)
    {
        String methodName = "readInclude()";
        boolean debug = false;
        int includeFilePathIdx = 0;
        int includeConditionTypeIdx = 1;
        int includeConditionParamsStartIdx = 2;

        //Check include condition
        String condition = lineData[includeConditionTypeIdx];
        switch (condition)
        {
            case INCLUDE_CONDITION_suspicion_lessequal:
                int suspicionThreshold = Integer.parseInt(lineData[includeConditionParamsStartIdx]);
                int currentSuspicion = GameVariables.getPlayerMaM_dayStart();
                if (currentSuspicion <= suspicionThreshold)//condition met
                {
                    if (debug)
                        System.out.println(CLASSNAME + methodName + "" + Arrays.toString(lineData) + " " + currentSuspicion + " / " + suspicionThreshold);
                    break;
                }
                else
                    return;
            case INCLUDE_CONDITION_day_greaterequal:
                int dayThreshold = Integer.parseInt(lineData[includeConditionParamsStartIdx]);
                long currentDay = GameVariables.gameDateTime().getDays();
                if (currentDay >= dayThreshold)
                    break;
                else
                    return;
            case INCLUDE_CONDITION_IF:
                List<Pair<String, String>> params = Utilities.readParameterPairs(Arrays.copyOfRange(lineData, includeConditionParamsStartIdx, lineData.length));
                boolean allVariabelsTrue = true;
                for (Pair<String, String> pair : params)
                    if (!GameVariables.getGenericVariableManager().getValue(pair.getKey()).equals(pair.getValue()))
                        allVariabelsTrue = false;
                if (allVariabelsTrue)
                    break;
                else
                    return;

            case INCLUDE_CONDITION_IF_NOT:
                List<Pair<String, String>> paramsnot = Utilities.readParameterPairs(Arrays.copyOfRange(lineData, includeConditionParamsStartIdx, lineData.length));
                boolean allVariabelsTrue2 = true;
                for (Pair<String, String> pair : paramsnot)
                    if (GameVariables.getGenericVariableManager().getValue(pair.getKey()).equals(pair.getValue()))
                        allVariabelsTrue2 = false;
                if (allVariabelsTrue2)
                    break;
                else
                    return;

            default:
                throw new RuntimeException(CLASSNAME + methodName + " Include Condition unknown: " + condition);

        }

        readFile(lineData[includeFilePathIdx]);

    }

    private void readSpawnPoint(String[] lineData)
    {
        String methodName = "readSpawnPoint()";
        int spawnIdIdx = 0;
        int spawnXId = 1;
        int spawnYId = 2;
        int directionIdx = 3;
        Integer x = Integer.parseInt(lineData[spawnXId]);
        Integer y = Integer.parseInt(lineData[spawnYId]);
        Direction direction = Direction.of(lineData[directionIdx]);
        SpawnData spawnData = new SpawnData(x, y, direction);
        spawnPointsMap.put(lineData[spawnIdIdx], spawnData);
    }

    private void readActorGroups(String[] lineData)
    {
        String methodName = "readActorGroups(String[])";
        boolean debug = false;

        int groupName_Idx = 0;
        int groupLogic_Idx = 1;
        int dependentGroupName_Idx = 2;
        int start_idx_memberIds = 3;
        //System.out.println(CLASS_NAME + methodName + Arrays.toString(lineData));
        actorMonitor.getGroupToLogicMap().put(lineData[groupName_Idx], lineData[groupLogic_Idx]);
        actorMonitor.getGroupIdToInfluencedGroupIdMap().put(lineData[groupName_Idx], lineData[dependentGroupName_Idx]);

        //map for all contained group members in which groups they are: actor -> groups
        ActorGroupData actorGroupData;
        for (int membersIdx = start_idx_memberIds; membersIdx < lineData.length; membersIdx++)
        {
            String actorId = lineData[membersIdx];
            if (!actorGroupDataMap.containsKey(actorId))
            {
                actorGroupDataMap.put(actorId, new ActorGroupData());
            }
            actorGroupData = actorGroupDataMap.get(actorId);
            actorGroupData.memberOfGroups.add(lineData[groupName_Idx]);
        }

        if (debug)
        {
            for (Map.Entry<String, ActorGroupData> actorData : actorGroupDataMap.entrySet())
                System.out.println(CLASSNAME + methodName + actorData.getKey() + " " + actorData.getValue().memberOfGroups);
        }


    }

    private Color readWorldShadow(String[] lineData)
    {
        if (lineData[0].equalsIgnoreCase("EMERGENCY_LIGHT"))
            return COLOR_EMERGENCY_LIGHT;
        else if (lineData[0].equalsIgnoreCase("none"))
            return null;
        throw new RuntimeException("Shadow Layer unknown: " + lineData[0]);
    }

    private void addToCollisionLayer(Sprite sprite, int layer)
    {
        sprite.setLayer(layer);
        switch (layer)
        {
            case 0:
                bttmLayer.add(sprite);
                break;
            case 1:
                mediumLayer.add(sprite);
                break;
            case 2:
                upperLayer.add(sprite);
                break;
            case 3:
                topLayer.add(sprite);
                break;
            default:
                throw new RuntimeException("Layer not defined");
        }
    }

    private void readLineOfTiles(String[] lineData, Boolean isPassiv) throws IllegalArgumentException
    {
        String methodName = "readLineOfTiles() ";
        String lineNumber = "[not set]";


        //from left to right, reads tile codes
        for (currentHorizontalTile = 0; currentHorizontalTile < lineData.length; currentHorizontalTile++)
        {
            //if first column is line number
            if (currentHorizontalTile == 0 && lineData[currentHorizontalTile].chars().allMatch(x -> Character.isDigit(x)))
            {
                lineNumber = lineData[0];
                lineData = Arrays.copyOfRange(lineData, 1, lineData.length);
            }

            //Is Tile
            if (tileDataMap.containsKey(lineData[currentHorizontalTile]))
            {
                SpriteData tile = tileDataMap.get(lineData[currentHorizontalTile]);
                Sprite ca;
                try
                {
                    ca = Sprite.createSprite(tile, 64 * currentHorizontalTile, currentVerticalTile * 64);
                }
                catch (IllegalArgumentException e)
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s : lineData)
                    {
                        stringBuilder.append(s).append(" ");
                    }
                    throw new IllegalArgumentException("\nLine: " + stringBuilder.toString() +
                            "\n " + lineData[currentHorizontalTile] + " ===> /res/img/" + tile.spriteName + ".png" + " not found");
                }


                if (isPassiv)
                    passivLayer.add(ca);
                else
                    addToCollisionLayer(ca, tile.renderLayer);
            }
            //Is Actor that is just relevant on this stage
            else if (actorDataMap.containsKey(lineData[currentHorizontalTile]))
            {
                Actor actor = createActor(lineData[currentHorizontalTile]);
                actorSprites.addAll(actor.spriteList);
                actorsList.add(actor);
                List<SpriteData> spriteDataList = actor.spriteDataMap.get(actor.compoundStatus);
                for (int j = 0; j < spriteDataList.size(); j++)
                    addToCollisionLayer(actor.spriteList.get(j), spriteDataList.get(j).renderLayer);
            }
            //Is Actor of global System
            else if (globalActorsMap.containsKey(lineData[currentHorizontalTile]))
            {
                Actor actor = globalActorsMap.get(lineData[currentHorizontalTile]);
                actor.getSpriteList().forEach(sprite ->
                {
                    sprite.setPosition(currentHorizontalTile * 64, currentVerticalTile * 64);
                });

                actorSprites.addAll(actor.spriteList);
                actorsList.add(actor);
                List<SpriteData> spriteDataList = actor.spriteDataMap.get(actor.compoundStatus);
                for (int j = 0; j < spriteDataList.size(); j++)
                    addToCollisionLayer(actor.spriteList.get(j), spriteDataList.get(j).renderLayer);
            }
            else if (isPassiv && lineData[currentHorizontalTile].equals(MAPDEFINITION_EMPTY))
            {
                //Do nothing => White
            }
            else if (!lineData[currentHorizontalTile].equals(Config.MAPDEFINITION_EMPTY))
                System.out.println("WorldLoader readTile: tile definition not found: " + lineData[currentHorizontalTile] + " in line " + lineNumber + " column " + currentHorizontalTile);

            loadedTileIdsSet.remove(lineData[currentHorizontalTile]); //For usage check of defined tiles and actors
            maxHorizontalTile = Math.max(currentHorizontalTile, maxHorizontalTile);
        }

        currentVerticalTile++;
        if (maxVerticalTile < currentVerticalTile)
            maxVerticalTile = currentVerticalTile;
    }

    private Actor createActor(String actorId)
    {
        return createActor(actorId, currentHorizontalTile, currentVerticalTile);
    }

    private Actor createActor(String actorId, Integer x, Integer y)
    {
        ActorData actorData = actorDataMap.get(actorId);

        //foreach Sprite Data add Sprite to layer, Actor save sprite
        Actor actor = new Actor(actorData.actorFileName, actorData.actorInGameName, actorData.generalStatus, actorData.sensor_status, actorData.direction);
        actor.setActorId(actorId);
        actor.updateCompoundStatus();
        List<SpriteData> spriteDataList = actor.spriteDataMap.get(actor.compoundStatus);
        if (spriteDataList == null)
            throw new RuntimeException("General status \"" + actor.compoundStatus + "\" not found in: " + actor.spriteDataMap.keySet());
        actor.actorMonitor = actorMonitor;

        //check for actorgroup Data
        ActorGroupData actorGroupData = actorGroupDataMap.get(actorId);
        if (actorGroupData != null)
        {
            actor.memberActorGroups.addAll(actorGroupData.memberOfGroups);
            for (String groupName : actor.memberActorGroups)
                actorMonitor.addActorToActorSystem(groupName, actor);
        }

        //Create initial Sprites of Actor
        for (int j = 0; j < spriteDataList.size(); j++)
        {
            Sprite actorSprite;
            SpriteData spriteData = spriteDataList.get(j);
            actorSprite = Sprite.createSprite(spriteData, x * 64, y * 64);
            actorSprite.setActor(actor);
            actorSprite.setAnimationEnds(spriteData.animationEnds);
            actor.setVelocity(spriteData.velocity);//Set as often as Sprites exist?
            actor.addSprite(actorSprite);
        }
        return actor;
    }

    private void readActorData(String[] lineData)
    {
        String methodName = "readActorData() ";
        //Reads sprite data from given status and add to tile definition, later actor will be added
        int actorCodeIdx = 0;
        int actorFileNameIdx = 1;
        int actorIngameNameIdx = 2;
        int sprite_statusIdx = 3;
        int sensor_statusIdx = 4;
        int directionIdx = 5;

        Direction direction = Direction.of(lineData[directionIdx]);
        ActorData actorData = new ActorData(lineData[actorFileNameIdx], lineData[actorIngameNameIdx], lineData[sprite_statusIdx], lineData[sensor_statusIdx], direction);
        actorDataMap.put(lineData[actorCodeIdx], actorData);

        //Player start position is not based on tile schema
        if (actorData.actorFileName.equals(ACTOR_DIRECTORY_PATH + "player"))
            createPlayer(actorData);
        else
            loadedTileIdsSet.add(lineData[actorCodeIdx]);//Player is not defined by layers
    }

    private void createPlayer(ActorData actorData)
    {
        String methodName = "createPlayer(ActorData) ";
        SpawnData playerSpawn;
        if (spawnPointsMap.containsKey(spawnId))
            playerSpawn = spawnPointsMap.get(spawnId);
        else
            throw new RuntimeException("Spawn Point " + spawnId + " not set in " + levelName + "\nSpawn Points: " + spawnPointsMap.toString());
        Actor actor;
        //Reruse Player if already created
        if (WorldView.getPlayer() != null)
        {
            actor = WorldView.getPlayer().getActor();
            WorldView.getPlayer().setPosition(playerSpawn.x * 64, playerSpawn.y * 64);
        }
        else
            actor = createActor("player", playerSpawn.x, playerSpawn.y);


        actor.setDirection(playerSpawn.direction);
        actorSprites.addAll(actor.spriteList);
        actorsList.add(actor);
        List<SpriteData> spriteDataList = actor.spriteDataMap.get(actor.compoundStatus);
        for (int j = 0; j < spriteDataList.size(); j++)
        {
            //System.out.println(CLASSNAME + methodName + actor.spriteList.get(j) +" layer: "+ spriteDataList.get(j).heightLayer + " size " + spriteDataList.size());
            addToCollisionLayer(actor.spriteList.get(j), spriteDataList.get(j).renderLayer);
        }

        player = actor.spriteList.get(0);
    }

    public Rectangle2D getBorders()
    {
        return borders;
    }

    public List<Sprite> getMediumLayer()
    {
        return mediumLayer;
    }

    public List<Sprite> getBttmLayer()
    {
        return bttmLayer;
    }

    public List<Sprite> getUpperLayer()
    {
        return upperLayer;
    }

    public Sprite getPlayer()
    {
        return player;
    }

    public List<Sprite> getPassivLayer()
    {
        return passivLayer;
    }

    public Color getShadowColor()
    {
        return shadowColor;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public String getSpawnId()
    {
        return spawnId;
    }

    public List<Sprite> getActorSprites()
    {
        return actorSprites;
    }

    public Set<String> getLoadedTileIdsSet()
    {
        return loadedTileIdsSet;
    }

    public Map<String, SpriteData> getTileDataMap()
    {
        return tileDataMap;
    }

    public Map<String, ActorData> getActorDataMap()
    {
        return actorDataMap;
    }

    public Map<String, SpawnData> getSpawnPointsMap()
    {
        return spawnPointsMap;
    }

    public ActorMonitor getStageMonitor()
    {
        return actorMonitor;
    }

    public Map<String, ActorGroupData> getActorGroupDataMap()
    {
        return actorGroupDataMap;
    }

    public String getReadMode()
    {
        return readMode;
    }

    public int getMaxVerticalTile()
    {
        return maxVerticalTile;
    }

    public int getCurrentVerticalTile()
    {
        return currentVerticalTile;
    }

    public int getCurrentHorizontalTile()
    {
        return currentHorizontalTile;
    }

    public int getMaxHorizontalTile()
    {
        return maxHorizontalTile;
    }

    public ClockMode getClockMode()
    {
        return clockMode;
    }

    public List<Sprite> getTopLayer()
    {
        return topLayer;
    }

    static class ActorGroupData
    {
        ArrayList memberOfGroups = new ArrayList();
    }

    public class SpawnData
    {
        Integer x, y;
        Direction direction;

        public SpawnData(Integer x, Integer y, Direction direction)
        {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }

        @Override
        public String toString()
        {
            return "x=" + x +
                    ", y=" + y +
                    ", direction=" + direction +
                    '}';
        }

        public Integer getX()
        {
            return x;
        }

        public Integer getY()
        {
            return y;
        }

        public Direction getDirection()
        {
            return direction;
        }


    }

    class ActorData
    {
        String actorFileName;
        String actorInGameName;
        String generalStatus;
        String sensor_status;
        Direction direction;

        public ActorData(String actorname, String actorInGameName, String generalStatus, String sensor_status, Direction direction)
        {
            this.actorFileName = actorname;
            this.actorInGameName = actorInGameName;
            this.sensor_status = sensor_status;
            this.generalStatus = generalStatus;
            this.direction = direction;
        }

        @Override
        public String toString()
        {
            return "ActorData{" +
                    "actorname='" + actorFileName + '\'' +
                    '}';
        }
    }

    public List<Actor> getActorsList()
    {
        return actorsList;
    }

    //public void addActiveActorChecked(Actor actor)
    //{
    //    if(actor.sensorStatus.onInRange_TriggerSensor != NOTHING
    //    || actor.sensorStatus.onInRange_TriggerSprite != NOTHING
    //    || actor.sensorStatus.onIntersection_TriggerSensor != NOTHING
    //    || actor.sensorStatus.onIntersection_TriggerSprite != NOTHING
    //    || actor.sensorStatus.onUpdate_TriggerSensor != NOTHING
    //    || actor.sensorStatus.onUpdate_TriggerSprite != NOTHING
    //    || actor.getSpriteList().get(0).getName().equalsIgnoreCase("player")
    //    )
    //    activeActors.add(actor);
    //    else
    //        System.out.println(CLASSNAME + actor.getActorInGameName());
    //}


}
