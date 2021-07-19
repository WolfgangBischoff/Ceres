package Core.GameTime;

public class TimeSpan
{
    Time begin;
    Time end;

    public TimeSpan(Time begin, Time end)
    {
        this.begin = begin;
        this.end = end;
    }

    public static boolean within(TimeSpan timeSpan, Time now)
    {
        TimeComparator comparator = new TimeComparator();
        if (comparator.compare(timeSpan.begin, timeSpan.end) > 1)
            throw new RuntimeException("Min is less than max");

        return (comparator.compare(timeSpan.begin, now) == 0 || comparator.compare(timeSpan.begin, now) < 0)
                && (comparator.compare(timeSpan.end, now) == 0 || comparator.compare(timeSpan.end, now) > 0);
    }

    public static boolean notWithin(TimeSpan timeSpan, Time now)
    {
        return !within(timeSpan, now);
    }

    public Time getBegin()
    {
        return begin;
    }

    public Time getEnd()
    {
        return end;
    }

    @Override
    public String toString()
    {
        return "(" + begin + " - " + end + ")";
    }
}
