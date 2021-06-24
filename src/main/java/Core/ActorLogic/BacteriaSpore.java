package Core.ActorLogic;

public enum BacteriaSpore
{

    FUEL_SPORE, METAL_SPORE;

    public static BacteriaSpore fromString(String type)
    {
        switch (type.toUpperCase())
        {
            case "FUEL_SPORE":
                return FUEL_SPORE;
            case "METAL_SPORE":
                return METAL_SPORE;
            default:
                return null;
        }
    }

}
