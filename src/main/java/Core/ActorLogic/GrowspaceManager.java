package Core.ActorLogic;

import Core.Actor;
import Core.Collectible;
import Core.CollectibleStack;
import Core.Enums.CollectableType;
import Core.GameTime.DateTime;
import Core.GameVariables;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.WorldView.WorldView;

import static Core.ActorLogic.GrowData.cultureData;
import static Core.ActorLogic.GrowData.sporeData;
import static Core.Enums.CollectableType.*;

public class GrowspaceManager
{
    public static final String BUILDTIME = "buildtime";
    public static final String NUTRITION = "nutrition";
    private static final String CLASSNAME = "GrowspaceManager";

    public static void grow(Actor growspace)
    {

        String growplaceStatus = growspace.getGeneralStatus();
        DateTime currentTime = GameVariables.getClock().getCurrentGameTime();
        if (growplaceStatus.equals("empty"))
        {
            growspace.removeGenericDateTimeAttribute(BUILDTIME);
            growspace.removeGenericStringAttribute(NUTRITION);
        }
        else if (sporeData.containsKey(BacteriaSpore.fromString(growplaceStatus)))//In seed state
        {
            GrowData data = sporeData.get(BacteriaSpore.fromString(growplaceStatus));
            if (growspace.getGenericDateTimeAttribute(BUILDTIME) == null)
            {
                growspace.setGenericDateTimeAttribute(BUILDTIME, currentTime);
                System.out.println("Set: " + growspace.getGenericDateTimeAttribute(BUILDTIME));
                System.out.println("Finished " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown));
                System.out.println("rotten " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown + data.minutesTillGrown));
            }

            if (data.isNutritionSuitable(growspace.getGenericStringAttributes().get(NUTRITION)))
            {
                DateTime grownTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown);
                if (currentTime.compareTo(grownTime) >= 0)
                {
                    growspace.setSpriteStatus(data.statusGrown.toString());
                    growspace.setSensorStatus("readyToHarvest");
                }
            }
            else
            {
                DateTime rottenTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(5);
                if (currentTime.compareTo(rottenTime) >= 0)
                {
                    System.out.println("Rotten due to wrong nutrition");
                    rotten(growspace);
                }
            }

        }
        else if (cultureData.containsKey(BacteriaCulture.fromString(growplaceStatus)))// in grown state
        {
            GrowData grown = cultureData.get(BacteriaCulture.fromString(growplaceStatus));
            DateTime rottenTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(grown.minutesTillGrown + grown.minutesTillGrown);
            if (currentTime.compareTo(rottenTime) >= 0)
            {
                rotten(growspace);
            }
        }

    }

    private static void rotten(Actor growspace)
    {
        growspace.getInventory().flush();
        growspace.setSpriteStatus("rotten");
        growspace.setSensorStatus("resetToEmpty");
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

    public static String getNutritionStatus(Collectible collectible)
    {
        if (collectible.getType().contains(BACTERIA_NUTRITION))
        {
            if (collectible.getType().contains(FOOD))
                return "NUTRITION_ORGANIC";
            if (collectible.getType().contains(METAL))
                return "NUTRITION_METAL";
        }
        return "none";
    }


    public static String getGrowingStatus(Collectible collectible)
    {
        switch (collectible.getSpriteStatus())
        {
            case "FUEL_SPORE":
                return "FUEL_SPORE";
            case "METAL_SPORE":
                return "METAL_SPORE";
            default:
                return "none";
        }
    }

    public static void handleCollectible(CollectibleStack from, Actor growspace)
    {
        if (from.getCollectible().getType().contains(BACTERIA_NUTRITION) && growspace.getGeneralStatus().equals("empty"))
        {
            String nutritiontype = GrowspaceManager.getNutritionStatus(from.getCollectible());
            growspace.setSpriteStatus(nutritiontype);
            growspace.setGenericStringAttribute(GrowspaceManager.NUTRITION, nutritiontype);
            growspace.getInventory().addNumberOfCollectibleNextSlot(from, 1);
        }
        else if (from.getCollectible().getType().contains(CollectableType.BACTERIA_SPORE) && (growspace.getGeneralStatus().startsWith("NUTRITION_")))
        {
            growspace.setSpriteStatus(GrowspaceManager.getGrowingStatus(from.getCollectible()));
            growspace.getInventory().addNumberOfCollectibleNextSlot(from, 1);
        }
        else
            System.out.println(CLASSNAME + "nothing happpens");
    }
}
