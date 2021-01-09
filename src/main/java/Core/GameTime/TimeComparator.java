package Core.GameTime;

import java.util.Comparator;

public class TimeComparator implements Comparator<Time>
{
    @Override
    public int compare(Time first, Time second)
    {
        if (first.hours.compareTo(second.hours) != 0)
            return first.hours.compareTo(second.hours);
        else
            return first.minutes.compareTo(second.minutes);
    }
}