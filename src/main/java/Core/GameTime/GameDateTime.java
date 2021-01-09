package Core.GameTime;

import java.text.DecimalFormat;

public class GameDateTime
{
    long days;
    long minutes;
    long fiveMinutes;
    long hour;

    public GameDateTime(long days, long hour, long minutes)
    {
        this.days = days;
        this.hour = hour;
        this.minutes = minutes;
        fiveMinutes = minutes / 5;
    }

    @Override
    public String toString()
    {
        DecimalFormat formatter = new DecimalFormat("00");
        String hourFormatted = formatter.format(hour);
        String minutesFormatted = formatter.format(fiveMinutes * 5);
        return "Day " + days + " - " + hourFormatted + ":" + minutesFormatted;
    }

    public String dayTime()
    {
        DecimalFormat formatter = new DecimalFormat("00");
        String hourFormatted = formatter.format(hour);
        String minutesFormatted = formatter.format(fiveMinutes * 5);
        return hourFormatted + ":" + minutesFormatted;
    }

    public long getDays()
    {
        return days;
    }

    public long getMinutes()
    {
        return minutes;
    }

    public long getFiveMinutes()
    {
        return fiveMinutes;
    }

    public long getHour()
    {
        return hour;
    }
}