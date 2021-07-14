package Core.GameTime;

import java.text.DecimalFormat;
import java.util.Objects;

import static Core.Utilities.tryParseInt;

public class Time implements Comparable<Time>
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

    public Time(long ticks)
    {
        this.hours = ticks / 60;
        this.minutes = ticks % 60;
        fiveMinutes = ticks % 60 / 5;
    }

    public static Time of(String time)
    {
        var s = time.split(":");
        if(s.length < 2)
            return null;
        if (tryParseInt((s[0])) && tryParseInt((s[1])))
            return new Time(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        else
            return null;
    }

    public static boolean isBetween(Time minRange, Time maxRange, Time now)
    {
        TimeComparator comparator = new TimeComparator();
        if (comparator.compare(minRange, maxRange) > 1)
            throw new RuntimeException("Min is less than max");

        return (comparator.compare(minRange, now) == 0 || comparator.compare(minRange, now) < 0)
                && (comparator.compare(maxRange, now) == 0 || comparator.compare(maxRange, now) > 0);//within interval
    }

    public Time add(int hours, int minutes)
    {
        return new Time(this.hours + hours, this.minutes + minutes);
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

    @Override
    public int compareTo(Time o)
    {
        if (o != null)
        {
            if (hours.compareTo(((Time) o).hours) != 0)
                return hours.compareTo(((Time) o).hours);
            else
                return minutes.compareTo(((Time) o).minutes);
        }
        return 0;

    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Time)) return false;
        Time time = (Time) o;
        return getMinutes().equals(time.getMinutes()) &&
                Objects.equals(getFiveMinutes(), time.getFiveMinutes()) &&
                getHours().equals(time.getHours());
    }

}