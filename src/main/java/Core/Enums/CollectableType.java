package Core.Enums;

public enum CollectableType
{
    MONEY, KEY, QUEST, FOOD, NONFUNCTION, BACTERIA_NUTRITION, BACTERIA_SPORE,BACTERIA_CULTURE, METAL;

    public static CollectableType getType(String type)
    {
        switch (type.toUpperCase())
        {
            case "MONEY": return MONEY;
            case "KEY": return KEY;
            case "QUEST": return QUEST;
            case "FOOD": return FOOD;
            case "METAL": return METAL;
            case "BACTERIA_NUTRITION": return BACTERIA_NUTRITION;
            case "BACTERIA_SPORE": return BACTERIA_SPORE;
            case "BACTERIA_CULTURE": return BACTERIA_CULTURE;
            case "NONFUNCTION": return NONFUNCTION;
            default: throw new RuntimeException("ItemType unknown: " + type);
        }
    }
}
