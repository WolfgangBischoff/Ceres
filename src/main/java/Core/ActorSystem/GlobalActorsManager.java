package Core.ActorSystem;

import Core.*;
import Core.Enums.Direction;

import java.util.*;

import static Core.Config.KEYWORD_ACTORS;
import static Core.Config.KEYWORD_GROUPS;

public class GlobalActorsManager
{
    private static final String CLASSNAME = "GlobalSystemStatus ";
    private static final Set<String> keywords = new HashSet<>();
    static StageMonitor globalStageMonitor = new StageMonitor();
    static Set<String> loadedSystems = new HashSet<>();

    public static void loadGlobalSystem(String path)
    {
        String methodName = "loadGlobalSystem() ";
        keywords.add(KEYWORD_ACTORS);
        keywords.add(KEYWORD_GROUPS);

        if(!loadedSystems.contains(path))
        {
            var lines = Utilities.readAllLineFromTxt(Config.RESOURCES_FILE_PATH + "global_systems/" + path + Config.CSV_POSTFIX);
            readGlobalSystem(lines);
            loadedSystems.add(path);
            System.out.println(CLASSNAME + methodName + "loaded "+ globalStageMonitor);
        }

    }

    private static void readGlobalSystem(List<String[]> file)
    {
        String methodName = "readGlobalSystem() ";
        String readStatus = null;
        Map<String, Actor> actorsIdsMap = new HashMap<>();
        for (String[] lineData : file) {
            if (keywords.contains(lineData[0].toLowerCase()))
            {
                readStatus = lineData[0].toLowerCase();
                continue;
            }

            //Read Actors
            if (readStatus.equals(KEYWORD_ACTORS)) {
                //airVeR;airSystem/airVent/airVent;Right ventilation;off;default;undefined
                Actor actor = new Actor(lineData[1], lineData[2], lineData[3], lineData[4], Direction.getDirectionFromValue(lineData[5]));
                actor.setStageMonitor(globalStageMonitor);
                actorsIdsMap.put(lineData[0], actor);
            }
            else if (readStatus.equals(KEYWORD_GROUPS)) {
                int groupName_Idx = 0;
                int groupLogic_Idx = 1;
                int dependentGroupName_Idx = 2;
                int start_idx_memberIds = 3;
                //System.out.println(CLASS_NAME + methodName + Arrays.toString(lineData));
                globalStageMonitor.getGroupToLogicMap().put(lineData[groupName_Idx], lineData[groupLogic_Idx]);
                globalStageMonitor.getGroupIdToInfluencedGroupIdMap().put(lineData[groupName_Idx], lineData[dependentGroupName_Idx]);
                //add actors to groups
                for (int membersIdx = start_idx_memberIds; membersIdx < lineData.length; membersIdx++) {
                    String actorId = lineData[membersIdx];
                    globalStageMonitor.addActorToActorSystem(lineData[groupName_Idx], actorsIdsMap.get(actorId));
                }
            }
        }
    }

    public static Map<String, Actor> getGlobalActors(List<String> actorIds)
    {
        String methodName = "getGlobalActors() ";
        Map<String, Actor> globalActors = new HashMap<>();
        actorIds.forEach(id -> globalActors.put(id, globalStageMonitor.getActorById(id)));
        return globalActors;
    }

}
