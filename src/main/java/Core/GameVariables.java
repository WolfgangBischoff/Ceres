package Core;

import Core.Configs.GenericVariablesManager;
import Core.Enums.ActorTag;
import Core.Enums.Knowledge;
import Core.GameTime.Clock;
import Core.GameTime.DateTime;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Menus.Email.EmailManager;
import Core.Sprite.Sprite;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.stream.Collectors;

import static Core.ActorLogic.GrowspaceManager.updateGrowplaces;
import static Core.Configs.Config.*;
import static Core.WorldView.WorldViewStatus.WORLD;

public class GameVariables
{
    private static final String CLASSNAME = "GameVariables/";
    static IntegerProperty playerMaM_duringDay = new SimpleIntegerProperty();
    static IntegerProperty playerHunger = new SimpleIntegerProperty(INIT_HUNGER);
    static Long lastTimeHungerFromTime;
    static Integer health = INIT_HEALTH;
    //Game State persistent over days
    static Sprite player;
    private static GameVariables singleton;
    static private IntegerProperty playerMoney = new SimpleIntegerProperty(INIT_MONEY);
    static private Set<Knowledge> playerKnowledge = new HashSet<>();
    static private int playerMaM_dayStart = 0;//ManagementAttentionMeter
    static private Clock clock;
    static private EmailManager emailManager;
    static private DateTime lastTimeTickUpdated;
    static private GenericVariablesManager booleanWorldVariables = new GenericVariablesManager();
    //Game State persistent on same day
    private static Map<String, LevelState> levelData = new HashMap<>();
    private static List<Collectible> stolenCollectibles = new ArrayList<>();

    public static void init()
    {
        clock = new Clock(GameWindow.getCurrentNanoRenderTimeGameWindow());
        emailManager = EmailManager.getInstance();
        lastTimeHungerFromTime = clock.getTotalTimeTicks();
    }

    public static LevelState getLevelData(String levelName)
    {
        if(!levelDataExists(levelName))
            levelData.put(levelName, LevelState.empty(levelName));
        return levelData.get(levelName);
    }

    public static boolean levelDataExists(String levelName)
    {
        return levelData.containsKey(levelName);

    }

    public static void saveLevelState(LevelState levelState)
    {
        savePlayer();
        levelData.put(levelState.levelName, levelState);
    }

    public static void saveLevelState(String levelName, List<Actor> persistentActors)
    {
        savePlayer();
        LevelState state = getLevelData(levelName);
        state.setActorList(persistentActors);
    }

    private static void savePlayer()
    {
        WorldView.getactorSpritesLayer().remove(player);
        WorldView.getmiddleLayer().remove(player); //Player Layer
        GameVariables.setPlayer(player);
    }

    public static void addPlayerMAM_duringDay(int deltaMAM)
    {
        setPlayerMaM_duringDay(playerMaM_duringDay.getValue() + deltaMAM);
    }

    public static void addStolenCollectible(Collectible collectible)
    {
        stolenCollectibles.add(collectible);
    }

    public static void incrementDay()
    {
        playerMaM_dayStart = playerMaM_duringDay.getValue();
        clock.skipToNextDay();
    }

    public static void updateTimeDependent_5Minutes(Long currentNanoTime, List<Actor> actorList)
    {
        if (WorldViewController.getWorldViewStatus() == WORLD)
        {
            //Hunger
            int intervalsForHunger = 9;// 12hours = 43 200 ticks
            if (lastTimeHungerFromTime + intervalsForHunger < clock.getTotalTimeTicks())
            {
                addHunger(-1);
                lastTimeHungerFromTime = clock.getTotalTimeTicks();
            }

            //Other Actors
            applyTimeToActorsGameTimeDepentend(currentNanoTime, actorList);
        }
    }

    public static void updateActorsByVariableCheck(Long currentNanoTime, List<Actor> actorList)
    {
        if (WorldViewController.getWorldViewStatus() == WORLD)
        {
            for (Actor a :actorList
                 )
            {
                System.out.println(a.getActorInGameName() + " updated One Minute");
            }
            //Check if Script fits
            //applyTimeToActorsGameTimeDepentend(currentNanoTime, actorList);
        }
    }

    private static void applyTimeToActorsGameTimeDepentend(Long currentNanoTime, List<Actor> timeDependentActors)
    {
        DateTime current = clock.getCurrentGameTime();
        updateGrowplaces(timeDependentActors.stream().filter(a -> a.hasTag(ActorTag.GROWPLACE)).collect(Collectors.toList()));
    }

    public static void addHunger(int delta)
    {
        int newValue = playerHunger.getValue() + delta;
        if (newValue > MAX_HUNGER)
            newValue = MAX_HUNGER;
        else if (newValue < 0)
            newValue = 0;
        playerHunger.setValue(newValue);
    }

    public static DateTime gameDateTime()
    {
        return clock.getCurrentGameTime();
    }

    public static int getPlayerMaM_dayStart()
    {
        return playerMaM_dayStart;
    }

    public static int getPlayerMaM_duringDay()
    {
        return playerMaM_duringDay.getValue();
    }

    public static void setPlayerMaM_duringDay(int playerMaM_duringDay)
    {
        String methodName = "setPlayerMaM_duringDay() ";
        GameVariables.playerMaM_duringDay.setValue(playerMaM_duringDay);
    }

    public static IntegerProperty getPlayerMaM_duringDayProperty()
    {
        return playerMaM_duringDay;
    }

    public static List<Collectible> getStolenCollectibles()
    {
        return stolenCollectibles;
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static GameVariables getSingleton()
    {
        return singleton;
    }

    public static Sprite getPlayer()
    {
        return player;
    }

    public static void setPlayer(Sprite player)
    {
        GameVariables.player = player;
    }

    public static void addPlayerMoney(int delta)
    {
        String methodName = "addPlayerMoney() ";
        playerMoney.setValue(playerMoney.getValue() + delta);
        //System.out.println(CLASSNAME + methodName + "New balance: " + playerMoney.getValue());
    }

    public static int getPlayerMoney()
    {
        return playerMoney.getValue();
    }

    public static IntegerProperty playerMoneyProperty()
    {
        return playerMoney;
    }

    public static Clock getClock()
    {
        return clock;
    }

    public static int getPlayerHunger()
    {
        return playerHunger.get();
    }

    public static IntegerProperty playerHungerProperty()
    {
        return playerHunger;
    }

    public static Integer getHealth()
    {
        return health;
    }

    public static void setHealth(Integer health)
    {
        if (health <= MAX_HEALTH)
            GameVariables.health = health;
    }

    public static GenericVariablesManager getGenericVariableManager()
    {
        return booleanWorldVariables;
    }

    public static void setGenericVariable(String varName, String newValue)
    {
        booleanWorldVariables.setValue(varName, newValue);
        System.out.println(CLASSNAME + "set " + varName + " to " + newValue);
    }

    public static Map<String, String> getGenericVariables()
    {
        return GenericVariablesManager.getStringWorldVariables();
    }

    public static Set<Knowledge> getPlayerKnowledge()
    {
        return playerKnowledge;
    }

    public static void addPlayerKnowledge(Knowledge knowledge)
    {
        if (!playerKnowledge.contains(knowledge))
            CentralMessageOverlay.showMsg("Learned " + knowledge.getName());
        playerKnowledge.add(knowledge);
    }

}
