package Core.ActorLogic;

public enum BacteriaCulture
{

    FUEL_CULTURE, METAL_CULTURE;

    public static BacteriaCulture fromString(String type)
    {
        switch (type.toUpperCase())
        {
            case "FUEL_CULTURE":
                return FUEL_CULTURE;
            case "METAL_CULTURE":
                return METAL_CULTURE;
            default:
                return null;
        }
    }

}
