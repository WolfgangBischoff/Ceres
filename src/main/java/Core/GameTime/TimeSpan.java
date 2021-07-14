package Core.GameTime;

public class TimeSpan
{
    Time earlier;
    Time later;

    public TimeSpan(Time earlier, Time later)
    {
        this.earlier = earlier;
        this.later = later;
    }

    public static boolean within(TimeSpan timeSpan, Time now)
    {
        TimeComparator comparator = new TimeComparator();
        if (comparator.compare(timeSpan.earlier, timeSpan.later) > 1)
            throw new RuntimeException("Min is less than max");

        return (comparator.compare(timeSpan.earlier, now) == 0 || comparator.compare(timeSpan.earlier, now) < 0)
                && (comparator.compare(timeSpan.later, now) == 0 || comparator.compare(timeSpan.later, now) > 0);
    }

    public static boolean notWithin(TimeSpan timeSpan, Time now)
    {
        return !within(timeSpan, now);
    }

    @Override
    public String toString()
    {
        return "(" + earlier + " - " + later + ")";
    }
}
