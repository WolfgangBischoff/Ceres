package Core.ActorLogic;

import Core.Actor;
import Core.Collectible;
import Core.CollectibleStack;
import Core.Enums.CollectableType;
import Core.Enums.Direction;
import Core.GameTime.DateTime;
import Core.GameVariables;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.List;

import static Core.ActorLogic.BacteriaNutrition.isBacteriaNutrition;
import static Core.ActorLogic.GrowData.cultureData;
import static Core.ActorLogic.GrowData.sporeData;
import static Core.Enums.CollectableType.*;
import static Core.Enums.Direction.*;
import static Core.Utilities.hasRandomEventOccured;

public class GrowspaceManager
{
    /*
    Spores kosten Geld
    Nutrition kostet Geld
    Weniger Spores setzen günstiger, aber langsamer spread
    Schnell verrottene Bacteria sollte mehr Spore haben
     */
    public static final String BUILDTIME = "buildtime";
    public static final String NUTRITION = "nutrition";
    private static final String CLASSNAME = "GrowspaceManager";

    public static void updateGrowplaces(List<Actor> growplaces)
    {
        checkNeighbors(growplaces);
        for (Actor a : growplaces)
            update(a);
    }

    public static void update(Actor growspace)
    {
        String growplaceStatus = growspace.getGeneralStatus();
        DateTime currentTime = GameVariables.getClock().getCurrentGameTime();
        if (growplaceStatus.equals("empty"))
        {
            growspace.removeGenericDateTimeAttribute(BUILDTIME);
            growspace.removeGenericStringAttribute(NUTRITION);
        }
        else if (isBacteriaNutrition(growplaceStatus))//In Nutrition state
        {

                updateNutritionState(growspace);

        }
        else if (sporeData.containsKey(BacteriaSpore.fromString(growplaceStatus)))//In seed state
        {
            updateSeedState(growspace, growplaceStatus, currentTime);
        }
        else if (cultureData.containsKey(BacteriaCulture.fromString(growplaceStatus)))// in grown state
        {
            updateGrownState(growspace, growplaceStatus, currentTime);
        }

    }

    private static void updateNutritionState(Actor growspace)
    {
        BacteriaSpore neighborSporeNorth = getBacteriaCultureNeighbor(growspace, NORTH);
        BacteriaSpore neighborSporeSouth = getBacteriaCultureNeighbor(growspace, SOUTH);
        BacteriaSpore neighborSporeWest = getBacteriaCultureNeighbor(growspace, WEST);
        BacteriaSpore neighborSporeEast = getBacteriaCultureNeighbor(growspace, EAST);

            if (neighborSporeNorth != null)
            {
                tryToSpread(growspace, neighborSporeNorth.toString());
            }
            if (neighborSporeSouth != null)
                tryToSpread(growspace, neighborSporeSouth.toString());
            if (neighborSporeWest != null)
                tryToSpread(growspace, neighborSporeWest.toString());
            if (neighborSporeEast != null)
                tryToSpread(growspace, neighborSporeEast.toString());

    }

    private static void tryToSpread(Actor growspace, String s)
    {
        if(hasRandomEventOccured(0.4))
        {
            System.out.println("Bacteria spread");
            growspace.setSpriteStatus(s);
        }
        else
            System.out.println("Bacteria did not spread");
    }

    private static BacteriaSpore getBacteriaCultureNeighbor(Actor growspace, Direction d)
    {
        Actor neightborGS = growspace.getGenericActorAttributes().get(d.toString());
        if (neightborGS == null)
            return null;
        BacteriaCulture neighborCulture = BacteriaCulture.fromString(neightborGS.getGeneralStatus());
        if (neighborCulture == null)
            return null;
        BacteriaSpore neighborSpore = cultureData.get(neighborCulture).spore;
        return neighborSpore;
    }

    private static void updateGrownState(Actor growspace, String growplaceStatus, DateTime currentTime)
    {
        GrowData grown = cultureData.get(BacteriaCulture.fromString(growplaceStatus));
        DateTime rottenTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(grown.minutesTillGrown + grown.minutesTillGrown);
        if (currentTime.compareTo(rottenTime) >= 0)
        {
            rotten(growspace);
        }
    }

    private static void updateSeedState(Actor growspace, String growplaceStatus, DateTime currentTime)
    {
        GrowData data = sporeData.get(BacteriaSpore.fromString(growplaceStatus));
        if (growspace.getGenericDateTimeAttribute(BUILDTIME) == null)
        {
            growspace.setGenericDateTimeAttribute(BUILDTIME, currentTime);
            //System.out.println("Set: " + growspace.getGenericDateTimeAttribute(BUILDTIME));
            //System.out.println("Finished " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown));
            //System.out.println("rotten " + growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown + data.minutesTillGrown));
        }

        if (data.isNutritionSuitable(growspace.getGenericStringAttributes().get(NUTRITION)))
        {
            DateTime grownTime = growspace.getGenericDateTimeAttribute(BUILDTIME).add(data.minutesTillGrown);
            if (currentTime.compareTo(grownTime) >= 0)
            {
                growspace.setSpriteStatus(data.culture.toString());
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

    private static void checkNeighbors(List<Actor> growspaces)
    {
        for (Actor gs : growspaces)
        {
            Rectangle2D currentRect = gs.getSpriteList().get(0).getHitbox();
            for (Actor otherGs : growspaces)
            {
                if (gs == otherGs)
                    continue;
                Rectangle2D otherRect = otherGs.getSpriteList().get(0).getHitbox();
                Direction d = nextTo(currentRect, otherRect);
                if (d != UNDEFINED)
                {
                    setNeightbors(gs, d, otherGs);
                }
            }
        }
    }

    private static void setNeightbors(Actor a, Direction d, Actor b)
    {
        a.setGenericActorAttribute(d.getOpposite().toString(), b);
        b.setGenericActorAttribute(d.toString(), a);
    }

    private static Direction nextTo(Rectangle2D a, Rectangle2D b)
    {
        var minX = a.getMinX();
        var minY = a.getMinY();
        var maxX = a.getMaxX();
        var maxY = a.getMaxY();
        if (b.contains(new Point2D(minX - 1, minY + 1)))
            return EAST;
        if (b.contains(new Point2D(maxX + 1, minY + 1)))
            return WEST;
        if (b.contains(new Point2D(minX + 1, minY - 1)))
            return SOUTH;
        if (b.contains(new Point2D(minX + 1, maxY + 1)))
            return NORTH;
        return UNDEFINED;

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
        throw new RuntimeException("Nutrition Status unknown: " + collectible.getSpriteStatus());
    }


    public static String getGrowingStatus(Collectible collectible)
    {
        switch (collectible.getSpriteStatus().toUpperCase())
        {
            case "FUEL_SPORE":
                return "FUEL_SPORE";
            case "METAL_SPORE":
                return "METAL_SPORE";
            default:
                throw new RuntimeException("GrowStatus unknown: " + collectible.getSpriteStatus());
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
