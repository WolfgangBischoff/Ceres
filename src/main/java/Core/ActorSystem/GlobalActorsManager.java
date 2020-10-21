package Core.ActorSystem;

import Core.*;
import Core.Configs.Config;
import Core.Enums.Direction;

import java.util.*;

import static Core.Configs.Config.KEYWORD_ACTORS;
import static Core.Configs.Config.KEYWORD_GROUPS;

public class GlobalActorsManager
{
    private static final String CLASSNAME = "GlobalActorsManager/";
    private static final Set<String> keywords = new HashSet<>();
    static Map<String, Actor> actorsIdsMap = new HashMap<>();
    static ActorMonitor globalActorMonitor = new ActorMonitor();
    static Set<String> loadedSystems = new HashSet<>();

    public static void loadGlobalSystem(String path)
    {
        String methodName = "loadGlobalSystem() ";
        keywords.add(KEYWORD_ACTORS);
        keywords.add(KEYWORD_GROUPS);

        if(!loadedSystems.contains(path))
        {
            var lines = Utilities.readAllLineFromTxt(Config.RESOURCES_FILE_PATH + "global_systems/" + path + Config.CSV_POSTFIX);
            parse(lines);
            loadedSystems.add(path);
        }

    }

    private static void parse(List<String[]> file)
    {
        String methodName = "parse() ";
        String readStatus = null;
        List<String> createdActorGroupsId = new ArrayList<>();
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

                actor.updateCompoundStatus();
                List<SpriteData> spriteDataList = actor.getSpriteDataMap().get(actor.getCompoundStatus());
                for (int j = 0; j < spriteDataList.size(); j++)
                {
                    Sprite actorSprite;
                    SpriteData spriteData = spriteDataList.get(j);
                    actorSprite = Sprite.createSprite(spriteData, 0, 0);
                    actorSprite.setActor(actor);
                    actorSprite.setAnimationEnds(spriteData.animationEnds);
                    actor.setSpeed(spriteData.velocity);//Set as often as Sprites exist?
                    actor.addSprite(actorSprite);
                }

                actor.setStageMonitor(globalActorMonitor);
                actor.setActorId(lineData[0]);
                actorsIdsMap.put(lineData[0], actor);
            }
            else if (readStatus.equals(KEYWORD_GROUPS)) {
                int groupName_Idx = 0;
                int groupLogic_Idx = 1;
                int dependentGroupName_Idx = 2;
                int start_idx_memberIds = 3;
                createdActorGroupsId.add(lineData[groupName_Idx]);
                globalActorMonitor.getGroupToLogicMap().put(lineData[groupName_Idx], lineData[groupLogic_Idx]);
                globalActorMonitor.getGroupIdToInfluencedGroupIdMap().put(lineData[groupName_Idx], lineData[dependentGroupName_Idx]);

                //add actors to groups
                String actorId = null;
                try{
                    for (int membersIdx = start_idx_memberIds; membersIdx < lineData.length; membersIdx++)
                    {
                        actorId = lineData[membersIdx];
                        Actor actor = actorsIdsMap.get(actorId);
                        globalActorMonitor.addActorToActorSystem(lineData[groupName_Idx], actor);
                        actor.getMemberActorGroups().add(lineData[groupName_Idx]);
                    }
                }
                catch (NullPointerException e)
                {
                    throw new NullPointerException("Cannot find " + actorId + " in " + actorsIdsMap);
                }
            }
        }
        for(String groupId : createdActorGroupsId)
            globalActorMonitor.sendSignalFrom(groupId);//init new groups to init world variables
    }

    public static Map<String, Actor> getGlobalActors(List<String> actorIds)
    {
        String methodName = "getGlobalActors() ";
        Map<String, Actor> globalActors = new HashMap<>();
        actorIds.forEach(id ->
        {
            String[] splitActorId = id.split(",");//ggf there is a target status
            String actorID = splitActorId[0];
            if(splitActorId.length>=2)
            {
                actorsIdsMap.get(actorID).setGeneralStatus(splitActorId[1]);
                actorsIdsMap.get(actorID).updateCompoundStatus();
            }
            globalActors.put(actorID, actorsIdsMap.get(actorID));
        });

        return globalActors;
    }

}
