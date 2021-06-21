package Core;

public class GrowData
{
    String statusGrown;
    int minutesTillGrown;
    int minutesTillRotten;

    public GrowData(String statusGrown, int minutesTillGrown, int minutesTillRotten)
    {
        this.statusGrown = statusGrown;
        this.minutesTillGrown = minutesTillGrown;
        this.minutesTillRotten = minutesTillRotten;
    }
}
