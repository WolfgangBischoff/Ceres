package Core.GameTime;

public enum TimeMode
{
    RUNNING, DAY, NIGHT;

    public static TimeMode of(String value)
    {
        switch (value.toUpperCase())
        {
            case "RUNNING": return RUNNING;
            case "DAY": return DAY;
            case "NIGHT": return NIGHT;
            default:throw new RuntimeException("Time Mode unknown: " + value);
        }
    }
}
