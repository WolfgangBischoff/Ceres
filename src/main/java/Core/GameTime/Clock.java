package Core.GameTime;

import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import static Core.Configs.Config.*;


public class Clock
{
    private static String CLASSNAME = "Clock/";
    private static final int TICKS_PER_MINUTE = 1;
    private static final int TICKS_PER_HOUR = 60 * TICKS_PER_MINUTE;
    private static final int TICKS_PER_DAY = TICKS_PER_HOUR * 24;
    LongProperty totalTimeTicks = new SimpleLongProperty(DAY_WAKE_UP_TIME.ticks());
    Long lastTimeIncremented;
    TimeMode timeMode = TimeMode.RUNNING;

    public Clock(Long initRealTime)
    {
        this.lastTimeIncremented = initRealTime;
    }

    public void tryIncrementTime(Long currentNanoTime)
    {
        String methodName = "tryIncrementTime() ";
        switch (timeMode)
        {
            case DAY:
            case NIGHT:
                break;
            case RUNNING:
                double elapsedTimeSinceLastIncrement = (currentNanoTime - lastTimeIncremented) / 1000000000.0;
                if (elapsedTimeSinceLastIncrement > LENGTH_GAME_MINUTE_SECONDS && WorldViewController.getWorldViewStatus() == WorldViewStatus.WORLD) {
                    totalTimeTicks.set(totalTimeTicks.getValue() + 1);
                    lastTimeIncremented = currentNanoTime;
                }
        }
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

    public TimeMode getTimeMode()
    {
        return timeMode;
    }

    public void setTimeMode(TimeMode timeMode)
    {
        this.timeMode = timeMode;
        //System.out.println(CLASSNAME + "setTimeMode: " + timeMode.name());
    }
}
