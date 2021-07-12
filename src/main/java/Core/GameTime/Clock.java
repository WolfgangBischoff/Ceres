package Core.GameTime;

import Core.GameVariables;
import Core.GameWindow;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.paint.Color;

import java.util.Date;

import static Core.Configs.Config.*;
import static Core.GameTime.DayPart.DAY;
import static Core.GameTime.DayPart.NIGHT;
import static Core.WorldView.WorldView.setShadowColor;


public class Clock
{
    private static final int TICKS_PER_MINUTE = 1;
    private static final int TICKS_PER_HOUR = 60 * TICKS_PER_MINUTE;
    private static final int TICKS_PER_DAY = TICKS_PER_HOUR * 24;
    private static String CLASSNAME = "Clock/";
    LongProperty totalTimeTicks = new SimpleLongProperty(DAY_WAKE_UP_TIME.ticks());
    Long lastTimeIncremented;
    ClockMode clockMode = ClockMode.RUNNING;
    DayPart dayPart = DAY;

    public Clock(Long initRealTime)
    {
        this.lastTimeIncremented = initRealTime;
    }

    public void tryIncrementTime(Long currentNanoTime)
    {
        switch (clockMode)
        {
            case STOPPED:
                break;
            case RUNNING:
                double elapsedTimeSinceLastIncrement = (currentNanoTime - lastTimeIncremented) / 1000000000.0;
                if (elapsedTimeSinceLastIncrement > LENGTH_GAME_MINUTE_SECONDS && WorldViewController.getWorldViewStatus() == WorldViewStatus.WORLD)
                {
                    incrementTime(currentNanoTime);
                    updateWorld(currentNanoTime);
                }
        }
    }

    private void incrementTime(Long currentNanoTime)
    {
        totalTimeTicks.set(totalTimeTicks.getValue() + 1);
        lastTimeIncremented = currentNanoTime;
    }

    public void updateWorld(Long currentNanoTime)
    {
        DateTime date = GameVariables.gameDateTime();
        if (Time.isBetween(DAY_LIGHT_ON_TIME, DAY_LIGHT_OFF_TIME, date.getTime()) && dayPart == NIGHT)
        {
            dayPart = DAY;
            String reloadDay = WorldView.getLevelName();
            //WorldView.getSingleton().changeStage(reloadDay, "default", false);
            System.out.println("Day begins");
        }
        else if (!Time.isBetween(DAY_LIGHT_ON_TIME, DAY_LIGHT_OFF_TIME, date.getTime()) && dayPart == DAY)
        {
            dayPart = NIGHT;
            String reloadDay = WorldView.getLevelName();
            //WorldView.getSingleton().changeStage(reloadDay, "default", false);
            System.out.println("Night begins");
        }
        setShadowColor(getShadowColorFromTime(date));

        //Jede 5 Minute ausführen
        if (totalTimeTicks.get() % 5 == 0)
            GameVariables.updateFromTimeGameTimeDependent(currentNanoTime, WorldView.getActorList());
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
        //updateWorld(GameWindow.getCurrentNanoRenderTimeGameWindow());
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
        //System.out.println(CLASSNAME + "setTimeMode: " + timeMode.name());
    }
}
