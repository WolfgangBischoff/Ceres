package Core.GameTime;

import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import static Core.Configs.Config.*;


public class Clock
{
    private static String CLASSNAME = "Clock/";
    private static final int MINUTES_PER_DAY = 60 * 24;
    LongProperty timeTicks = new SimpleLongProperty(DAY_WAKE_UP_TIME.ticks());
    Long lastTimeIncremented;

    public Clock(Long initRealTime)
    {
        this.lastTimeIncremented = initRealTime;
    }

    public void tryIncrementTime(Long currentNanoTime)
    {
        String methodName = "tryIncrementTime() ";
        double elapsedTimeSinceLastIncrement = (currentNanoTime - lastTimeIncremented) / 1000000000.0;
        if (elapsedTimeSinceLastIncrement > LENGTH_GAME_MINUTE_SECONDS && WorldViewController.getWorldViewStatus() == WorldViewStatus.WORLD) {
            timeTicks.set(timeTicks.getValue() + 1);
            lastTimeIncremented = currentNanoTime;
        }
    }

    public void skipToNextDay()
    {
        long pastTicksCurrentDay = timeTicks.getValue() % MINUTES_PER_DAY;
        long ticksToNextDayStartTime = MINUTES_PER_DAY - pastTicksCurrentDay + DAY_WAKE_UP_TIME.ticks();
        timeTicks.setValue(timeTicks.getValue() + ticksToNextDayStartTime);
    }

    public DateTime getCurrentGameTime()
    {
        long ticks = timeTicks.getValue();
        long days = ticks / MINUTES_PER_DAY;
        ticks = ticks - (days * MINUTES_PER_DAY);
        long hours = ticks / 60;
        ticks -= hours * 60;
        long minutes = ticks;
        return new DateTime(days, new Time(hours, minutes));
    }

    public long getTimeTicks()
    {
        return timeTicks.get();
    }

    public LongProperty timeTicksProperty()
    {
        return timeTicks;
    }
}