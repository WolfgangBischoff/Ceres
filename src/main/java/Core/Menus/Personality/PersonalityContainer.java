package Core.Menus.Personality;

import Core.Actor;
import Core.GameVariables;
import Core.Menus.AchievmentLog.NewMessageOverlay;
import Core.Menus.CoinGame.CoinType;

import java.util.ArrayList;
import java.util.List;

public class PersonalityContainer
{
    private static final String CLASSNAME = "PersonalityContainer/";
    private Integer cooperation = 0;
    private Integer numberOfInteractions = 0;
    private Actor ownerActor;

    private List<CoinType> traits = new ArrayList<>();

    public PersonalityContainer(Actor owner)
    {
        this.ownerActor = owner;
    }

    @Override
    public String toString()
    {
        return "PersonalityContainer: " + traits.toString();
    }

    public boolean isPersonalityMatch(CoinType trait)
    {
        return traits.contains(trait);
    }

    public void increaseCooperation(Integer addition)
    {
        String methodName = "increaseCooperation() ";
        this.cooperation += addition;
        traits.forEach(trait ->
        {
            if (cooperation >= trait.getCooperationVisibilityThreshold() && trait.getCooperationVisibilityThreshold() >= 0)
                if(trait.setVisibility(true))
                    NewMessageOverlay.showMsg(ownerActor.getActorInGameName() + " has trait " + trait.getName());
        });
    }

    public void incrementNumberOfInteraction()
    {
        numberOfInteractions++;
        increaseCooperation(1);
    }

    public Integer getCooperation()
    {
        return cooperation;
    }

    public List<CoinType> getTraits()
    {
        return traits;
    }

    public List<CoinType> getVisibleCoins()
    {
        List<CoinType> visibleTraits = new ArrayList<>();
//        traits.forEach(trait ->
//        {
//            if (cooperation >= trait.getCooperationVisibilityThreshold()
//                    && trait.getCooperationVisibilityThreshold() >= 0
//                    || GameVariables.getPlayerKnowledge().contains(trait.getKnowledgeVisibility())
//            )
//                visibleTraits.add(trait);
//        });
        traits.forEach(trait ->
        {
            if (trait.getVisibility())
                visibleTraits.add(trait);
        });
        return visibleTraits;
    }

}
