package Core.ActorLogic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.ActorLogic.BacteriaCulture.FUEL_CULTURE;
import static Core.ActorLogic.BacteriaCulture.METAL_CULTURE;
import static Core.ActorLogic.BacteriaNutrition.NUTRITION_METAL;
import static Core.ActorLogic.BacteriaNutrition.NUTRITION_ORGANIC;
import static Core.ActorLogic.BacteriaSpore.FUEL_SPORE;
import static Core.ActorLogic.BacteriaSpore.METAL_SPORE;

public class GrowData
{
    public static final Map<BacteriaSpore, GrowData> sporeData = new HashMap<>();
    public static final Map<BacteriaCulture, GrowData> cultureData = new HashMap<>();

    static
    {
        GrowData fuel = new GrowData(FUEL_CULTURE, FUEL_SPORE, 10, 10, Arrays.asList(NUTRITION_ORGANIC));
        sporeData.put(FUEL_SPORE, fuel);
        cultureData.put(FUEL_CULTURE, fuel);

        GrowData metal = new GrowData(METAL_CULTURE, METAL_SPORE, 10, 10, Arrays.asList(NUTRITION_METAL));
        sporeData.put(METAL_SPORE, metal);
        cultureData.put(METAL_CULTURE, metal);
    }

    BacteriaCulture culture;
    BacteriaSpore spore;
    List<BacteriaNutrition> suitableNutritions;
    int minutesTillGrown;
    int minutesTillRotten;

    public GrowData(BacteriaCulture statusGrown, BacteriaSpore spore, int minutesTillGrown, int minutesTillRotten, List<BacteriaNutrition> suitableNutritions)
    {
        this.culture = statusGrown;
        this.spore = spore;
        this.minutesTillGrown = minutesTillGrown;
        this.minutesTillRotten = minutesTillRotten;
        this.suitableNutritions = suitableNutritions;
    }

    public boolean isNutritionSuitable(BacteriaNutrition nutrition)
    {
        return suitableNutritions.contains(nutrition);
    }

    public boolean isNutritionSuitable(String nutrition)
    {
        return suitableNutritions.contains(BacteriaNutrition.fromString(nutrition));
    }

}
