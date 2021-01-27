package Core;


import Core.ActorSystem.ActorMonitor;
import Core.Configs.Config;
import Core.Enums.*;
import Core.Menus.CoinGame.CoinType;
import Core.Menus.Inventory.Inventory;
import Core.Menus.Inventory.InventoryController;
import Core.Menus.Personality.PersonalityContainer;
import Core.Sprite.Sprite;
import Core.Sprite.SpriteData;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.*;

import static Core.Configs.Config.*;
import static Core.Enums.ActorTag.*;
import static Core.Enums.Direction.*;
import static Core.Enums.Direction.WEST;
import static Core.Enums.TriggerType.CONDITION;
import static Core.Enums.TriggerType.TEXTBOX_CONDITION;

public class Actor
{
    private static final String CLASSNAME = "Actor/";
    private static final Set<String> actorDefinitionKeywords = new HashSet<>();

    //General
    String actorFileName;
    String actorId;
    String actorInGameName;
    private Direction direction;
    private double velocityX;
    private double velocityY;
    private double speed = 50;
    private double interactionAreaWidth = 8;
    private double interactionAreaDistance = 30;
    private double interactionAreaOffsetX = 0;
    private double interactionAreaOffsetY = 0;
    private Long lastInteraction = 0L;
    private Long lastAutomaticInteraction = 0L;

    //Sprite
    String generalStatus;
    String compoundStatus = "default";
    final Map<String, String> statusTransitions = new HashMap<>();
    final Map<String, List<SpriteData>> spriteDataMap = new HashMap<>();
    final List<Sprite> spriteList = new ArrayList<>();

    //Sensor
    Map<String, SensorStatus> sensorStatusMap = new HashMap<>();
    SensorStatus sensorStatus;

    List<ActorCondition> conditions = new ArrayList<>();
    private String collectable_type;
    String textbox_analysis_group_name = "none";
    ActorMonitor actorMonitor;
    List<String> memberActorGroups = new ArrayList<>();
    Inventory inventory;
    public Set<ActorTag> tags = new HashSet<>();
    PersonalityContainer personalityContainer;
    Map<String, Double> numeric_generic_attributes = new HashMap<>();
    Script script;

    public Actor(String actorFileName, String actorInGameName, String initGeneralStatus, String initSensorStatus, Direction direction)
    {
        String methodName = "Actor() ";
        inventory = new Inventory(this);

        this.actorFileName = actorFileName;
        this.actorInGameName = actorInGameName;
        this.generalStatus = initGeneralStatus.toLowerCase();
        this.direction = direction;
        List<String[]> actordata;

        if (actorDefinitionKeywords.isEmpty()) //To avoid adding for each actor
        {
            actorDefinitionKeywords.add(KEYWORD_transition);
            actorDefinitionKeywords.add(KEYWORD_interactionArea);
            actorDefinitionKeywords.add(KEYWORD_text_box_analysis_group);
            actorDefinitionKeywords.add(COLLECTIBLE_DATA_ACTOR);
            actorDefinitionKeywords.add(CONTAINS_COLLECTIBLE_ACTOR);
            actorDefinitionKeywords.add(KEYWORD_sensorStatus);
            actorDefinitionKeywords.add(TAGS_ACTOR);
            actorDefinitionKeywords.add(CONDITION_ACTOR);
            actorDefinitionKeywords.add(SUSPICIOUS_VALUE_ACTOR);
            actorDefinitionKeywords.add(PERSONALITY_ACTOR);
            actorDefinitionKeywords.add(SCRIPT_ACTOR);
        }
        actordata = Utilities.readAllLineFromTxt(actorFileName + CSV_POSTFIX);
        for (String[] linedata : actordata) {
            if (checkForKeywords(linedata))
                continue;

            //Collect Actor Sprite Data
            try {
                SpriteData data = SpriteData.tileDefinition(linedata);
                data.animationDuration = Double.parseDouble(linedata[SpriteData.getAnimationDurationIdx()]);
                data.velocity = Integer.parseInt(linedata[SpriteData.getVelocityIdx()]);
                data.dialogueID = linedata[SpriteData.getDialogueIDIdx()];
                data.animationEnds = Boolean.parseBoolean(linedata[SpriteData.getAnimationEndsIdx()]);
                String statusName = linedata[0].toLowerCase();
                if (!spriteDataMap.containsKey(statusName))
                    spriteDataMap.put(statusName, new ArrayList<>());
                spriteDataMap.get(statusName).add(data);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(e.getMessage() + "\n in Actorfile: " + actorFileName);
            }
        }

        sensorStatus = sensorStatusMap.get(initSensorStatus);

        if (sensorStatus == null)
            System.out.println(CLASSNAME + methodName + actorInGameName + " no sensor found: " + initSensorStatus);
    }

    private boolean checkForKeywords(String[] linedata)
    {
        int keywordIdx = 0;
        String possibleKeyword = linedata[keywordIdx];
        String keyword;

        if (actorDefinitionKeywords.contains(possibleKeyword))
            keyword = possibleKeyword;
        else
            return false;

        switch (keyword) {
            case KEYWORD_transition:
                statusTransitions.put(linedata[1].toLowerCase(), linedata[2].toLowerCase());// old/new status
                break;
            case KEYWORD_interactionArea:
                double areaDistance = Double.parseDouble(linedata[1]);
                double areaWidth = Double.parseDouble(linedata[2]);
                double offsetX = Double.parseDouble(linedata[3]);
                double offsetY = Double.parseDouble(linedata[4]);
                interactionAreaDistance = areaDistance;
                interactionAreaWidth = areaWidth;
                interactionAreaOffsetX = offsetX;
                interactionAreaOffsetY = offsetY;
                break;
            case KEYWORD_text_box_analysis_group:
                textbox_analysis_group_name = linedata[1];
                break;
            case COLLECTIBLE_DATA_ACTOR:
                collectable_type = linedata[1];
                getNumeric_generic_attributes().put("base_value", Double.parseDouble(linedata[2]));
                break;
            case CONTAINS_COLLECTIBLE_ACTOR:
                Collectible collectible = Collectible.createCollectible(linedata[1], linedata[2], linedata[3]);
                inventory.addItem(collectible);
                break;
            case KEYWORD_sensorStatus:
                sensorStatusMap.put(linedata[1], readSensorData(linedata));
                break;
            case TAGS_ACTOR:
                tags.addAll(readTagData(linedata));
                break;
            case CONDITION_ACTOR:
                conditions.add(readCondition(linedata));
                break;
            case PERSONALITY_ACTOR:
                personalityContainer = readPersonality(linedata);
                break;
            case SUSPICIOUS_VALUE_ACTOR:
                numeric_generic_attributes.put(linedata[0], Double.parseDouble(linedata[1]));
                break;
            case SCRIPT_ACTOR:
                script = readScript(linedata);
                break;
            default:
                throw new RuntimeException("Keyword unknown: " + keyword);
        }

        return true;
    }

    private Script readScript(String[] linedata)
    {
        return new Script(Utilities.readXMLFile(linedata[1]));
    }

    private PersonalityContainer readPersonality(String[] linedata)
    {
        String methodName = "readPersonality() ";
        PersonalityContainer readContainer = new PersonalityContainer(this);
        int initCooperationValue = Integer.parseInt(linedata[1]);
        readContainer.increaseCooperation(initCooperationValue);
        int firstTraitIdx = 2;
        for (int i = firstTraitIdx; i < linedata.length; i++) {
            String[] traitData = linedata[i].split(",");
            CoinType coinType = CoinType.of(traitData[0]);
            Integer cooperationThreshold = -1;
            if (Utilities.tryParseInt(traitData[1]))
                cooperationThreshold = Integer.parseInt(traitData[1]);
            String knowledge = traitData[2];
            coinType.setCooperationVisibilityThreshold(cooperationThreshold);
            coinType.setKnowledgeVisibility(Knowledge.of(knowledge));
            readContainer.getTraits().add(coinType);
        }
        return readContainer;
    }

    public void setDialogueFile(String dialogueFile)
    {
        spriteList.forEach(sprite -> sprite.setDialogueFileName(dialogueFile));
    }

    public void setDialogueId(String dialogueId)
    {
        spriteList.forEach(sprite -> sprite.setInitDialogueId(dialogueId));
    }

    private ActorCondition readCondition(String[] linedata)
    {
        String methodName = "readCondition() ";
        //#condition; if sprite-status ;if sensor-status ;type ;true-sprite-status ;true-sensor-status ;false-sprite-status ;false-sensor-status	;params
        int spriteStatusConditionIdx = 1;
        int sensorStatusConditionIdx = 2;
        int actorConditionTypeIdx = 3;
        int trueSpriteStatusIdx = 4;
        int trueSensorStatusIdx = 5;
        int falseSpriteStatusIdx = 6;
        int falseSensorStatusIdx = 7;
        int paramsIdx = 8;
        ActorCondition actorCondition = new ActorCondition();
        actorCondition.spriteStatusCondition = linedata[spriteStatusConditionIdx];
        actorCondition.sensorStatusCondition = linedata[sensorStatusConditionIdx];
        actorCondition.actorConditionType = ActorConditionType.getConditionFromValue(linedata[actorConditionTypeIdx]);
        actorCondition.trueSpriteStatus = linedata[trueSpriteStatusIdx];
        actorCondition.trueSensorStatus = linedata[trueSensorStatusIdx];
        actorCondition.falseSpriteStatus = linedata[falseSpriteStatusIdx];
        actorCondition.falseSensorStatus = linedata[falseSensorStatusIdx];
        actorCondition.params.addAll(Arrays.asList(linedata).subList(paramsIdx, linedata.length));
        return actorCondition;
    }

    private Set<ActorTag> readTagData(String[] linedata)
    {
        Set<ActorTag> tagDataSet = new HashSet<>();
        int startIdxTags = 1;
        for (int i = startIdxTags; i < linedata.length; i++) {
            tagDataSet.add(ActorTag.getType(linedata[i]));
        }
        return tagDataSet;
    }

    private SensorStatus readSensorData(String[] lineData) throws ArrayIndexOutOfBoundsException
    {
        String methodName = "readSensorData(String[]) ";
        int sensorDataNameIdx = 1;

        int onInteractionIdx = 2;
        int onInteractionToStatusIdx = 3;
        int onInteraction_TriggerSensorIdx = 4;
        int onInteraction_StatusSensorIdx = 5;

        int onInRangeIdx = 6;
        int onInRangeToStatusIdx = 7;
        int onInRange_TriggerSensorIdx = 8;
        int onInRange_StatusSensorIdx = 9;

        int onIntersectionIdx = 10;
        int onIntersectionToStatusIdx = 11;
        int onIntersection_TriggerSensorIdx = 12;
        int onIntersection_StatusSensorIdx = 13;

        int onUpdateIdx = 14;
        int onUpdateToStatusIdx = 15;
        int onUpdate_TriggerSensorIdx = 16;
        int onUpdate_StatusSensorIdx = 17;

        int onMonitorIdx = 18;
        int onMonitor_TriggerSensorIdx = 19;

        int onTextBoxIdx = 20;
        int onTextBox_TriggerSensorIdx = 21;

        SensorStatus sensorStatus = new SensorStatus(lineData[sensorDataNameIdx]);
        try {
            sensorStatus.onInteraction_TriggerSprite = TriggerType.getStatus(lineData[onInteractionIdx]);
            sensorStatus.onInteractionToStatusSprite = lineData[onInteractionToStatusIdx];
            sensorStatus.onInteraction_TriggerSensor = TriggerType.getStatus(lineData[onInteraction_TriggerSensorIdx]);
            sensorStatus.onInteraction_StatusSensor = lineData[onInteraction_StatusSensorIdx];

            sensorStatus.onInRange_TriggerSprite = TriggerType.getStatus(lineData[onInRangeIdx]);
            sensorStatus.onInRangeToStatusSprite = lineData[onInRangeToStatusIdx];
            sensorStatus.onInRange_TriggerSensor = TriggerType.getStatus(lineData[onInRange_TriggerSensorIdx]);
            sensorStatus.onInRangeToStatusSensorStatus = lineData[onInRange_StatusSensorIdx];

            sensorStatus.onIntersection_TriggerSprite = TriggerType.getStatus(lineData[onIntersectionIdx]);
            sensorStatus.onIntersectionToStatusSprite = lineData[onIntersectionToStatusIdx];
            sensorStatus.onIntersection_TriggerSensor = TriggerType.getStatus(lineData[onIntersection_TriggerSensorIdx]);
            sensorStatus.onIntersection_StatusSensor = lineData[onIntersection_StatusSensorIdx];

            sensorStatus.onUpdate_TriggerSprite = TriggerType.getStatus(lineData[onUpdateIdx]);
            sensorStatus.onUpdateToStatusSprite = lineData[onUpdateToStatusIdx];
            sensorStatus.onUpdate_TriggerSensor = TriggerType.getStatus(lineData[onUpdate_TriggerSensorIdx]);
            sensorStatus.onUpdate_StatusSensor = lineData[onUpdate_StatusSensorIdx];

            sensorStatus.onMonitorSignal_TriggerSprite = TriggerType.getStatus(lineData[onMonitorIdx]);
            sensorStatus.onMonitor_TriggerSensor = TriggerType.getStatus(lineData[onMonitor_TriggerSensorIdx]);

            sensorStatus.onTextBoxSignal_SpriteTrigger = TriggerType.getStatus(lineData[onTextBoxIdx]);
            sensorStatus.onTextBox_TriggerSensor = TriggerType.getStatus(lineData[onTextBox_TriggerSensorIdx]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(actorInGameName + "\n" + e.getMessage());
        }
        return sensorStatus;
    }

    public void onUpdate(Long currentNanoTime)
    {
        //No lastInteraction time update, just resets if not used. like a automatic door
        String methodName = "onUpdate(Long) ";
        //double elapsedTimeSinceLastInteraction = (currentNanoTime - lastInteraction) / 1000000000.0;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastAutomaticInteraction) / 1000000000.0;
        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS) {
            //Sprite
            if (sensorStatus.onUpdate_TriggerSprite != TriggerType.NOTHING && !sensorStatus.onUpdateToStatusSprite.equals(generalStatus))
                evaluateTriggerType(sensorStatus.onUpdate_TriggerSprite, sensorStatus.onUpdateToStatusSprite, null);

            //SensorStatus
            if (sensorStatus.getOnUpdate_TriggerSensor() == CONDITION)
                updateStatusFromConditions(this);
            else if (sensorStatus.onUpdate_TriggerSensor != TriggerType.NOTHING && !sensorStatus.onUpdate_StatusSensor.equals(sensorStatus.statusName))
                setSensorStatus(sensorStatus.onUpdate_StatusSensor);

        }
    }

    private void updateStatusFromConditions(Actor activeActor)
    {
        String methodName = "updateStatusFromConditions() ";
        boolean debug = false;
        for (ActorCondition condition : conditions) {
            if //check pre-condition
            (
                    (generalStatus.equals(condition.spriteStatusCondition) || condition.spriteStatusCondition.equals("*"))
                            &&
                            (sensorStatus.statusName.equals(condition.sensorStatusCondition) || condition.sensorStatusCondition.equals("*"))
            ) {
                if (condition.evaluate(activeActor, this))
                //condition met
                {
                    if (!condition.trueSpriteStatus.equals("*")) {
                        generalStatus = condition.trueSpriteStatus;
                        updateCompoundStatus();
                    }
                    if (!condition.trueSensorStatus.equals("*"))
                        setSensorStatus(condition.trueSensorStatus);

                    if (debug)
                        System.out.println(CLASSNAME + methodName + " condition met " + generalStatus + " " + sensorStatus.statusName);
                }
                else
                //condition not met
                {
                    if (!condition.falseSpriteStatus.equals("*")) {
                        generalStatus = condition.falseSpriteStatus;
                        updateCompoundStatus();
                    }
                    if (!condition.falseSensorStatus.equals("*"))
                        setSensorStatus(condition.falseSensorStatus);

                    if (debug)
                        System.out.println(CLASSNAME + methodName + "Not met " + generalStatus + " " + sensorStatus.statusName);
                }
            }
        }
    }

    public void onInteraction(Sprite activeSprite, Long currentNanoTime)
    {
        String methodName = "onInteraction(Sprite, Long) ";
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastInteraction) / 1000000000.0;

        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_INTERACTIONS) {
            if (sensorStatus.getOnInteraction_TriggerSensor() == CONDITION
                    || sensorStatus.getOnInteraction_TriggerSprite() == CONDITION
                    || sensorStatus.getOnInteraction_TriggerSensor() == TEXTBOX_CONDITION
                    || sensorStatus.getOnInteraction_TriggerSprite() == TEXTBOX_CONDITION)
                updateStatusFromConditions(activeSprite.getActor());

            //react
            evaluateTriggerType(sensorStatus.onInteraction_TriggerSprite, sensorStatus.onInteractionToStatusSprite, activeSprite.getActor());
            setLastInteraction(currentNanoTime);
        }
    }

    /***
     * DialogueId and File is just set by this method, other methods just change sprite status
     * @param newCompoundStatus
     * @param newSensorStatue
     * @param newDialogueId
     * @param newDialogueFile
     */
    public void onMonitorSignal(String newCompoundStatus, String newSensorStatue, String newDialogueId, String newDialogueFile)
    {
        String methodName = "onMonitorSignal() ";
        if (sensorStatus.onMonitorSignal_TriggerSprite == null)
            System.out.println(CLASSNAME + methodName + "OnMonitorSignal not set");

        if(newDialogueId != null)
            setDialogueId(newDialogueId);
        evaluateTriggerType(sensorStatus.onMonitorSignal_TriggerSprite, newCompoundStatus, null);
    }

    public void onTextboxSignal(String newCompoundStatus)
    {
        String methodName = "onTextboxSignal() ";
        evaluateTriggerType(sensorStatus.onTextBoxSignal_SpriteTrigger, newCompoundStatus, null);
    }

    public void onIntersection(Sprite detectedSprite, Long currentNanoTime)
    {
        String methodName = "onIntersection() ";
        boolean debug = false;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastAutomaticInteraction) / 1000000000.0;

        //Check if detection is relevant
        boolean actorRelevant = true;
        if (detectedSprite.getActor() == null ||
                (
                        (tags.contains(AUTOMATED_DOOR) && !detectedSprite.getActor().tags.contains(AUTOMATED_DOOR_DETECTABLE)) //is door and other detectable
                                || tags.contains(BECOME_TRANSPARENT) && !detectedSprite.getActor().tags.contains(AUTOMATED_DOOR_DETECTABLE) //for roof
                                || tags.contains(DETECTS_PLAYER) && !detectedSprite.getActor().tags.contains(PLAYER) //for trigger
                )
        )
            actorRelevant = false;

        //trigger
        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS && actorRelevant) {
            if (debug)
                System.out.println(CLASSNAME + methodName + actorFileName + " onIntersection " + detectedSprite.getName());

            //Sprite Status
            if (sensorStatus.onIntersection_TriggerSprite != TriggerType.NOTHING) {
                evaluateTriggerType(sensorStatus.onIntersection_TriggerSprite, sensorStatus.onIntersectionToStatusSprite, detectedSprite.getActor());
            }

            //SensorStatus
            if (sensorStatus.onIntersection_TriggerSensor != TriggerType.NOTHING)
                setSensorStatus(sensorStatus.onIntersection_StatusSensor);
            lastAutomaticInteraction = currentNanoTime;
        }
    }

    public void onInRange(Sprite detectedSprite, Long currentNanoTime)
    {
        String methodName = "onInRange(Sprite, Long) ";
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastAutomaticInteraction) / 1000000000.0;

        //TODO general lookup
        if (
                (tags.contains(AUTOMATED_DOOR) && !detectedSprite.getActor().tags.contains(AUTOMATED_DOOR_DETECTABLE))
                        || tags.contains(BECOME_TRANSPARENT) && !detectedSprite.getActor().tags.contains(AUTOMATED_DOOR_DETECTABLE)
        )
            return;

        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS) {
            evaluateTriggerType(sensorStatus.onInRange_TriggerSprite, sensorStatus.onInRangeToStatusSprite, detectedSprite.getActor());
            lastAutomaticInteraction = currentNanoTime;
        }
    }

    private void changeLayer(Sprite sprite, int targetLayer)
    {
        String methodName = "changeLayer() ";
        WorldView.getBottomLayer().remove(sprite);
        WorldView.getMiddleLayer().remove(sprite);
        WorldView.getTopLayer().remove(sprite);
        switch (targetLayer) {
            case 0:
                WorldView.getBottomLayer().add(sprite);
                break;
            case 1:
                WorldView.getMiddleLayer().add(sprite);
                break;
            case 2:
                WorldView.getTopLayer().add(sprite);
                break;
        }
    }

    private void changeSprites()
    {
        String methodName = "changeSprites() ";
        List<SpriteData> targetSpriteData = spriteDataMap.get(compoundStatus.toLowerCase());

        if (targetSpriteData == null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, List<SpriteData>> entry : spriteDataMap.entrySet())
                stringBuilder.append("\t").append(entry.getKey()).append("\n");
            throw new RuntimeException(compoundStatus + " not found in \n" + stringBuilder.toString());
        }

        if (spriteList.isEmpty())//Before Actor is initialized
            return;

        //For all Sprites of the actor onUpdate to new Status
        for (int i = 0; i < spriteList.size(); i++) {
            SpriteData ts = targetSpriteData.get(i);
            Sprite toChange = spriteList.get(i);
            toChange.setImage(ts.spriteName, ts.fps, ts.totalFrames, ts.cols, ts.rows, ts.frameWidth, ts.frameHeight);
            toChange.setBlocker(ts.blocking);
            toChange.setLightningSpriteName(ts.lightningSprite);
            toChange.setAnimationEnds(ts.animationEnds);
            toChange.setLayer(ts.heightLayer);
            if (WorldView.getBottomLayer().contains(toChange) || WorldView.getMiddleLayer().contains(toChange) || WorldView.getTopLayer().contains(toChange))
                changeLayer(toChange, ts.heightLayer);//Change layer if sprite in current stage, not on other map (global system) but sprite change triggered by StageMonitor
        }

    }

    private void evaluateTargetStatus(String targetStatusField)
    {
        String methodName = "evaluateTargetStatus(String) ";

        //Do lookup (status is toggled from definition of actorfile)
        if (targetStatusField.toLowerCase().equals(Config.KEYWORD_transition))
            transitionGeneralStatus();
        else
            generalStatus = targetStatusField.toLowerCase();

        //Check if status is valid dependent on influencing system
        String influencedOfGroup = actorMonitor.isDependentOnGroup(memberActorGroups);
        if (influencedOfGroup != null && !actorMonitor.checkIfStatusIsValid(generalStatus, influencedOfGroup))
                actorMonitor.sendSignalFrom(influencedOfGroup);//If not, trigger system to refresh

        updateCompoundStatus();
    }

    //React on outside sensor
    private void evaluateTriggerType(TriggerType triggerType, String targetStatusField, Actor activeActor)
    {
        String methodName = "evaluateTriggerType() ";
        switch (triggerType) {
            case NOTHING:
                System.out.println(CLASSNAME + methodName + actorInGameName + " triggered without trigger type");
                return;
            case PERSISTENT:
                evaluateTargetStatus(targetStatusField);
                break;
            case PERSISTENT_TEXT:
                evaluateTargetStatus(targetStatusField);
                activateText(activeActor);
                break;
            case TIMED_TEXT:
                evaluateTargetStatus(targetStatusField);
                playTimedStatus();
                activateText(activeActor);
                break;
            case TIMED:
                evaluateTargetStatus(targetStatusField);
                playTimedStatus();
                break;
            case TEXTBOX:
            case TEXTBOX_ANALYSIS:
            case TEXTBOX_CONDITION:
                activateText(activeActor);
                break;
            case COLLECTABLE:
                collect(activeActor);
                break;
            case SCRIPT:
                actAccordingToScript();
                break;
            case INVENTORY_SHOP:
                WorldViewController.setWorldViewStatus(WorldViewStatus.INVENTORY_SHOP);
                InventoryController.setExchangeInventoryActor(this);
                break;
            case INVENTORY_EXCHANGE:
                WorldViewController.setWorldViewStatus(WorldViewStatus.INVENTORY_EXCHANGE);
                InventoryController.setExchangeInventoryActor(this);
                break;
            case CONDITION:
                break;
        }
    }

    private void actAccordingToScript()
    {

        String methodName = "actAccordingToScript() ";
        script.update(this);
    }

    private void collect(Actor collectingActor)
    {
        String methodName = "collect(String) ";
        CollectableType collectableType = CollectableType.getType(collectable_type);
        Collectible collected = new Collectible(generalStatus, collectableType, actorInGameName, getNumeric_generic_attributes().get("base_value").intValue());
        collected.image = spriteList.get(0).getBaseimage();
        collectingActor.inventory.addItem(collected);

        //check if Management-Attention-Meter is affected for Player
        if (collectingActor.tags.contains(ActorTag.PLAYER) && numeric_generic_attributes.containsKey(SUSPICIOUS_VALUE_ACTOR)) {
            int suspicious_value = numeric_generic_attributes.get(SUSPICIOUS_VALUE_ACTOR).intValue();
            GameVariables.addPlayerMAM_duringDay(suspicious_value);
            GameVariables.addStolenCollectible(collected);
        }

        WorldView.getToRemove().addAll(spriteList);
    }

    public void setSensorStatus(String sensorStatusString)
    {
        String methodName = "setSensorStatus(String) ";
        boolean debug = false;

        if (sensorStatusMap.get(sensorStatusString) == null)
            throw new RuntimeException("Sensor Status not defined: " + sensorStatusString + " at actor " + actorFileName + " known status: " + sensorStatusMap);

        if (debug)
            System.out.println(CLASSNAME + methodName + "set sensor from " + sensorStatus.statusName + " to " + sensorStatusString);
        if (sensorStatusMap.get(sensorStatusString) != sensorStatus)
            this.sensorStatus = sensorStatusMap.get(sensorStatusString);
    }

    private void playTimedStatus()
    {
        String methodName = "playTimedStatus() ";
        List<SpriteData> targetSpriteData = spriteDataMap.get(compoundStatus.toLowerCase());

        if (targetSpriteData == null)
            System.out.println(CLASSNAME + methodName + compoundStatus + " not found in " + spriteDataMap);

        double animationDuration = targetSpriteData.get(0).animationDuration;
        PauseTransition delay = new PauseTransition(Duration.millis(animationDuration * 1000));
        delay.setOnFinished(t ->
        {
            transitionGeneralStatus();
            updateCompoundStatus();
        });

        delay.play();
    }

    public void activateText(Actor activeActor)
    {
        String methodName = "activateText() ";
        if (sensorStatus.onInteraction_TriggerSprite.equals(TriggerType.TEXTBOX_ANALYSIS)) {
            String analyzedGroupName = null;
            List<Actor> analyzedGroup = null;
            try {
                analyzedGroupName = textbox_analysis_group_name;//set in actor file
                analyzedGroup = actorMonitor.getGroupIdToActorGroupMap().get(analyzedGroupName).getSystemMembers();
                WorldView.getTextbox().groupAnalysis(analyzedGroup, this);
            } catch (NullPointerException e) {
                StringBuilder stringBuilder = new StringBuilder();
                if (actorMonitor == null)
                    stringBuilder.append("\nStageMonitor is null");
                if (analyzedGroupName == null)
                    stringBuilder.append("\nAnalyzed group is null: " + memberActorGroups.get(0));
                if (analyzedGroup == null)
                    stringBuilder.append("\nDependent group does not exist or is empty: " + analyzedGroupName);

                throw new NullPointerException(e.getMessage() + stringBuilder.toString());
            }

        }
        else {
            WorldView.getTextbox().startConversation(this);
            if (tags.contains(TURNS_DIRECTION_ONINTERACTION)) {
                numeric_generic_attributes.put("previousDirection", Double.valueOf(direction.getValue()));
                setDirection(activeActor.direction.getOpposite());
            }
        }
        WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
    }

    private void transitionGeneralStatus()
    {
        String methodName = "transitionGeneralStatus() ";
        if (statusTransitions.containsKey(generalStatus)) {
            generalStatus = statusTransitions.get(generalStatus);
        }
        else
            System.out.println(CLASSNAME + methodName + "No status transition found for " + actorFileName + " " + generalStatus + " in " + statusTransitions);
    }

    public void updateCompoundStatus()
    {
        String methodName = "updateCompoundStatus() ";
        String oldCompoundStatus = compoundStatus;
        String newStatusString = generalStatus;


        //GeneralStatus - [DIRECTION] - [MOVING]
        if (!(direction == Direction.UNDEFINED))
            newStatusString = newStatusString + "-" + direction.toString().toLowerCase();
        if (isMoving())
            newStatusString = newStatusString + "-moving";
        compoundStatus = newStatusString;

        if (!(oldCompoundStatus.equals(compoundStatus)))
            changeSprites();

        //If is part of a group
        if (actorMonitor != null)
            actorMonitor.sendSignalFrom(memberActorGroups);
    }

    @Override
    public String toString()
    {
        return actorInGameName;
    }


    public void addSprite(Sprite sprite)
    {
        spriteList.add(sprite);
        sprite.setActor(this);
    }

    public Direction getDirection()
    {
        return direction;
    }

    public void setDirection(Direction direction)
    {
        this.direction = direction;
        updateCompoundStatus();
    }

    public void setVelocity(double x, double y)
    {
        velocityX = x;
        velocityY = y;
        if(x < 0)
            setDirection(WEST);
        else if(x > 0)
            setDirection(EAST);
        if(y < 0)
            setDirection(NORTH);
        else if (y > 0)
            setDirection(SOUTH);

        updateCompoundStatus();
    }

    public boolean isMoving()
    {
        return velocityX != 0 || velocityY != 0;
    }

    public Double getVelocityX()
    {
        return velocityX;
    }

    public Double getVelocityY()
    {
        return velocityY;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public double getSpeed()
    {
        return speed;
    }

    public double getInteractionAreaWidth()
    {
        return interactionAreaWidth;
    }

    public double getInteractionAreaDistance()
    {
        return interactionAreaDistance;
    }

    public double getInteractionAreaOffsetX()
    {
        return interactionAreaOffsetX;
    }

    public double getInteractionAreaOffsetY()
    {
        return interactionAreaOffsetY;
    }

    public void setLastInteraction(Long value)
    {
        String methodName = "setLastInteraction(Long) ";
        lastInteraction = value;
    }

    public Long getLastInteraction()
    {
        return lastInteraction;
    }

    public PersonalityContainer getPersonalityContainer()
    {
        return personalityContainer;
    }

    public String getActorFileName()
    {
        return actorFileName;
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static Set<String> getActorDefinitionKeywords()
    {
        return actorDefinitionKeywords;
    }

    public String getActorInGameName()
    {
        return actorInGameName;
    }

    public List<Sprite> getSpriteList()
    {
        return spriteList;
    }

    public Map<String, List<SpriteData>> getSpriteDataMap()
    {
        return spriteDataMap;
    }

    public String getGeneralStatus()
    {
        return generalStatus;
    }

    public String getCompoundStatus()
    {
        return compoundStatus;
    }

    public String getCollectable_type()
    {
        return collectable_type;
    }

    public List<String> getMemberActorGroups()
    {
        return memberActorGroups;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    public Map<String, SensorStatus> getSensorStatusMap()
    {
        return sensorStatusMap;
    }

    public SensorStatus getSensorStatus()
    {
        return sensorStatus;
    }

    public Set<ActorTag> getTags()
    {
        return tags;
    }

    public Map<String, Double> getNumeric_generic_attributes()
    {
        return numeric_generic_attributes;
    }

    public void setStageMonitor(ActorMonitor actorMonitor)
    {
        this.actorMonitor = actorMonitor;
    }

    public String getActorId()
    {
        return actorId;
    }

    public void setActorId(String actorId)
    {
        this.actorId = actorId;
    }

    public void setActorFileName(String actorFileName)
    {
        this.actorFileName = actorFileName;
    }

    public void setActorInGameName(String actorInGameName)
    {
        this.actorInGameName = actorInGameName;
    }

    public void setVelocityX(double velocityX)
    {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY)
    {
        this.velocityY = velocityY;
    }

    public void setInteractionAreaWidth(double interactionAreaWidth)
    {
        this.interactionAreaWidth = interactionAreaWidth;
    }

    public void setInteractionAreaDistance(double interactionAreaDistance)
    {
        this.interactionAreaDistance = interactionAreaDistance;
    }

    public void setInteractionAreaOffsetX(double interactionAreaOffsetX)
    {
        this.interactionAreaOffsetX = interactionAreaOffsetX;
    }

    public void setInteractionAreaOffsetY(double interactionAreaOffsetY)
    {
        this.interactionAreaOffsetY = interactionAreaOffsetY;
    }

    public void setGeneralStatus(String generalStatus)
    {
        this.generalStatus = generalStatus;
    }

    public void setCompoundStatus(String compoundStatus)
    {
        this.compoundStatus = compoundStatus;
    }

    public void setSensorStatusMap(Map<String, SensorStatus> sensorStatusMap)
    {
        this.sensorStatusMap = sensorStatusMap;
    }

    public void setSensorStatus(SensorStatus sensorStatus)
    {
        this.sensorStatus = sensorStatus;
    }

    public void setCollectable_type(String collectable_type)
    {
        this.collectable_type = collectable_type;
    }

    public void setTextbox_analysis_group_name(String textbox_analysis_group_name)
    {
        this.textbox_analysis_group_name = textbox_analysis_group_name;
    }

    public void setMemberActorGroups(List<String> memberActorGroups)
    {
        this.memberActorGroups = memberActorGroups;
    }

    public void setInventory(Inventory inventory)
    {
        this.inventory = inventory;
    }

    public void setTags(Set<ActorTag> tags)
    {
        this.tags = tags;
    }

    public void setPersonalityContainer(PersonalityContainer personalityContainer)
    {
        this.personalityContainer = personalityContainer;
    }

    public void setNumeric_generic_attributes(Map<String, Double> numeric_generic_attributes)
    {
        this.numeric_generic_attributes = numeric_generic_attributes;
    }


}
