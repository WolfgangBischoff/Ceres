package Core.GameTime;

import Core.Enums.ActorTag;
import Core.GameVariables;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Menus.Email.EmailManager;
import Core.WorldView.MapTimeData;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.paint.Color;

import java.util.stream.Collectors;

import static Core.Configs.Config.*;
import static Core.GameTime.DayPart.DAY;
import static Core.GameTime.DayPart.NIGHT;
import static Core.WorldView.WorldView.getPlayer;
import static Core.WorldView.WorldView.setShadowColor;


public class Clock
{
    private static final int TICKS_PER_MINUTE = 1;
    private static final int TICKS_PER_HOUR = 60 * TICKS_PER_MINUTE;
    private static final int TICKS_PER_DAY = TICKS_PER_HOUR * 24;
    private static String CLASSNAME = "Clock/";
    LongProperty totalTimeTicks = new SimpleLongProperty(DAY_WAKE_UP_TIME.ticks());
    Long lastTimeIncremented;
    Long lastTimeActorUpdatedFromVariableCheck = 0L;
    ClockMode clockMode = ClockMode.RUNNING;
    DayPart dayPart = DAY;

    public Clock(Long initRealTime)
    {
        this.lastTimeIncremented = initRealTime;
    }

    public void tryIncrementTime(Long currentNanoTime)
    {
        switch (WorldView.getMapTimeData().getClockMode())
        {
            case STOPPED:
                break;
            case RUNNING:
                double elapsedTimeSinceLastIncrement = (currentNanoTime - lastTimeIncremented) / 1000000000.0;
                if (elapsedTimeSinceLastIncrement > LENGTH_GAME_MINUTE_SECONDS && WorldViewController.getWorldViewStatus() == WorldViewStatus.WORLD)
                {
                    incrementTime(currentNanoTime);
                    updateWorldFromTime(currentNanoTime);
                }
                break;
        }
        long second = 1000000000;
        //Always
        if (currentNanoTime > lastTimeActorUpdatedFromVariableCheck + second)
        {
            lastTimeActorUpdatedFromVariableCheck = currentNanoTime;//jede game minute
            GameVariables.updateActorsByVariableCheck(currentNanoTime, WorldView.getActorList().stream().filter(a -> a.hasTag(ActorTag.APPLY_VARIABLE_CHECK)).collect(Collectors.toList()));
        }
    }

    private void incrementTime(Long currentNanoTime)
    {
        totalTimeTicks.set(totalTimeTicks.getValue() + 1);
        lastTimeIncremented = currentNanoTime;
    }

    public void updateWorldFromTime(Long currentNanoTime)
    {
        DateTime date = GameVariables.gameDateTime();
        MapTimeData mapTimeData = WorldView.getMapTimeData() == null ? MapTimeData.getDefault() : WorldView.getMapTimeData();

        //Automatisches Entfernen des Spielers
        if (mapTimeData.getOpenedTime() != null)
            checkAreaOpenedTime(date, mapTimeData);

        //Umschalten zwischen Tag und NachtMap
        checkDayNightCycle(date, mapTimeData);

        //Jede 5 Minute ausführen
        if (totalTimeTicks.get() % 5 == 0)
        {
            GameVariables.updateTimeDependent_5Minutes(currentNanoTime, WorldView.getActorList().stream().filter(a -> a.hasTag(ActorTag.APPLY_TIME_5_MINUTES)).collect(Collectors.toList()));
            EmailManager.checkSendMails();
        }
    }

    private void checkDayNightCycle(DateTime date, MapTimeData mapTimeData)
    {
        if (Time.isBetween(mapTimeData.getLightOnTime(), mapTimeData.getLightOffTime(), date.getTime()) && dayPart == NIGHT)
        {
            dayPart = DAY;
            WorldView.getSingleton().changeStage(WorldView.getLevelName(), getPlayer().getPosition());
        }
        else if (!Time.isBetween(mapTimeData.getLightOnTime(), mapTimeData.getLightOffTime(), date.getTime()) && dayPart == DAY)
        {
            dayPart = NIGHT;
            WorldView.getSingleton().changeStage(WorldView.getLevelName(), getPlayer().getPosition());
        }
        setShadowColor(getShadowColorFromTime(date));
    }

    private void checkAreaOpenedTime(DateTime date, MapTimeData mapTimeData)
    {
        if (mapTimeData.getOpenedTime().getEnd().equals(date.getTime().add(1, 0)))
            CentralMessageOverlay.showMsg("The Area will close soon!");
        if (TimeSpan.notWithin(mapTimeData.getOpenedTime(), date.getTime()))
        {
            CentralMessageOverlay.showMsg("The Area is closed now, you had to leave!");
            WorldView.getSingleton().changeStage(mapTimeData.getLevelAfterClose(), mapTimeData.getSpawnPointAfterClose());
        }
    }

    private Color getShadowColorFromTime(DateTime time)
    {
        if (WorldView.getShadowColor() == COLOR_EMERGENCY_LIGHT)
        {
            return COLOR_EMERGENCY_LIGHT;
        }
        else if (dayPart == DAY)
        {
            return null;
        }
        else
            return COLOR_NIGHT_LIGHT;
    }

    public void addTime(int hours)
    {
        totalTimeTicks.setValue(totalTimeTicks.getValue() + hours * TICKS_PER_HOUR);
    }

    public void skipToNextDay()
    {
        long pastTicksCurrentDay = totalTimeTicks.getValue() % TICKS_PER_DAY;
        long ticksToNextDayStartTime = TICKS_PER_DAY - pastTicksCurrentDay + DAY_WAKE_UP_TIME.ticks();
        totalTimeTicks.setValue(totalTimeTicks.getValue() + ticksToNextDayStartTime);
    }

    public DateTime getCurrentGameTime()
    {
        long ticks = totalTimeTicks.getValue();
        long days = ticks / TICKS_PER_DAY;
        ticks = ticks - (days * TICKS_PER_DAY);
        long hours = ticks / 60;
        ticks -= hours * 60;
        long minutes = ticks;
        return new DateTime(days, new Time(hours, minutes));
    }

    public long getTotalTimeTicks()
    {
        return totalTimeTicks.get();
    }

    public LongProperty totalTimeTicksProperty()
    {
        return totalTimeTicks;
    }

    public ClockMode getClockMode()
    {
        return clockMode;
    }

    public void setClockMode(ClockMode clockMode)
    {
        this.clockMode = clockMode;
    }

    public DayPart getDayPart()
    {
        return dayPart;
    }
}
