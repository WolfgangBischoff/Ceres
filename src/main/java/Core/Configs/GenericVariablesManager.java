package Core.Configs;

import java.util.HashMap;
import java.util.Map;

public class GenericVariablesManager
{
    static private String CLASSNAME = "GenericVariablesManager/";
    static private Map<String, String> stringWorldVariables = new HashMap<>();

    public GenericVariablesManager()
    {
        Map<String, Boolean> ret = new HashMap<>();
        stringWorldVariables.put("transporter_air_system_quest_started", "false");
        stringWorldVariables.put("IS_LIFE_SUPP_OPEN", "false");
    }

    public String getValue(String varName)
    {
        return stringWorldVariables.get(varName);
    }

    public void setValue(String varName, String newValue)
    {
        String methodName = "setValue() ";
        System.out.println(CLASSNAME + methodName + "Set " + varName + " from " + stringWorldVariables.get(varName) + " to " + newValue);
        stringWorldVariables.put(varName,newValue);
    }

}
