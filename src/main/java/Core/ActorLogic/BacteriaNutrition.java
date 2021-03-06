package Core.ActorLogic;

public enum BacteriaNutrition
{

    NUTRITION_ORGANIC, NUTRITION_METAL;

    public static BacteriaNutrition fromString(String type)
    {
        switch (type.toUpperCase())
        {
            case "NUTRITION_ORGANIC":
                return NUTRITION_ORGANIC;
            case "NUTRITION_METAL":
                return NUTRITION_METAL;
            default:
                return null;
        }
    }

    public static boolean isBacteriaNutrition(String s)
    {
        return fromString(s) != null;
    }

}
