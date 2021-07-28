package Core.ActorSystem;

import Core.Actor;
import Core.Configs.Config;
import Core.Enums.Direction;
import Core.Sprite.Sprite;
import Core.Sprite.SpriteData;
import Core.Utilities;

import java.util.*;

import static Core.Configs.Config.MAPFILE_ACTORS;
import static Core.Configs.Config.MAPFILE_GROUPS;

public class GlobalActorsManager
{
    private static final String CLASSNAME = "GlobalActorsManager/";
    private static final Set<String> keywords = new HashSet<>();
    static Map<String, Actor> actorsIdsMap = new HashMap<>();
    static ActorMonitor globalActorMonitor = new ActorMonitor();
    static Set<String> loadedSystems = new HashSet<>();

    public static void loadGlobalSystem(String path)
    {
        keywords.add(MAPFILE_ACTORS);
        keywords.add(MAPFILE_GROUPS);

        if (!loadedSystems.contains(path)) {
            var lines = Utilities.readAllLineFromTxt("global_systems/" + path + Config.CSV_POSTFIX);
            parse(lines);
            loadedSystems.add(path);
        }

    }

    private static void parse(List<String[]> file)
    {
        String readStatus = null;
        List<String> createdActorGroupsId = new ArrayList<>();
        for (String[] lineData : file) {
            if (keywords.contains(lineData[0].toLowerCase())) {
                readStatus = lineData[0].toLowerCase();
                continue;
            }

            //Read Actors
            if (readStatus.equals(MAPFILE_ACTORS)) {
                //airVeR;airSystem/airVent/airVent;Right ventilation;off;default;undefined
                Actor actor = new Actor(lineData[1], lineData[2], lineData[3], lineData[4], Direction.of(lineData[5]));

                actor.updateCompoundStatus();
                List<SpriteData> spriteDataList = actor.getSpriteDataMap().get(actor.getCompoundStatus());
                for (int j = 0; j < spriteDataList.size(); j++) {
                    Sprite actorSprite;
                    SpriteData spriteData = spriteDataList.get(j);
                    actorSprite = Sprite.createSprite(spriteData, 0, 0);
                    actorSprite.setActor(actor);
                    actorSprite.setAnimationEnds(spriteData.animationEnds);
                    actor.setVelocity(spriteData.velocity);//Set as often as Sprites exist?
                    actor.addSprite(actorSprite);
                }

                actor.setStageMonitor(globalActorMonitor);
                actor.setActorId(lineData[0]);
                actorsIdsMap.put(lineData[0], actor);
            }
            else if (readStatus.equals(MAPFILE_GROUPS)) {
                int groupName_Idx = 0;
                int groupLogic_Idx = 1;
                int dependentGroupName_Idx = 2;
                int start_idx_memberIds = 3;
                createdActorGroupsId.add(lineData[groupName_Idx]);
                globalActorMonitor.getGroupToLogicMap().put(lineData[groupName_Idx], lineData[groupLogic_Idx]);
                globalActorMonitor.getGroupIdToInfluencedGroupIdMap().put(lineData[groupName_Idx], lineData[dependentGroupName_Idx]);

                //add actors to groups
                String actorId = null;
                try {
                    for (int membersIdx = start_idx_memberIds; membersIdx < lineData.length; membersIdx++) {
                        actorId = lineData[membersIdx];
                        Actor actor = actorsIdsMap.get(actorId);
                        globalActorMonitor.addActorToActorSystem(lineData[groupName_Idx], actor);
                        actor.getMemberActorGroups().add(lineData[groupName_Idx]);
                    }
                } catch (NullPointerException e) {
                    throw new NullPointerException("Cannot find " + actorId + " in " + actorsIdsMap);
                }
            }
        }
        for (String groupId : createdActorGroupsId)
            globalActorMonitor.sendSignalFrom(groupId);//init new groups to init world variables
    }

    public static Map<String, Actor> getAndUpdateGlobalActorsWithStatus(List<String> actorData)
    {
        Map<String, Actor> globalActors = new HashMap<>();
        int idIdx = 0, spriteStatusIdx = 1, directionIdx = 2, sensorStatusIdx = 3, dialogueFileIdx = 4, dialogueIdIdx = 5, scriptIdx = 6;
        actorData.forEach(id ->
        {
            String[] detailData = id.split(",");
            String actorId = detailData[idIdx].trim();
            for (int i = 0; i < detailData.length; i++)//check optional data to change actor
            {
                if (!detailData[i].trim().isEmpty())
                    switch (i) {
                        case 1:
                            actorsIdsMap.get(actorId).setSpriteStatus(detailData[spriteStatusIdx].trim());
                            break;
                        case 2:
                            actorsIdsMap.get(actorId).setDirection(Direction.of(detailData[directionIdx].trim()));
                            break;
                        case 3:
                            actorsIdsMap.get(actorId).setSensorStatus(detailData[sensorStatusIdx].trim());
                            break;
                        case 4:
                            actorsIdsMap.get(actorId).setDialogueFile(detailData[dialogueFileIdx].trim());
                            break;
                        case 5:
                            actorsIdsMap.get(actorId).setDialogueId(detailData[dialogueIdIdx].trim());
                            break;
                        case 6:
                            actorsIdsMap.get(actorId).setScript(detailData[scriptIdx].trim());
                            break;
                    }
            }
            globalActors.put(actorId, actorsIdsMap.get(actorId));
        });

        return globalActors;
    }

    public static Map<String, Actor> getGlobalActorsWithStatus(List<String> actorData)
    {
        Map<String, Actor> globalActors = new HashMap<>();
        actorData.forEach(id ->
        {
            String[] detailData = id.split(",");
            String actorId = detailData[0].trim();;
            globalActors.put(actorId, actorsIdsMap.get(actorId));
        });

        return globalActors;
    }

}
