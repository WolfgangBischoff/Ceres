package Core.Menus.CoinGame;

import Core.Enums.Knowledge;

public enum CharacterCoinBuff implements CoinType
{
    BUFF_SLOWED("Slowed"), BUFF_DOUBLE_REWARD("Double Reward");//, PROTECTED, NO_CLICK, HARD_CLICK; //multiple clicks per coin?!

    int duration = 5;
    Long activeSince = null;
    String name;
    boolean visible;

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
    public boolean getVisibility()
    {
        return visible;
    }

    @Override
    public void setVisibility(boolean val)
    {
        visible = val;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
