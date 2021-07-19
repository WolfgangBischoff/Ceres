package Core.Configs;

import java.util.HashMap;
import java.util.Map;

public class GenericVariablesManager
{
    static private String CLASSNAME = "GenericVariablesManager/";
    static private Map<String, String> stringWorldVariables = new HashMap<>();

    public GenericVariablesManager()
    {
        stringWorldVariables.put("TRANSPORTER_AIR_SYSTEM_QUEST_TALKED_TO_SISTER", "false");
        stringWorldVariables.put("transporter_air_system_quest_started", "false");
        stringWorldVariables.put("IS_LIFE_SUPP_OPEN", "false");
        stringWorldVariables.put("AIR_SYSTEM_REBOOT_PART1_WON", "false");
        stringWorldVariables.put("METEROIT_HIT", "false");
        stringWorldVariables.put("METEROIT_DAMAGE_ANALYZED", "false");
        stringWorldVariables.put("STRANGER_APPEARANCE_QUEST", "false");
        stringWorldVariables.put("RESCUE_POD_QUEST_STATUS", "false");
        stringWorldVariables.put("ARRIVED_CERES", "false");
        stringWorldVariables.put("CAPIGENI_DELIVERED_BACTERIA", "false");
        stringWorldVariables.put("CAPIGENI_TRAINEE_AREA_ACESS_LEVEL_2", "true");
    }

    public String getValue(String varName)
    {
        return stringWorldVariables.get(varName);
    }

    public void setValue(String varName, String newValue)
    {
        System.out.println(CLASSNAME + " Set " + varName + " from " + stringWorldVariables.get(varName) + " to " + newValue);
        stringWorldVariables.put(varName,newValue);
    }

    public static Map<String, String> getStringWorldVariables()
    {
        return stringWorldVariables;
    }
}
