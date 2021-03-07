package Core.Enums;

public enum CollectableType
{
    MONEY, KEY, QUEST, FOOD, NONFUNCTION, BACTERIA_BASE;

    public static CollectableType getType(String type)
    {
        switch (type.toUpperCase())
        {
            case "MONEY": return MONEY;
            case "KEY": return KEY;
            case "QUEST": return QUEST;
            case "FOOD": return FOOD;
            case "BACTERIA_BASE": return BACTERIA_BASE;
            case "NONFUNCTION": return NONFUNCTION;
            default: throw new RuntimeException("ItemType unknown: " + type);
        }
    }
}
