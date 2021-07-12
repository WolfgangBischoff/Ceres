package Core.GameTime;

public enum ClockMode
{
    RUNNING, STOPPED;

    public static ClockMode of(String value)
    {
        switch (value.toUpperCase())
        {
            case "RUNNING": return RUNNING;
            case "STOPPED": return STOPPED;
            default:throw new RuntimeException("Clock Mode unknown: " + value);
        }
    }
}
