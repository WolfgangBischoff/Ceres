package Core;

import Core.Enums.CollectableType;
import Core.GameTime.DateTime;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.WorldView.WorldView;

import java.util.HashMap;
import java.util.Map;

public class GrowspaceManager
{
    private static Map<String, GrowData> seedData = new HashMap<>();
    private static Map<String, GrowData> grownData = new HashMap<>();
    static
    {
        GrowData fuel = new GrowData("bac_fuella_grown", 10, 10);
        seedData.put("bac_fuella_growing", fuel);
        grownData.put("bac_fuella_grown", fuel);
    }

    public static void grow(Actor growspace)
    {
        String BUILDTIME = "buildtime";
        String growplaceStatus = growspace.getGeneralStatus();
        DateTime currentTime = GameVariables.getClock().getCurrentGameTime();
        if(growplaceStatus.equals("empty"))
        {
            growspace.setGenericDateTimeAttribute(BUILDTIME, null);
        }
        else if(seedData.containsKey(growplaceStatus))
        {
            GrowData seed = seedData.get(growplaceStatus);
            if (growspace.getGenericDateTimeAttribute(BUILDTIME) == null)
            {
                growspace.setGenericDateTimeAttribute(BUILDTIME, currentTime);
                System.out.println("Set: " + growspace.getGenericDateTimeAttribute(BUILDTIME));
                System.out.println("Finished " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(seed.minutesTillGrown));
                System.out.println("rotten " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(seed.minutesTillGrown + seed.minutesTillGrown));
            }
            DateTime grownTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(seed.minutesTillGrown);
            if (currentTime.compareTo(grownTime) >= 0)
            {
                growspace.setSpriteStatus(seed.statusGrown);
                growspace.setSensorStatus("readyToHarvest");
            }

        }
        else if(grownData.containsKey(growplaceStatus))
        {
            GrowData grown = grownData.get(growplaceStatus);
            DateTime rottenTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(grown.minutesTillGrown + grown.minutesTillGrown);
            if (currentTime.compareTo(rottenTime) >= 0)
            {
                growspace.getInventory().flush();
                growspace.setSpriteStatus("rotten");
                growspace.setSensorStatus("resetToEmpty");
            }
        }

    }

    public static boolean harvest(Actor growspace)
    {
        Collectible collected = Collectible.createCollectible("actorData/collectibles/bacteria/bacteria_grown", growspace.getGeneralStatus());
        if (WorldView.getPlayer().getActor().getInventory().addCollectibleStackNextSlot(new CollectibleStack(collected)))
        {
            growspace.getInventory().flush();
            CentralMessageOverlay.showMsg("New " + collected.getIngameName() + "!");
            return true;
        }
        return false;
    }

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
                return "bac_food_food";
            default:
                return "none";
        }
    }
    static String getGrowingStatus(Collectible collectible)
    {
        switch (collectible.getSpriteStatus())
        {
            case "fuel":
                return "bac_fuella_growing";
            default:
                return "none";
        }
    }
}
