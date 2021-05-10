package Core.Enums;

public enum ActorTag
{
    AUTOMATED_DOOR, AUTOMATED_DOOR_DETECTABLE,
    BECOME_TRANSPARENT, DETECTS_PLAYER, PLAYER,
    PERSISTENT, //is loaded on the next day from LevelState
    TURNS_DIRECTION_ONINTERACTION,
    APPLY_TIME,
    GROWPLACE,
    NO_COLLISION;

    public static ActorTag getType(String type)
    {
        switch (type.toUpperCase())
        {
            case "AUTOMATED_DOOR_RELEVANT":
                return AUTOMATED_DOOR_DETECTABLE;
            case "AUTOMATED_DOOR":
                return AUTOMATED_DOOR;
            case "BECOME_TRANSPARENT":
                return BECOME_TRANSPARENT;
            case "DETECTS_PLAYER":
                return DETECTS_PLAYER;
            case "PLAYER":
                return PLAYER;
            case "PERSISTENT":
                return PERSISTENT;
            case "TURNS_DIRECTION_ONINTERACTION":
                return TURNS_DIRECTION_ONINTERACTION;
            case "NO_COLLISION":
                return NO_COLLISION;
            case "APPLY_TIME":
                return APPLY_TIME;
            case "GROWPLACE":
                return GROWPLACE;
            default:
                throw new RuntimeException("ActorType unknown: " + type);
        }
    }
}
