package Core.GameTime;

import java.util.Comparator;

public class DateTimeComparator implements Comparator<DateTime>
{
    @Override
    public int compare(DateTime first, DateTime second)
    {
        if (first.days.compareTo(second.days) != 0)
            return first.days.compareTo(second.days);
        else
            return new TimeComparator().compare(first.dayTime, second.dayTime);
    }
}
