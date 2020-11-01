package Core.Menus.Personality;

import Core.Enums.Knowledge;
import Core.Menus.DiscussionGame.CoinType;

public enum PersonalityTrait implements CoinType
{

    //self  	;social		    ;detail	 ;holistic	;logic	  ;emotion;conservative progressive ;
    INTROVERSION("Introversion"), EXTROVERSION("Extroversion"), SENSING("Sensing"), INTUITION("Intuition"), THINKING("Thinking"), FEELING("Feeling"), JUDGING("Judging"), PERCEIVING("Perceiving");

    int visibilityThreshold = 0;
    Knowledge visibilityKnowledge = null;
    String name;

    PersonalityTrait(String name)
    {
        this.name = name;
    }

    @Override
    public int getCooperationVisibilityThreshold()
    {
        return visibilityThreshold;
    }

    @Override
    public void setCooperationVisibilityThreshold(int visibilityThreshold)
    {
        this.visibilityThreshold = visibilityThreshold;
    }

    @Override
    public Knowledge getKnowledgeVisibility()
    {
        return visibilityKnowledge;
    }

    @Override
    public void setKnowledgeVisibility(Knowledge visibilityKnowledge)
    {
        this.visibilityKnowledge = visibilityKnowledge;
    }

    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return name() +
                " {visibilityThreshold=" + visibilityThreshold +
                ", visibilityKnowledge='" + visibilityKnowledge + "}";
    }


}
