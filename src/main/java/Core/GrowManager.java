package Core;

import Core.GameTime.DateTime;

import java.util.HashMap;
import java.util.Map;

public class GrowManager
{
    class GrowData
    {
        String statusGrown;
        String statusSeed;
        int minutesTillGrown;
        int minutesTillRotten;

        public GrowData(String statusGrown, int minutesTillGrown, int minutesTillRotten)
        {
            this.statusGrown = statusGrown;
           // this.statusSeed = statusSeed;
            this.minutesTillGrown = minutesTillGrown;
            this.minutesTillRotten = minutesTillRotten;
        }
    }

    private static GrowManager singleton;
    private Map<String, GrowData> seedData = new HashMap<>();
    private Map<String, GrowData> grownData = new HashMap<>();
    private GrowManager()
    {
        GrowData fuel = new GrowData("fuel", 10, 10);
        seedData.put("fuel_seed", fuel);
        grownData.put("fuel", fuel);
    }
    public static GrowManager get()
    {
        if(singleton == null)
            singleton = new GrowManager();
        return singleton;
    }


    public void grow(Actor growspace)
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
                growspace.setSpriteStatus("rotten");
                growspace.setSensorStatus("resetToEmpty");
            }
        }

    }
}
