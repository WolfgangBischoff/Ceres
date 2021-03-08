package Core.ActorSystem;

import Core.Actor;
import Core.GameVariables;
import Core.WorldView.WorldView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Core.ActorSystem.SystemStatus.*;

public class ActorMonitor
{
    private final static String CLASSNAME = "ActorMonitor/";
    Map<String, String> groupToLogicMap = new HashMap<>();
    Map<String, String> groupIdToInfluencedGroupIdMap = new HashMap<>();
    Map<String, ActorGroup> groupIdToActorGroupMap = new HashMap<>();

    public Actor getActorById(String actorId)
    {
        for (Map.Entry<String, ActorGroup> entry : groupIdToActorGroupMap.entrySet())
        {
            if (entry.getValue().containsActor(actorId))
                return entry.getValue().getActor(actorId);
        }
        return null;
    }

    public void addActorToActorSystem(String actorSystemId, Actor actor)
    {
        boolean debug = false;
        String methodName = "addActorToActorSystem() ";
        if (!groupIdToActorGroupMap.containsKey(actorSystemId))
            groupIdToActorGroupMap.put(actorSystemId, new ActorGroup(actorSystemId));
        ActorGroup actorGroup = groupIdToActorGroupMap.get(actorSystemId);
        actorGroup.addActor(actor);

        if (debug)
            System.out.println(CLASSNAME + methodName + " added " + actor.getActorInGameName() + " to " + actorGroup);
    }

    public String isDependentOnGroup(List<String> checkedGroupId)
    {
        for (Map.Entry<String, String> entry : groupIdToInfluencedGroupIdMap.entrySet())
            if (checkedGroupId.contains(entry.getValue()))//actor can be in multiple groups but just be influenced by one
                return entry.getKey();
        return null;
    }


    public void sendSignalFrom(List<String> notifyingGroupsList)
    {
        String methodName = "sendSignalFrom(List<String>) ";
        //Notify all groups of the notifying Actor
        for (int i = 0; i < notifyingGroupsList.size(); i++)
        {
            sendSignalFrom(notifyingGroupsList.get(i));
        }
    }

    public void sendSignalFrom(String notifyingGroup)
    {
        String methodName = "sendSignalFrom(String) ";
        boolean debug = false;
        String targetGroupID = groupIdToInfluencedGroupIdMap.get(notifyingGroup);
        String logicCode = groupToLogicMap.get(notifyingGroup);

        if (debug)
            System.out.println(CLASSNAME + methodName + " " + notifyingGroup + " used " + logicCode + " on " + targetGroupID);
        switch (logicCode)
        {
            case "none":
                break;
            case "isBaseSystem":
                apply_baseSystemLogic_group(notifyingGroup, targetGroupID);
                break;
            case "allOn_default/locked":
                allOn_setSensorStatus(notifyingGroup, targetGroupID, UNLOCKED, LOCKED);
                break;
            case "always_sensorDefault_spriteOn":
                always_sensorStatus(notifyingGroup, targetGroupID, ON);
                always_spriteStatus(notifyingGroup, targetGroupID, ON);
                break;
            case "levelchange":
                changeLevel(notifyingGroup, targetGroupID);
                break;
            case "transitionOnChange":
                transitionOnChange(notifyingGroup, targetGroupID);
                break;
            case "setWorldVariableTrueIfSystemOn":
                setWorldVariableTrueIfSystemOn(notifyingGroup);
                break;
            default:
                throw new RuntimeException(CLASSNAME + methodName + "logicCode not found: " + logicCode);
        }
    }

    private void setWorldVariableTrueIfSystemOn(String notifyingGroup)
    {
        String methodName = "setGameVariable() ";
        ActorGroup notifier = groupIdToActorGroupMap.get(notifyingGroup);
        GameVariables.getGenericVariableManager().setValue(notifyingGroup, notifier.areAllMembersStatusOn().toString());
    }

    private void transitionOnChange(String notifyingGroup, String targetGroupID)
    {
        String methodName = "triggerOnChange() ";
        ActorGroup notifier = groupIdToActorGroupMap.get(notifyingGroup);
        ActorGroup signaled = groupIdToActorGroupMap.get(targetGroupID);
        signaled.setMemberToGeneralStatus(SystemStatus.TRANSITION);
    }

    private void changeLevel(String filename_level, String spawnId)
    {
        String methodName = "changeLevel(String) ";
        //System.out.println(CLASSNAME + methodName + "loaded: " + filename_level + " spawn at " + spawnId);
        WorldView.getSingleton().changeStage(filename_level, spawnId);
        //WorldView.getSingleton().saveStage();
        //WorldView.getSingleton().loadStage(filename_level, spawnId);
    }

    private void always_sensorStatus(String notifyingGroup, String targetGroupID, SystemStatus sensorStatus)
    {
        String methodName = "always_sensorStatus(String, String, String) ";
        ActorGroup notifier = groupIdToActorGroupMap.get(notifyingGroup);
        ActorGroup signaled = groupIdToActorGroupMap.get(targetGroupID);
        signaled.setMemberToSensorStatus(sensorStatus);
    }

    private void always_spriteStatus(String notifyingGroup, String targetGroupID, SystemStatus spriteStatus)
    {
        String methodName = "always_sensorStatus(String, String, String) ";
        ActorGroup notifier = groupIdToActorGroupMap.get(notifyingGroup);
        ActorGroup signaled = groupIdToActorGroupMap.get(targetGroupID);
        signaled.setMemberToGeneralStatus(spriteStatus);
    }

    private void allOn_setSensorStatus(String notifyingGroup, String targetGroupID, SystemStatus trueStatus, SystemStatus falseStatus)
    {
        String methodName = "allOn_setSensorStatus(String, String, String, String) ";
        ActorGroup notifier = groupIdToActorGroupMap.get(notifyingGroup);
        ActorGroup signaled = groupIdToActorGroupMap.get(targetGroupID);

        if (notifier.areAllMembersStatusOn())
            signaled.setMemberToSensorStatus(trueStatus);
        else
            signaled.setMemberToSensorStatus(falseStatus);

    }

    private void apply_baseSystemLogic_group(String checkedGroup, String dependentGroup)
    {
        String methodName = "apply_baseSystemLogic() ";

        ActorGroup checkedSystem = groupIdToActorGroupMap.get(checkedGroup);
        ActorGroup dependentSystem = groupIdToActorGroupMap.get(dependentGroup);
        String influencingSystemStatus = checkedSystem.areAllMembersStatusOn().toString();

        for (Actor influenced : dependentSystem.getSystemMembers())
        {
            String currentStatus = influenced.getGeneralStatus();
            String statusConsideringLogic = apply_baseSystemLogic_single(influencingSystemStatus, currentStatus);
            String dialogueId = influenced.getSpriteDataMap().get(statusConsideringLogic).get(0).dialogueID;
            String dialogueFile = influenced.getSpriteDataMap().get(statusConsideringLogic).get(0).dialogieFile;
            influenced.onMonitorSignal(statusConsideringLogic, null, dialogueId, dialogueFile);
        }
    }

    private String apply_baseSystemLogic_single(String influencingGroupStatus, String influencedActorStatus)
    {
        String baseSystemOfflineString = "basesystemoffline";
        switch (influencingGroupStatus.toLowerCase())
        {
            case "true":
                if (influencedActorStatus.equals(baseSystemOfflineString))
                    return "on";
                else
                    return influencedActorStatus;

            case "false":
                if (influencedActorStatus.equals("on"))
                    return baseSystemOfflineString;
                else
                    return influencedActorStatus;

            default:
                throw new RuntimeException("statusTransition not defined of status: " + influencingGroupStatus.toLowerCase());
        }
    }

    //for Actors to double check their status changes
    public boolean checkIfStatusIsValid(String influencedStatus, String influencingSystemId)
    {
        String methodName = "checkIfStatusIsValid(String, String) ";
        ActorGroup influencingSystem = groupIdToActorGroupMap.get(influencingSystemId);
        String logic = groupToLogicMap.get(influencingSystemId);

        if (logic.equals("isBaseSystem"))
        {
            String influencingSystemStatus = influencingSystem.areAllMembersStatusOn().toString();
            return apply_baseSystemLogic_single(influencingSystemStatus, influencedStatus).equals(influencedStatus);
        }
        return true;
    }

    public Map<String, String> getGroupToLogicMap()
    {
        return groupToLogicMap;
    }

    public void setGroupToLogicMap(Map<String, String> groupToLogicMap)
    {
        this.groupToLogicMap = groupToLogicMap;
    }

    public Map<String, String> getGroupIdToInfluencedGroupIdMap()
    {
        return groupIdToInfluencedGroupIdMap;
    }

    public void setGroupIdToInfluencedGroupIdMap(Map<String, String> groupIdToInfluencedGroupIdMap)
    {
        this.groupIdToInfluencedGroupIdMap = groupIdToInfluencedGroupIdMap;
    }

    public Map<String, ActorGroup> getGroupIdToActorGroupMap()
    {
        return groupIdToActorGroupMap;
    }

    public void setGroupIdToActorGroupMap(Map<String, ActorGroup> groupIdToActorGroupMap)
    {
        this.groupIdToActorGroupMap = groupIdToActorGroupMap;
    }

    @Override
    public String toString()
    {
        return
                "groupToLogicMap=" + groupToLogicMap +
                        ", groupIdToInfluencedGroupIdMap=" + groupIdToInfluencedGroupIdMap +
                        ", groupIdToActorGroupMap=" + groupIdToActorGroupMap
                ;
    }
}
