package Core.GameTime;

public enum DayPart
{
    DAY, NIGHT;

    public static DayPart of(String value)
    {
        switch (value.toUpperCase())
        {
            case "DAY": return DAY;
            case "NIGHT": return NIGHT;
            default:throw new RuntimeException("DayPart unknown: " + value);
        }
    }
}
