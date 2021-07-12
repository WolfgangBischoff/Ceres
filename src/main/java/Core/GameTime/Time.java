package Core.GameTime;

import java.text.DecimalFormat;
import java.util.Objects;

public class Time implements Comparable
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

    public Time add(int hours, int minutes)
    {
        return new Time(this.hours + hours, this.minutes + minutes);
    }

    public static boolean isBetween(Time minRange, Time maxRange, Time now)
    {
        TimeComparator comparator = new TimeComparator();
        if (comparator.compare(minRange, maxRange) > 1)
            throw new RuntimeException("Min is less than max");

        return (comparator.compare(minRange, now) == 0 || comparator.compare(minRange, now) < 0)
                && (comparator.compare(maxRange, now) == 0 || comparator.compare(maxRange, now) > 0);//within interval
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
    public int compareTo(Object o)
    {
        if (o instanceof Time)
        {
            Time other = (Time) o;
            if (hours.compareTo(other.hours) != 0)
                return hours.compareTo(other.hours);
            else
                return minutes.compareTo(other.minutes);
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