package Core.Menus.Personality;

import Core.Menus.DiscussionGame.CharacterCoin;
import Core.Menus.DiscussionGame.CoinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.Menus.Personality.PersonalityTrait.*;

public class PersonalityContainer
{
    private static final String CLASSNAME = "PersonalityContainer/";
    //public MyersBriggsPersonality myersBriggsPersonality;
    private Integer cooperation = 0;
    private Integer numberOfInteractions = 0;

    private List<CoinType> traitsV2 = new ArrayList<>();

    @Override
    public String toString()
    {
        return "PersonalityContainer: " + traitsV2.toString();
    }

    public boolean isPersonalityMatch(CoinType trait)
    {
        return traitsV2.contains(trait);
    }

    public void increaseCooperation(Integer addition)
    {
        String methodName = "increaseCooperation() ";
        this.cooperation += addition;
        //System.out.println(CLASSNAME + methodName + "added " + addition + " to " + cooperation);
    }

    public void incrementNumberOfInteraction()
    {
        numberOfInteractions++;
        increaseCooperation(1);
    }

    public Integer getNumberOfInteractions()
    {
        return numberOfInteractions;
    }

    public Integer getCooperation()
    {
        return cooperation;
    }


    public List<CoinType> getTraits()
    {
        return traitsV2;
    }

    public void setTraitsV2(List<CoinType> traitsV2)
    {
        this.traitsV2 = traitsV2;
    }
}
