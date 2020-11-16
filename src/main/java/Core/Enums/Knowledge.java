package Core.Enums;

import java.util.InputMismatchException;

public enum Knowledge
{
    AIR_SYSTEM_CONTROL_COMPUTE_TRAIT("Air System Terminal has Local Computation"), AIR_SYSTEM_NETWORK_SERVER_TRAIT("Air System Terminal has Server Protocols");

    private String name;

    Knowledge(String name)
    {
        this.name = name;
    }

    public static Knowledge of(String value)
    {
        switch (value.toUpperCase())
        {
            case "AIR_SYSTEM_CONTROL_COMPUTE_TRAIT":
                return AIR_SYSTEM_CONTROL_COMPUTE_TRAIT;
            case "AIR_SYSTEM_NETWORK_SERVER_TRAIT":
                return AIR_SYSTEM_NETWORK_SERVER_TRAIT;
            case "NONE":
                return null;
            default:
                throw new InputMismatchException("Knowledge unkown: " + value);
        }
    }

    public String getName()
    {
        return name;
    }
}
