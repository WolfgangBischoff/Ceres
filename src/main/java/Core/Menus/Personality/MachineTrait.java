package Core.Menus.Personality;

import Core.Menus.DiscussionGame.CoinType;

public enum MachineTrait implements CoinType
{
    COMPUTE_LOCAL("Local Computation"), COMPUTE_CLOUD("Cloud Computation"), COMPUTE_VIRTUAL("Virtualized Computation");

    int visibilityThreshold = 0;
    String visibilityKnowledge = "none";
    String name;

    MachineTrait(String name)
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
    public String getKnowledgeVisibility()
    {
        return visibilityKnowledge;
    }

    @Override
    public void setKnowledgeVisibility(String visibilityKnowledge)
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
