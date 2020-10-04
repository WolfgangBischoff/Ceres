package Core.ActorSystem;

public enum SystemStatus
{
    ON, OFF, LOCKED, UNLOCKED, TRANSITION;

    public static SystemStatus getOff(String value)
    {
        switch (value.toLowerCase())
        {
            case "on": return ON;
            case "off": return OFF;
            case "transition": return TRANSITION;
            case "locked": return LOCKED;
            case "unlocked": return UNLOCKED;
            default:throw new RuntimeException("SystemStatus unknown: " + value);
        }
    }
}
