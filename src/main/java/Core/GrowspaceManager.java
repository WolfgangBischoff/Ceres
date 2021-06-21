package Core;

import Core.Enums.CollectableType;

public class GrowspaceManager
{
    static boolean isBacteriaFood(Collectible collectible)
    {
        return collectible.getType() == CollectableType.FOOD;
    }

    static boolean isBacteriaCulture(Collectible collectible)
    {
        return collectible.getType() == CollectableType.BACTERIA_BASE;
    }

    static String getFoodStatus(Collectible collectible)
    {
        switch (collectible.getType())
        {
            case FOOD:
                return "fuel_seed";
            default:
                return "none";
        }
    }
}
