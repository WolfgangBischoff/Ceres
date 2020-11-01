package Core.Menus.DiscussionGame;

import Core.Enums.Knowledge;

public enum CharacterCoinBuff implements CoinType
{
    BUFF_SLOWED("buff_slowed"), BUFF_DOUBLE_REWARD("buff_double_reward");//, PROTECTED, NO_CLICK, HARD_CLICK; //multiple clicks per coin?!

    int duration = 5;
    Long activeSince = null;
    String name;

    CharacterCoinBuff(String name)
    {
        this.name = name;
    }

    @Override
    public int getCooperationVisibilityThreshold()
    {
        return 0;
    }

    @Override
    public void setCooperationVisibilityThreshold(int visibilityThreshold)
    {

    }

    @Override
    public Knowledge getKnowledgeVisibility()
    {
        return null;
    }

    @Override
    public void setKnowledgeVisibility(Knowledge visibilityKnowledge)
    {

    }

    @Override
    public String getName()
    {
        return name;
    }
}
