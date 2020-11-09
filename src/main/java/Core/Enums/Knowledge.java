package Core.Enums;

import java.util.InputMismatchException;

public enum Knowledge
{
    AIR_SYSTEM_CONTROL_COMPUTE_TRAIT, AIR_SYSTEM_NETWORK_SERVER_TRAIT;

    public static Knowledge of(String value)
    {
        switch (value.toUpperCase()) {
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
}