package Core.Menus.Personality;

import Core.Enums.Knowledge;
import Core.Menus.CoinGame.CoinType;

public enum MachineTrait implements CoinType
{
    COMPUTE_LOCAL("Local Computation"), COMPUTE_CLOUD("Cloud Computation"), COMPUTE_VIRTUAL("Virtualized Computation"),
    MANAGEMENT_DEBUG("Debugger"), MANAGEMENT_LOGGING("Logging"), MANAGEMENT_MONITORING("Monitoring"),
    INTERFACE_DIRECT("Direct"), INTERFACE_ANALOG("Analog"), INTERFACE_CONNECTION("Connection"),
    NETWORK_UNCONNECTED("Unconnected"), NETWORK_SERVER("Server"), NETWORK_CLIENT("Client");

    int visibilityThreshold = 0;
    boolean visible;
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
    public boolean getVisibility()
    {
        return visible;
    }

    @Override
    public boolean setVisibility(boolean val)
    {
        if (!visible && val)
        {
            visible = val;
            return true;
        }
        else return false;
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
