package Core.ActorLogic;

import Core.Actor;
import Core.Collectible;
import Core.CollectibleStack;
import Core.Enums.CollectableType;
import Core.GameTime.DateTime;
import Core.GameVariables;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.WorldView.WorldView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static Core.Enums.CollectableType.*;

public class GrowspaceManager
{
    public static final String BUILDTIME = "buildtime";
    public static final String NUTRITION = "nutrition";
    private static final String CLASSNAME = "GrowspaceManager";
    private static Map<String, GrowData> seedData = new HashMap<>();
    private static Map<String, GrowData> grownData = new HashMap<>();

    static
    {
        GrowData fuel = new GrowData("bac_fuella_grown", 10, 10, Arrays.asList("bac_nutrition_food"));
        seedData.put("bac_fuella_growing", fuel);
        grownData.put("bac_fuella_grown", fuel);
        GrowData metal = new GrowData("bac_metal_grown", 10, 10, Arrays.asList("bac_nutrition_metal"));
        seedData.put("bac_metal_growing", metal);
        grownData.put("bac_metal_grown", metal);
    }

    public static void grow(Actor growspace)
    {

        String growplaceStatus = growspace.getGeneralStatus();
        DateTime currentTime = GameVariables.getClock().getCurrentGameTime();
        if (growplaceStatus.equals("empty"))
        {
            growspace.removeGenericDateTimeAttribute(BUILDTIME);
            growspace.removeGenericStringAttribute(NUTRITION);
        }
        else if (seedData.containsKey(growplaceStatus))//In seed state
        {
            GrowData seedData = GrowspaceManager.seedData.get(growplaceStatus);
            if (growspace.getGenericDateTimeAttribute(BUILDTIME) == null)
            {
                growspace.setGenericDateTimeAttribute(BUILDTIME, currentTime);
                System.out.println("Set: " + growspace.getGenericDateTimeAttribute(BUILDTIME));
                System.out.println("Finished " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(seedData.minutesTillGrown));
                System.out.println("rotten " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(seedData.minutesTillGrown + seedData.minutesTillGrown));
            }

            if (seedData.suitableNutritions.contains(growspace.getGenericStringAttributes().get(NUTRITION)))
            {
                DateTime grownTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(seedData.minutesTillGrown);
                if (currentTime.compareTo(grownTime) >= 0)
                {
                    growspace.setSpriteStatus(seedData.statusGrown);
                    growspace.setSensorStatus("readyToHarvest");
                }
            }
            else
            {
                DateTime rottenTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(5);
                if (currentTime.compareTo(rottenTime) >= 0)
                {
                    System.out.println("Rotten due to wrong nutrition");
                    growspace.getInventory().flush();
                    growspace.setSpriteStatus("rotten");
                    growspace.setSensorStatus("resetToEmpty");
                }
            }


        }
        else if (grownData.containsKey(growplaceStatus))// in grown state
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

    public static String getFoodStatus(Collectible collectible)
    {
        if (collectible.getType().contains(BACTERIA_NUTRITION))
        {
            if (collectible.getType().contains(FOOD))
                return "bac_nutrition_food";
            if (collectible.getType().contains(METAL))
                return "bac_nutrition_metal";
        }
        return "none";
    }


    public static String getGrowingStatus(Collectible collectible)
    {
        switch (collectible.getSpriteStatus())
        {
            case "fuel":
                return "bac_fuella_growing";
            case "metal":
                return "bac_metal_growing";
            default:
                return "none";
        }
    }

    public static void handleCollectible(CollectibleStack from, Actor growspace)
    {
        if (from.getCollectible().getType().contains(BACTERIA_NUTRITION) && growspace.getGeneralStatus().equals("empty"))
        {
            String nutritiontype = GrowspaceManager.getFoodStatus(from.getCollectible());
            growspace.setSpriteStatus(nutritiontype);
            growspace.setGenericStringAttribute(GrowspaceManager.NUTRITION, nutritiontype);
            growspace.getInventory().addNumberOfCollectibleNextSlot(from, 1);
        }
        else if (from.getCollectible().getType().contains(CollectableType.BACTERIA_SPORE) && (growspace.getGeneralStatus().startsWith("bac_nutrition_")))
        {
            growspace.setSpriteStatus(GrowspaceManager.getGrowingStatus(from.getCollectible()));
            growspace.getInventory().addNumberOfCollectibleNextSlot(from, 1);
        }
        else
            System.out.println(CLASSNAME + "nothing happpens");
    }
}
