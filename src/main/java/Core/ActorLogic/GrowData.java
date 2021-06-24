package Core.ActorLogic;

import Core.Enums.CollectableType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GrowData
{
    String statusGrown;
    List<String> suitableNutritions = new ArrayList<>();
    int minutesTillGrown;
    int minutesTillRotten;

    public GrowData(String statusGrown, int minutesTillGrown, int minutesTillRotten, List<String> suitableNutritions)
    {
        this.statusGrown = statusGrown;
        this.minutesTillGrown = minutesTillGrown;
        this.minutesTillRotten = minutesTillRotten;
        this.suitableNutritions = suitableNutritions;
    }
}
