package Core.GameTime;

import java.text.DecimalFormat;

public class DateTime implements Comparable
{
    Long days;
    Time dayTime;

    public DateTime(long days, Time dayTime)
    {
        this.days = days;
        this.dayTime = dayTime;
    }

    public static boolean isWithin(DateTime minRange, DateTime maxRange, DateTime now)
    {
        DateTimeComparator comparator = new DateTimeComparator();
        if (comparator.compare(minRange, maxRange) > 1)
            throw new RuntimeException("Min is less than max");

        if (comparator.compare(minRange, now) > 0 && comparator.compare(maxRange, now) < 0)
        {
            return true;//within interval
        }
        else
            return false;
    }

    @Override
    public String toString()
    {
        DecimalFormat formatter = new DecimalFormat("00");
        return "Day " + days + "-" + dayTime;
    }

    public String dayTime()
    {
        return dayTime.toString();
    }

    public Long getDays()
    {
        return days;
    }

    public Time getTime()
    {
        return dayTime;
    }

    public DateTime add(int days, int hours)
    {
        return new DateTime(this.days + days, this.dayTime.add(hours));
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof DateTime)
        {
            DateTime other = (DateTime) o;
            if (days.compareTo(other.days) != 0)
                return days.compareTo(other.days);
            else
                return dayTime.compareTo(other.dayTime);
        }
        return 0;
    }
}