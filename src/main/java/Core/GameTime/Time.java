package Core.GameTime;

import java.text.DecimalFormat;

public class Time
{
    Long minutes;
    Long fiveMinutes;
    Long hours;

    public Time(long hours, long minutes)
    {
        this.hours = hours;
        this.minutes = minutes;
        fiveMinutes = minutes / 5;
    }

    public Long ticks()
    {
        return hours * 60 + minutes;
    }

    @Override
    public String toString()
    {
        DecimalFormat formatter = new DecimalFormat("00");
        String hourFormatted = formatter.format(hours);
        String minutesFormatted = formatter.format(fiveMinutes * 5);
        return hourFormatted + ":" + minutesFormatted;
    }

    public static boolean isWithin(Time minRange, Time maxRange, Time now)
    {
        TimeComparator comparator = new TimeComparator();
        if (comparator.compare(minRange, maxRange) > 1)
            throw new RuntimeException("Min is less than max");

        if ((comparator.compare(minRange, now) == 0 || comparator.compare(minRange, now) < 0)
                && (comparator.compare(maxRange, now) == 0 || comparator.compare(maxRange, now) > 0)) {
            return true;//within interval
        }
        else
            return false;
    }

    public Long getMinutes()
    {
        return minutes;
    }

    public Long getFiveMinutes()
    {
        return fiveMinutes;
    }

    public Long getHours()
    {
        return hours;
    }
}