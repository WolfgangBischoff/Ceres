package Core;

import Core.Configs.GenericVariablesManager;
import Core.Enums.Knowledge;
import Core.GameTime.Clock;
import Core.GameTime.DateTime;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Sprite.Sprite;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;

import static Core.Configs.Config.*;

public class GameVariables
{
    private static final String CLASSNAME = "GameVariables/";
    private static GameVariables singleton;
    static private IntegerProperty playerMoney = new SimpleIntegerProperty(INIT_MONEY);
    static IntegerProperty playerMaM_duringDay = new SimpleIntegerProperty();
    static IntegerProperty playerHunger = new SimpleIntegerProperty(INIT_HUNGER);
    static private Set<Knowledge> playerKnowledge = new HashSet<>();
    static Long lastTimeHungerFromTime;
    static Integer health = INIT_HEALTH;
    static private int playerMaM_dayStart = 0;//ManagementAttentionMeter
    static private Clock clock;
    static private GenericVariablesManager booleanWorldVariables = new GenericVariablesManager();

    //Game State persistent over days
    static Sprite player;

    //Game State persistent on same day
    private static Map<String, LevelState> levelData = new HashMap<>();
    private static List<Collectible> stolenCollectibles = new ArrayList<>();

    public static void init()
    {
        clock = new Clock(GameWindow.getCurrentNanoRenderTimeGameWindow());
        lastTimeHungerFromTime = clock.getTotalTimeTicks();
    }

    public static void setPlayer(Sprite player)
    {
        GameVariables.player = player;
    }

    public static void saveLevelState(LevelState levelState)
    {
        String methodName = "saveLevelState() ";
        boolean debug = false;
        levelData.put(levelState.levelName, levelState);

        if (debug)
            System.out.println(CLASSNAME + methodName + "Saved: " + levelState);
    }


    public static void addPlayerMAM_duringDay(int deltaMAM)
    {
        String methodName = "addPlayerManagementAttention(int) ";
        boolean debug = true;
        if (debug)
            System.out.println(CLASSNAME + methodName + "MAM: " + playerMaM_duringDay + " + " + deltaMAM + " = " + (playerMaM_duringDay.getValue() + deltaMAM));

        setPlayerMaM_duringDay(playerMaM_duringDay.getValue() + deltaMAM);
    }

    public static void addStolenCollectible(Collectible collectible)
    {
        stolenCollectibles.add(collectible);
    }

    public static void incrementDay()
    {
        String methodName = "incrementDay() ";
        playerMaM_dayStart = playerMaM_duringDay.getValue();
        clock.skipToNextDay();
        //System.out.println(CLASSNAME + methodName + clock.getCurrentGameTime() + " MaM-Start: " + playerMaM_dayStart);
    }

    public static void updateHunger(Long currentNanoTime)
    {
        String methodName = "updateHunger() ";
        int intervalsForHunger = 9;// 12hours = 43 200 ticks
        if (lastTimeHungerFromTime + intervalsForHunger < clock.getTotalTimeTicks()) {
            addHunger(-1);
            lastTimeHungerFromTime = clock.getTotalTimeTicks();
            //System.out.println(CLASSNAME + methodName + playerHunger.getValue() + " " + clock.getFormattedTime());
        }
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

    public static IntegerProperty getPlayerMaM_duringDayProperty()
    {
        return playerMaM_duringDay;
    }

    public static Map<String, LevelState> getLevelData()
    {
        return levelData;
    }

    public static List<Collectible> getStolenCollectibles()
    {
        return stolenCollectibles;
    }

    public static void setPlayerMaM_duringDay(int playerMaM_duringDay)
    {
        String methodName = "setPlayerMaM_duringDay() ";
        GameVariables.playerMaM_duringDay.setValue(playerMaM_duringDay);
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
