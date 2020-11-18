package Core.Enums;

public enum ActorConditionType
{
    HAS_ITEM, VARIABLE, HAS_STATUS, HAS_TAG;

    public static ActorConditionType getConditionFromValue(String value)
    {
        switch (value.toLowerCase())
        {
            case "hasitem": return HAS_ITEM;
            case "variable": return VARIABLE;
            case "hasstatus": return HAS_STATUS;
            default: throw new RuntimeException("ActorConditionType not defined: " + value);
        }
    }


}
