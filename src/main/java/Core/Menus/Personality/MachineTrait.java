package Core.Menus.Personality;

import Core.Enums.Knowledge;
import Core.Menus.DiscussionGame.CoinType;

public enum MachineTrait implements CoinType
{
    COMPUTE_LOCAL("Local Computation"), COMPUTE_CLOUD("Cloud Computation"), COMPUTE_VIRTUAL("Virtualized Computation"),
    MANAGEMENT_DEBUG("Debugger"), MANAGEMENT_LOGGING("Logging"), MANAGEMENT_MONITORING("Monitoring");

    int visibilityThreshold = 0;
    Knowledge visibilityKnowledge = null;
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
