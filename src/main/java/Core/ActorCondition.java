package Core;

import Core.Enums.ActorConditionType;
import Core.Enums.CollectableType;

import java.util.ArrayList;
import java.util.List;

public class ActorCondition
{
    private static final String CLASSNAME = "ActorCondition/";
    String spriteStatusCondition, sensorStatusCondition,
            trueSpriteStatus, trueSensorStatus, trueDialogueId, trueDialogueFile,
            falseSpriteStatus, falseSensorStatus, falseDialogueId, falseDialogueFile;
    List<String> params = new ArrayList<>();
    ActorConditionType actorConditionType;

    @Override
    public String toString()
    {
        return "\nprecondition: " + spriteStatusCondition + " and " + sensorStatusCondition
                + "\nif " + actorConditionType
                + " params= " + params
                + "\n\tthen " + trueSpriteStatus + " " + trueSensorStatus
                + "\n\telse " + falseSpriteStatus + " " + falseSensorStatus
                ;
    }

    public boolean evaluate(Actor activeActor, Actor passiveActor)
    {
        switch (actorConditionType)
        {
            case HAS_ITEM:
                return hasItem(activeActor);
            case CONSUMES_ITEM:
                return consumesItem(activeActor);
            case  VARIABLE:
                return variable();
            default:
                throw new RuntimeException("ActorConditionType not defined: " + actorConditionType);
        }
    }

    private boolean hasItem(Actor actor)
    {
        //Params must be in pairs of name - type
        //If at least on item fits return true
        for (int i = 0; i < params.size(); i = i + 2)
        {
            String technicalName = params.get(i);
            CollectableType type = CollectableType.getType(params.get(i + 1));
            if (actor.inventory.hasCollectibleStackOfType(technicalName, type))
                return true;
        }
        return false;
    }

    private boolean consumesItem(Actor actor)
    {
        for (int i = 0; i < params.size(); i = i + 2)
        {
            String technicalName = params.get(i);
            CollectableType type = CollectableType.getType(params.get(i + 1));
            if (actor.inventory.hasCollectibleStackOfType(technicalName, type))
            {
                CollectibleStack s = actor.getInventory().getCollectibleStackOfType(technicalName, type);
                actor.getInventory().reduceCollectibleStack(s, 1);
                return true;
            }
        }
        return false;
    }

    private boolean variable()
    {
        String variableName = params.get(0);
        return Boolean.parseBoolean(GameVariables.getGenericVariableManager().getValue(variableName));
    }
}
