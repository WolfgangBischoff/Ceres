package Core.ActorSystem;

import Core.Actor;
import Core.Config;
import Core.Enums.Direction;
import Core.Utilities;
import Core.WorldLoader;

import java.util.*;

import static Core.Config.KEYWORD_ACTORS;
import static Core.Config.KEYWORD_GROUPS;

public class GlobalSystemStatus
{
    private static final String CLASSNAME = "GlobalSystemStatus ";
    ActorGroup actorGroup;
    SystemStatus systemStatus;
    GlobalSystemStatus dependentSystem;
    GlobalSystemStatus limitingSystem;
    private static final Set<String> keywords = new HashSet<>();


    public static List<GlobalSystemStatus> init()
    {
        String methodName = "init() ";
        keywords.add(KEYWORD_ACTORS);
        keywords.add(KEYWORD_GROUPS);

        List<GlobalSystemStatus> globalSystemStatuses = new ArrayList<>();
        //TODO load all Systems
        var lines = Utilities.readAllLineFromTxt(Config.RESOURCES_FILE_PATH + "global_systems/transporter_air_system" + Config.CSV_POSTFIX);
        lines.forEach(arr -> System.out.println(CLASSNAME + methodName + Arrays.toString(arr)));
        globalSystemStatuses.add(readGlobalSystem("global_systems/transporter_air_system", lines));

        return globalSystemStatuses;
    }

    private static GlobalSystemStatus readGlobalSystem(String systemName, List<String[]> file)
    {
        String methodName = "readGlobalSystem() ";
        String readStatus = null;
        ActorGroup actorGroup = new ActorGroup(systemName);
        List<Actor> actors = new ArrayList<>();
        for (String[] line : file)
        {
            if (keywords.contains(line[0].toLowerCase()))
                readStatus = line[0];

            //Read Actors
            if (readStatus.equals(KEYWORD_ACTORS))
            {
                //airVeR;airSystem/airVent/airVent;Right ventilation;off;default;undefined
                Actor actor = new Actor(line[1], line[2], line[3], line[4], Direction.getDirectionFromValue(line[5]));
                actors.add(actor);
            }
            else if (readStatus.equals(KEYWORD_GROUPS))
            {
                //TODO add actors to groups
//                int groupName_Idx = 0;
//                int groupLogic_Idx = 1;
//                int dependentGroupName_Idx = 2;
//                int start_idx_memberIds = 3;
//                //System.out.println(CLASS_NAME + methodName + Arrays.toString(lineData));
//                stageMonitor.groupToLogicMap.put(lineData[groupName_Idx], lineData[groupLogic_Idx]);
//                stageMonitor.groupIdToInfluencedGroupIdMap.put(lineData[groupName_Idx], lineData[dependentGroupName_Idx]);
//
//                //map for all contained group members in which groups they are: actor -> groups
//                WorldLoader.ActorGroupData actorGroupData;
//                for (int membersIdx = start_idx_memberIds; membersIdx < lineData.length; membersIdx++)
//                {
//                    String actorId = lineData[membersIdx];
//                    if (!actorGroupDataMap.containsKey(actorId))
//                    {
//                        actorGroupDataMap.put(actorId, new WorldLoader.ActorGroupData());
//                    }
//                    actorGroupData = actorGroupDataMap.get(actorId);
//                    actorGroupData.memberOfGroups.add(lineData[groupName_Idx]);
//                }
            }

        }
        //TODO Create System
        return new GlobalSystemStatus(systemName);
    }

    private GlobalSystemStatus(String systemId)
    {
        actorGroup = new ActorGroup(systemId);
    }

    /*
    We load all system at the beginning and keep them in memory
    Level can get the actors by id => globalactor; actorId
    if a actor is updateChecked() the system is updated from base
    the updates are unchecked() to avoid loops
     */

    private void addMember(Actor actor)
    {
        actorGroup.addActor(actor);

    }

    public void updateSystemChecked()
    {
        //TODO check if new status is valid

        //once status is valid update downstream
        updateDownstream();
    }

    private void updateSystemUnchecked()
    {
        //TODO update own status from members according to logic
    }

    private void updateDownstream()
    {
        updateSystemUnchecked();
        if (dependentSystem != null)
            dependentSystem.updateDownstream();
    }

    private void updateFromBase()
    {
        //Find base system and update
        if (limitingSystem != null)
        {
            limitingSystem.updateFromBase();
        }
        //once updated update yourself
        updateSystemUnchecked();
    }


    @Override
    public String toString()
    {
        return "GlobalSystemStatus{" +
                "actorGroup=" + actorGroup +
                ", systemStatus=" + systemStatus +
                '}';
    }
}
