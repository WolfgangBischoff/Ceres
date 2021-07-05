package Core.Menus.Textbox;

import Core.CollectibleStack;
import Core.Enums.CollectableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Menus.Inventory.Inventory.getPlayerInventory;

class OptionConditionData
{
    String conditionType = "NONE";
    int amount = 0;
    String collectableTechnicalName = "NONE";
    CollectableType collectableType = CollectableType.NONFUNCTION;
    Map<String, String> messageOutcomes = new HashMap<>();

    public String getEvaluatedResult()
    {
        switch (conditionType)
        {
            case TEXTBOX_OPTION_ATTRIBUTE_CONSUME_ITEMS:
            {
                if (getPlayerInventory().hasCollectibleStackOfTypeAndNumber(collectableTechnicalName, collectableType, amount))
                {
                    CollectibleStack s = getPlayerInventory().getCollectibleStackOfTypeOfTypeAndNumber(collectableTechnicalName, collectableType, amount);
                    getPlayerInventory().reduceCollectibleStack(s, amount);
                    return messageOutcomes.get(TEXTBOX_ATTRIBUTE_SUCCESS);
                }
                else
                    return messageOutcomes.get(TEXTBOX_ATTRIBUTE_DEFEAT);
            }
            case TEXTBOX_ATTRIBUTE_COIN_GAME:
            {
                if (getPlayerInventory().hasCollectibleStackOfTypeAndNumber(collectableTechnicalName, collectableType, amount))
                {
                    CollectibleStack s = getPlayerInventory().getCollectibleStackOfTypeOfTypeAndNumber(collectableTechnicalName, collectableType, amount);
                    getPlayerInventory().reduceCollectibleStack(s, amount);
                    return messageOutcomes.get(TEXTBOX_ATTRIBUTE_SUCCESS);
                }
                else
                    return messageOutcomes.get(TEXTBOX_ATTRIBUTE_DEFEAT);
            }
            default:
                return "ERROR";
        }

    }
}
