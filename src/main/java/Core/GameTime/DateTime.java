package Core.GameTime;

import java.text.DecimalFormat;

public class DateTime
{
    Long days;
    Time dayTime;

    public DateTime(long days, Time dayTime)
    {
        this.days = days;
        this.dayTime = dayTime;
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

    public static boolean isWithin(DateTime minRange, DateTime maxRange, DateTime now)
    {
        DateTimeComparator comparator = new DateTimeComparator();
        if (comparator.compare(minRange, maxRange) > 1)
            throw new RuntimeException("Min is less than max");

        if (comparator.compare(minRange, now) > 0 && comparator.compare(maxRange, now) < 0) {
            return true;//within interval
        }
        else
            return false;
    }


    public Long getDays()
    {
        return days;
    }

    public Time getTime()
    {
        return dayTime;
    }
}