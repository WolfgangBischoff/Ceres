package Core.Menus.Email;

import Core.GameVariables;

public class EmailCondition
{
    boolean bedNeeded;
    String variableNeeded;
    String variableValueNeeded;

    public EmailCondition(boolean bedNeeded, String variableNeeded, String variableValueNeeded)
    {
        this.bedNeeded = bedNeeded;
        this.variableNeeded = variableNeeded;
        this.variableValueNeeded = variableValueNeeded;
    }

    public boolean evaluate(boolean slept)
    {
        String var = GameVariables.getGenericVariableManager().getValue(variableNeeded);
        if(var != null && var.equals(variableValueNeeded) && isSleepConditionFullfilled(slept))
            return true;
        return false;
    }

    private boolean isSleepConditionFullfilled(boolean slept)
    {
        return !bedNeeded || slept;
    }
}
