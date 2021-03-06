package Core.Enums;

public enum TriggerType
{
    NOTHING,
    PERSISTENT,
    PERSISTENT_TEXT,
    TIMED,
    TIMED_TEXT,
    TEXTBOX,
    TEXTBOX_ANALYSIS,
    TEXTBOX_CONDITION,
    COLLECTABLE,
    SCRIPT,
    INVENTORY_EXCHANGE,
    INVENTORY_SHOP,
    CONDITION,
    PERSISTENT_HARVEST,
    LEVELCHANGE,
    INCUBATOR;

    public static TriggerType getStatus(String status)
    {
        switch (status.toLowerCase())
        {
            case "none":
            case "nothing": return NOTHING;
            case "persistent": return PERSISTENT;
            case "persistent_text": return PERSISTENT_TEXT;
            case "timed": return TIMED;
            case "timed_text": return TIMED_TEXT;
            case "textbox": return TEXTBOX;
            case "textbox_analysis": return TEXTBOX_ANALYSIS;
            case "textbox_condition": return TEXTBOX_CONDITION;
            case "collectable": return COLLECTABLE;
            case "script": return SCRIPT;
            case "inventory_exchange": return INVENTORY_EXCHANGE;
            case "inventory_shop": return INVENTORY_SHOP;
            case "condition": return CONDITION;
            case "incubator": return INCUBATOR;
            case "persistent_harvest": return PERSISTENT_HARVEST;
            case "levelchange": return LEVELCHANGE;
            default: throw new RuntimeException("TriggerType unknown: " + status);
        }
    }

}
