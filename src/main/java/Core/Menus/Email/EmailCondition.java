package Core.Menus.Email;

import Core.GameVariables;

public class EmailCondition
{
    String variableNeeded;
    String variableValueNeeded;

    public EmailCondition(String variableNeeded, String variableValueNeeded)
    {
        this.variableNeeded = variableNeeded;
        this.variableValueNeeded = variableValueNeeded;
    }

    public boolean evaluate()
    {
        String var = GameVariables.getGenericVariableManager().getValue(variableNeeded);
        if(var != null && var.equals(variableValueNeeded))
            return true;
        return false;
    }

}
