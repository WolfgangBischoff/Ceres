package Core;


import Core.ActorLogic.GrowspaceManager;
import Core.ActorLogic.Script;
import Core.ActorSystem.ActorMonitor;
import Core.Configs.Config;
import Core.Enums.*;
import Core.GameTime.DateTime;
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
import javafx.util.Duration;

import java.util.*;

import static Core.Configs.Config.*;
import static Core.Enums.ActorTag.*;
import static Core.Enums.Direction.*;
import static Core.Enums.TriggerType.*;
import static Core.WorldView.WorldView.addToLayer;
import static Core.WorldView.WorldView.isSpriteLoaded;
import static Core.WorldView.WorldViewStatus.WORLD;

public class Actor
{
    private static final String CLASSNAME = "Actor/";
    private static final Set<String> actorDefinitionKeywords = new HashSet<>();
    final Map<String, String> statusTransitions = new HashMap<>();
    final Map<String, List<SpriteData>> spriteDataMap = new HashMap<>();
    final List<Sprite> spriteList = new ArrayList<>();
    public Set<ActorTag> tags = new HashSet<>();
    //General
    String actorFileName;
    String actorId;
    String actorInGameName;
    //Sprite
    String generalStatus;
    String compoundStatus = "default";
    //Sensor
    Map<String, SensorStatus> sensorStatusMap = new HashMap<>();
    SensorStatus sensorStatus;
    List<ActorCondition> conditions = new ArrayList<>();
    String textbox_analysis_group_name = "none";
    ActorMonitor actorMonitor;
    List<String> memberActorGroups = new ArrayList<>();
    Inventory inventory;
    PersonalityContainer personalityContainer;
    Map<String, Double> genericDoubleAttributes = new HashMap<>();
    Map<String, String> genericStringAttributes = new HashMap<>();
    Map<String, Actor> genericActorAttributes = new HashMap<>();
    Script script;
    private Set<CollectableType> collectableTags = new HashSet<>();
    private Map<String, DateTime> genericDateTimeAttributes = new HashMap<>();
    private Direction direction;
    private double currentVelocityX;
    private double currentVelocityY;
    private double velocity = 50;
    private double interactionAreaWidth = 8;
    private double interactionAreaDistance = 30;
    private double interactionAreaOffsetX = 0;
    private double interactionAreaOffsetY = 0;
    private Long lastInteraction = 0L;
    private Long lastAutomaticInteraction = 0L;

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
            actorDefinitionKeywords.add(COLLECTIBLE_BASE_VALUE);
            actorDefinitionKeywords.add(COLLECTIBLE_TAGS);
            actorDefinitionKeywords.add(CONTAINS_COLLECTIBLE_ACTOR);
            actorDefinitionKeywords.add(KEYWORD_sensorStatus);
            actorDefinitionKeywords.add(TAGS_ACTOR);
            actorDefinitionKeywords.add(CONDITION_ACTOR);
            actorDefinitionKeywords.add(SUSPICIOUS_VALUE_ACTOR);
            actorDefinitionKeywords.add(PERSONALITY_ACTOR);
            actorDefinitionKeywords.add(SCRIPT_ACTOR);
        }
        actordata = Utilities.readAllLineFromTxt(actorFileName + CSV_POSTFIX);
        for (String[] linedata : actordata)
        {
            if (checkForKeywords(linedata))
                continue;

            //Collect Actor Sprite Data
            try
            {
                SpriteData data = SpriteData.tileDefinition(linedata);
                data.animationDuration = Double.parseDouble(linedata[SpriteData.getAnimationDurationIdx()]);
                data.velocity = Integer.parseInt(linedata[SpriteData.getVelocityIdx()]);
                data.dialogueID = linedata[SpriteData.getDialogueIDIdx()];
                data.animationEnds = Boolean.parseBoolean(linedata[SpriteData.getAnimationEndsIdx()]);
                String statusName = linedata[0].toLowerCase();
                if (!spriteDataMap.containsKey(statusName))
                    spriteDataMap.put(statusName, new ArrayList<>());
                spriteDataMap.get(statusName).add(data);
            }
            catch (IndexOutOfBoundsException e)
            {
                throw new IndexOutOfBoundsException(e.getMessage() + "\n in Actorfile: " + actorFileName);
            }
        }

        sensorStatus = sensorStatusMap.get(initSensorStatus);

        if (sensorStatus == null)
            System.out.println(CLASSNAME + methodName + actorInGameName + " no sensor found: " + initSensorStatus);
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static Set<String> getActorDefinitionKeywords()
    {
        return actorDefinitionKeywords;
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

        switch (keyword)
        {
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
            case COLLECTIBLE_BASE_VALUE:
                getGenericDoubleAttributes().put("base_value", Double.parseDouble(linedata[1]));
                break;
            case COLLECTIBLE_TAGS:
                collectableTags.addAll(readCollectibleTagData(linedata));
                break;
            case CONTAINS_COLLECTIBLE_ACTOR:
                Collectible collectible = Collectible.createCollectible(linedata[1], linedata[2]);
                int number = Integer.parseInt(linedata[3]);
                inventory.addCollectibleStackNextSlot(new CollectibleStack(collectible, number));
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
                genericDoubleAttributes.put(linedata[0], Double.parseDouble(linedata[1]));
                break;
            case SCRIPT_ACTOR:
                setScript(linedata[1]);
                break;
            default:
                throw new RuntimeException("Keyword unknown: " + keyword);
        }

        return true;
    }

    private Script readScript(String path)
    {
        return new Script(Utilities.readXMLFile(path));
    }

    public void setScript(String path)
    {
        script = readScript(path);
    }

    private PersonalityContainer readPersonality(String[] linedata)
    {
        String methodName = "readPersonality() ";
        PersonalityContainer readContainer = new PersonalityContainer(this);
        int initCooperationValue = Integer.parseInt(linedata[1]);
        readContainer.increaseCooperation(initCooperationValue);
        int firstTraitIdx = 2;
        for (int i = firstTraitIdx; i < linedata.length; i++)
        {
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
        int spriteStatusConditionIdx = 1;
        int sensorStatusConditionIdx = 2;
        int actorConditionTypeIdx = 3;
        int trueSpriteStatusIdx = 4;
        int trueSensorStatusIdx = 5;
        int trueDialogueIdIdx = 6;
        int trueDialogueFileIdx = 7;
        int falseSpriteStatusIdx = 8;
        int falseSensorStatusIdx = 9;
        int falseDialogueIdIdx = 10;
        int falseDialogueFileIdx = 11;
        int paramsIdx = 12;
        ActorCondition actorCondition = new ActorCondition();
        actorCondition.spriteStatusCondition = linedata[spriteStatusConditionIdx];
        actorCondition.sensorStatusCondition = linedata[sensorStatusConditionIdx];
        actorCondition.actorConditionType = ActorConditionType.getConditionFromValue(linedata[actorConditionTypeIdx]);
        actorCondition.trueSpriteStatus = linedata[trueSpriteStatusIdx];
        actorCondition.trueSensorStatus = linedata[trueSensorStatusIdx];
        actorCondition.trueDialogueId = linedata[trueDialogueIdIdx];
        actorCondition.trueDialogueFile = linedata[trueDialogueFileIdx];
        actorCondition.falseSpriteStatus = linedata[falseSpriteStatusIdx];
        actorCondition.falseSensorStatus = linedata[falseSensorStatusIdx];
        actorCondition.falseDialogueId = linedata[falseDialogueIdIdx];
        actorCondition.falseDialogueFile = linedata[falseDialogueFileIdx];
        actorCondition.params.addAll(Arrays.asList(linedata).subList(paramsIdx, linedata.length));
        return actorCondition;
    }

    private Set<ActorTag> readTagData(String[] linedata)
    {
        Set<ActorTag> tagDataSet = new HashSet<>();
        int startIdxTags = 1;
        for (int i = startIdxTags; i < linedata.length; i++)
        {
            tagDataSet.add(ActorTag.getType(linedata[i]));
        }
        return tagDataSet;
    }

    private Set<CollectableType> readCollectibleTagData(String[] linedata)
    {
        Set<CollectableType> tagDataSet = new HashSet<>();
        int startIdxTags = 1;
        for (int i = startIdxTags; i < linedata.length; i++)
        {
            tagDataSet.add(CollectableType.getType(linedata[i]));
        }
        return tagDataSet;
    }

    private SensorStatus readSensorData(String[] lineData) throws ArrayIndexOutOfBoundsException
    {
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
        try
        {
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
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new ArrayIndexOutOfBoundsException(actorInGameName + "\n" + e.getMessage());
        }
        return sensorStatus;
    }

    public void onUpdate(Long currentNanoTime)
    {
        //No lastInteraction time update, just resets if not used. like a automatic door
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastAutomaticInteraction) / 1000000000.0;
        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS)
        {
            //Sprite
            if (sensorStatus.onUpdate_TriggerSprite != TriggerType.NOTHING && !sensorStatus.onUpdateToStatusSprite.equals(generalStatus))
                evaluateTriggerTypeSprite(sensorStatus.onUpdate_TriggerSprite, sensorStatus.onUpdateToStatusSprite, null);

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
        for (ActorCondition condition : conditions)
        {
            if //check pre-condition
            (
                    (generalStatus.equalsIgnoreCase(condition.spriteStatusCondition) || condition.spriteStatusCondition.equals("*"))
                            &&
                            (sensorStatus.statusName.equals(condition.sensorStatusCondition) || condition.sensorStatusCondition.equals("*"))
            )
            {
                if (condition.evaluate(activeActor, this))
                //condition met
                {
                    if (!condition.trueSpriteStatus.equals("*"))
                    {
                        generalStatus = condition.trueSpriteStatus;
                        updateCompoundStatus();
                    }
                    if (!condition.trueSensorStatus.equals("*"))
                        setSensorStatus(condition.trueSensorStatus);
                    if (!condition.trueDialogueFile.equals("*"))
                        setDialogueFile(condition.trueDialogueFile);
                    if (!condition.trueDialogueId.equals("*"))
                        setDialogueId(condition.trueDialogueId);

                    if (debug)
                        System.out.println(CLASSNAME + methodName + " condition met " + generalStatus + " " + sensorStatus.statusName);
                }
                else
                //condition not met
                {
                    if (!condition.falseSpriteStatus.equals("*"))
                    {
                        generalStatus = condition.falseSpriteStatus;
                        updateCompoundStatus();
                    }
                    if (!condition.falseSensorStatus.equals("*"))
                        setSensorStatus(condition.falseSensorStatus);
                    if (!condition.falseDialogueFile.equals("*"))
                        setDialogueFile(condition.falseDialogueFile);
                    if (!condition.falseDialogueId.equals("*"))
                        setDialogueId(condition.falseDialogueId);

                    if (debug)
                        System.out.println(CLASSNAME + methodName + "Not met " + generalStatus + " " + sensorStatus.statusName);
                }
            }
        }
    }

    public void onInteraction(Sprite activeSprite, Long currentNanoTime)
    {
        double elapsedTimeSinceLastInteraction = (currentNanoTime - lastInteraction) / 1000000000.0;

        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_INTERACTIONS)
        {
            if (sensorStatus.getOnInteraction_TriggerSensor() == CONDITION
                    || sensorStatus.getOnInteraction_TriggerSprite() == CONDITION
                    || sensorStatus.getOnInteraction_TriggerSensor() == TEXTBOX_CONDITION
                    || sensorStatus.getOnInteraction_TriggerSprite() == TEXTBOX_CONDITION)
                updateStatusFromConditions(activeSprite.getActor());

            evaluateTriggerTypeSprite(sensorStatus.onInteraction_TriggerSprite, sensorStatus.onInteractionToStatusSprite, activeSprite.getActor());
            evaluateTriggerTypeSensor(sensorStatus.onInteraction_TriggerSensor, sensorStatus.onInteraction_StatusSensor);
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
        if (sensorStatus.onMonitorSignal_TriggerSprite != null)
            evaluateTriggerTypeSprite(sensorStatus.onMonitorSignal_TriggerSprite, newCompoundStatus, null);
        if (newDialogueId != null)
            setDialogueId(newDialogueId);

    }

    public void onTextboxSignal(String newCompoundStatus)
    {
        evaluateTriggerTypeSprite(sensorStatus.onTextBoxSignal_SpriteTrigger, newCompoundStatus, null);
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
        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS && actorRelevant)
        {
            if (debug)
                System.out.println(CLASSNAME + methodName + actorFileName + " onIntersection " + detectedSprite.getName());

            //Sprite Status
            if (sensorStatus.onIntersection_TriggerSprite != TriggerType.NOTHING)
            {
                evaluateTriggerTypeSprite(sensorStatus.onIntersection_TriggerSprite, sensorStatus.onIntersectionToStatusSprite, detectedSprite.getActor());
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

        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS)
        {
            evaluateTriggerTypeSprite(sensorStatus.onInRange_TriggerSprite, sensorStatus.onInRangeToStatusSprite, detectedSprite.getActor());
            lastAutomaticInteraction = currentNanoTime;
        }

        if (elapsedTimeSinceLastInteraction > TIME_BETWEEN_AUTOMATIC_INTERACTIONS && sensorStatus.onInRange_TriggerSensor != NOTHING)
        {
            evaluateTriggerTypeSensor(sensorStatus.onInRange_TriggerSensor, sensorStatus.onInRangeToStatusSensorStatus);
            //if (sensorStatus.onInRange_TriggerSensor == PERSISTENT)//TODO should be like evaluateTriggerType for sensorStaus
            //    setSensorStatus(sensorStatusMap.get(sensorStatus.onInRangeToStatusSensorStatus));
            lastAutomaticInteraction = currentNanoTime;
        }
    }

    private void evaluateTriggerTypeSensor(TriggerType triggerType, String triggerSensorStatus)
    {
        switch (triggerType)
        {
            case PERSISTENT:
                setSensorStatus(triggerSensorStatus);
                break;
        }
    }

    private void changeSprites()
    {
        List<SpriteData> targetSpriteData = spriteDataMap.get(compoundStatus.toLowerCase());

        if (targetSpriteData == null)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not find: ").append(compoundStatus.toLowerCase()).append(" in \n");
            for (Map.Entry<String, List<SpriteData>> entry : spriteDataMap.entrySet())
                stringBuilder.append("\t").append(entry.getKey()).append("\n");
            System.out.println(stringBuilder.toString());
            applySpriteData(List.of(new SpriteData("error", "img/notfound_64_64", false,
                    0.0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, "none", "none", "none")));
        }
        else
            //For all Sprites of the actor onUpdate to new Status
            applySpriteData(targetSpriteData);

        if (spriteList.isEmpty())//Before Actor is initialized
            return;

    }

    public void applySpriteData(List<SpriteData> targetSpriteData)
    {
        try
        {
            for (int i = 0; i < spriteList.size(); i++)
            {
                SpriteData ts = targetSpriteData.get(i);
                Sprite toChange = spriteList.get(i);
                toChange.setImage(ts.spriteName, ts.fps, ts.totalFrames, ts.cols, ts.rows, ts.frameWidth, ts.frameHeight);
                toChange.setBlocker(ts.blocking);
                toChange.setLightningSpriteName(ts.lightningSprite);
                toChange.setAnimationEnds(ts.animationEnds);
                toChange.setLayer(ts.renderLayer);
                if (isSpriteLoaded(toChange))
                    addToLayer(toChange);//Change layer if sprite in current stage, not on other map (global system) but sprite change triggered by StageMonitor
            }
        }
        catch (NullPointerException e)
        {
            targetSpriteData.forEach(s -> System.out.println("Data:" + s));
        }
    }

    private void applySpriteStatus(String targetStatusField)
    {
        String targetStatusChecked = "";
        if (targetStatusField.equalsIgnoreCase(Config.KEYWORD_transition))
            targetStatusChecked = lookupSpriteStatusTransition();
        else
        {
            targetStatusChecked = targetStatusField.toLowerCase();
        }

        //Correct new status and dialogue if system is dependent
        String influencedOfGroup = actorMonitor.isDependentOnGroup(memberActorGroups);
        if (influencedOfGroup != null)
        {
            targetStatusChecked = actorMonitor.correctSpriteStatusFromInfluencingGroup(targetStatusChecked, influencedOfGroup);
            setDialogueFile(spriteDataMap.get(createCompoundStatus(targetStatusChecked)).get(0).dialogieFile);
            setDialogueId(spriteDataMap.get(createCompoundStatus(targetStatusChecked)).get(0).dialogueID);
        }

        setSpriteStatus(targetStatusChecked);
    }

    //React on outside sensor
    private void evaluateTriggerTypeSprite(TriggerType triggerType, String targetStatusField, Actor activeActor)
    {
        String methodName = "evaluateTriggerType() ";
        switch (triggerType)
        {
            case NOTHING:
                System.out.println(CLASSNAME + methodName + actorInGameName + " triggered without trigger type");
                return;
            case PERSISTENT:
                applySpriteStatus(targetStatusField);
                break;
            case PERSISTENT_TEXT:
                applySpriteStatus(targetStatusField);
                activateText(activeActor);
                break;
            case PERSISTENT_HARVEST:
                if (GrowspaceManager.harvest(this))
                    applySpriteStatus(targetStatusField);
                break;
            case TIMED_TEXT:
                applySpriteStatus(targetStatusField);
                playTimedStatus();
                activateText(activeActor);
                break;
            case TIMED:
                applySpriteStatus(targetStatusField);
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
                actAccordingToScript(GameWindow.getCurrentNanoRenderTimeGameWindow());
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
            case INCUBATOR:
                WorldViewController.setWorldViewStatus(WorldViewStatus.INCUBATOR);
                InventoryController.setExchangeInventoryActor(this);
                break;
            case LEVELCHANGE:
                String filename_level = sensorStatus.onInteractionToStatusSprite;
                String spawnId = sensorStatus.onInteraction_StatusSensor;
                WorldView.getSingleton().changeStage(filename_level, spawnId, false);
                break;
        }
    }

    public void interactWithMenuItem(CollectibleStack from)
    {
        if (tags.contains(GROWPLACE))
        {
            GrowspaceManager.handleCollectible(from, this);
        }
    }

    public void actAccordingToScript(Long currentNanoTime)
    {
        if (WorldViewController.getWorldViewStatus() == WORLD)
        {
            script.update(this);
        }
        else
            setVelocity(0, 0);
    }


    private void collect(Actor collectingActor)
    {
        Collectible collected = Collectible.createCollectible(actorFileName, generalStatus);
        collected.image = spriteList.get(0).getBaseimage();
        double amountCollected = genericDoubleAttributes.getOrDefault(GENERIC_STACK_AMOUNT, 1D);
        boolean wasCollected = collectingActor.inventory.addCollectibleStackNextSlot(new CollectibleStack(collected, (int) amountCollected));

        if (wasCollected)
        {
            //check if Management-Attention-Meter is affected for Player
            if (collectingActor.tags.contains(ActorTag.PLAYER) && genericDoubleAttributes.containsKey(SUSPICIOUS_VALUE_ACTOR))
            {
                int suspicious_value = genericDoubleAttributes.get(SUSPICIOUS_VALUE_ACTOR).intValue();
                GameVariables.addPlayerMAM_duringDay(suspicious_value);
                GameVariables.addStolenCollectible(collected);
            }
            WorldView.getToRemove().addAll(spriteList);
        }
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
            setSpriteStatus(lookupSpriteStatusTransition());
        });

        delay.play();
    }

    public void activateText(Actor activeActor)
    {
        if (sensorStatus.onInteraction_TriggerSprite.equals(TriggerType.TEXTBOX_ANALYSIS))
        {
            String analyzedGroupName = null;
            List<Actor> analyzedGroup = null;
            try
            {
                analyzedGroupName = textbox_analysis_group_name;//set in actor file
                analyzedGroup = actorMonitor.getGroupIdToActorGroupMap().get(analyzedGroupName).getSystemMembers();
                WorldView.getTextbox().groupAnalysis(analyzedGroup, this);
            }
            catch (NullPointerException e)
            {
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
        else
        {
            WorldView.getTextbox().startConversation(this);
            if (tags.contains(TURNS_DIRECTION_ONINTERACTION))
            {
                genericDoubleAttributes.put("previousDirection", Double.valueOf(direction.getValue()));
                setDirection(activeActor.direction.getOpposite());
            }
        }
        WorldViewController.setWorldViewStatus(WorldViewStatus.TEXTBOX);
    }

    public boolean hasTag(ActorTag tag)
    {
        return tags.contains(tag);
    }

    private String lookupSpriteStatusTransition()
    {
        if (statusTransitions.containsKey(generalStatus))
        {
            return statusTransitions.get(generalStatus);
        }
        else
        {
            System.out.println(CLASSNAME + "No status transition found for " + actorFileName + " " + generalStatus + " in " + statusTransitions);
            return generalStatus;
        }
    }

    public void updateCompoundStatus()
    {
        String oldCompoundStatus = compoundStatus;
        String spriteStatus = generalStatus;
        compoundStatus = createCompoundStatus(spriteStatus);

        if (!(oldCompoundStatus.equals(compoundStatus)))
            changeSprites();

        //If is part of a group
        if (actorMonitor != null)
            actorMonitor.sendSignalFrom(memberActorGroups);
    }

    private String createCompoundStatus(String spriteStatus)
    {
        //GeneralStatus - [DIRECTION] - [MOVING]
        if (!(direction == Direction.UNDEFINED))
            spriteStatus = spriteStatus + "-" + direction.toString().toLowerCase();
        if (isMoving())
            spriteStatus = spriteStatus + "-moving";
        return spriteStatus;
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
        currentVelocityX = x;
        currentVelocityY = y;

        if (y < 0)
            setDirection(NORTH);
        else if (y > 0)
            setDirection(SOUTH);
        else if (x < 0)
            setDirection(WEST);
        else if (x > 0)
            setDirection(EAST);


        updateCompoundStatus();
    }

    public boolean isActiveActor()
    {
        return
                (sensorStatus.onInRange_TriggerSensor != NOTHING
                        || sensorStatus.onInRange_TriggerSprite != NOTHING
                        || sensorStatus.onIntersection_TriggerSensor != NOTHING
                        || sensorStatus.onIntersection_TriggerSprite != NOTHING
                        || sensorStatus.onUpdate_TriggerSensor != NOTHING
                        || sensorStatus.onUpdate_TriggerSprite != NOTHING
                        || getSpriteList().get(0).getName().equalsIgnoreCase("player"));
    }

    public boolean isMoving()
    {
        return currentVelocityX != 0 || currentVelocityY != 0;
    }

    public Double getCurrentVelocityX()
    {
        return currentVelocityX;
    }

    public void setCurrentVelocityX(double currentVelocityX)
    {
        this.currentVelocityX = currentVelocityX;
    }

    public Double getCurrentVelocityY()
    {
        return currentVelocityY;
    }

    public void setCurrentVelocityY(double currentVelocityY)
    {
        this.currentVelocityY = currentVelocityY;
    }

    public double getVelocity()
    {
        return velocity;
    }

    public void setVelocity(double velocity)
    {
        this.velocity = velocity;
    }

    public double getInteractionAreaWidth()
    {
        return interactionAreaWidth;
    }

    public void setInteractionAreaWidth(double interactionAreaWidth)
    {
        this.interactionAreaWidth = interactionAreaWidth;
    }

    public double getInteractionAreaDistance()
    {
        return interactionAreaDistance;
    }

    public void setInteractionAreaDistance(double interactionAreaDistance)
    {
        this.interactionAreaDistance = interactionAreaDistance;
    }

    public double getInteractionAreaOffsetX()
    {
        return interactionAreaOffsetX;
    }

    public void setInteractionAreaOffsetX(double interactionAreaOffsetX)
    {
        this.interactionAreaOffsetX = interactionAreaOffsetX;
    }

    public double getInteractionAreaOffsetY()
    {
        return interactionAreaOffsetY;
    }

    public void setInteractionAreaOffsetY(double interactionAreaOffsetY)
    {
        this.interactionAreaOffsetY = interactionAreaOffsetY;
    }

    public Long getLastInteraction()
    {
        return lastInteraction;
    }

    public void setLastInteraction(Long value)
    {
        String methodName = "setLastInteraction(Long) ";
        lastInteraction = value;
    }

    public PersonalityContainer getPersonalityContainer()
    {
        return personalityContainer;
    }

    public void setPersonalityContainer(PersonalityContainer personalityContainer)
    {
        this.personalityContainer = personalityContainer;
    }

    public String getActorFileName()
    {
        return actorFileName;
    }

    public void setActorFileName(String actorFileName)
    {
        this.actorFileName = actorFileName;
    }

    public String getActorInGameName()
    {
        return actorInGameName;
    }

    public void setActorInGameName(String actorInGameName)
    {
        this.actorInGameName = actorInGameName;
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

    public void setSpriteStatus(String generalStatus)
    {
        this.generalStatus = generalStatus;
        updateCompoundStatus();
    }

    public String getCompoundStatus()
    {
        return compoundStatus;
    }

    public void setCompoundStatus(String compoundStatus)
    {
        this.compoundStatus = compoundStatus;
    }

    public Set<CollectableType> getCollectableTags()
    {
        return collectableTags;
    }

    public List<String> getMemberActorGroups()
    {
        return memberActorGroups;
    }

    public void setMemberActorGroups(List<String> memberActorGroups)
    {
        this.memberActorGroups = memberActorGroups;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    public void setInventory(Inventory inventory)
    {
        this.inventory = inventory;
    }

    public Map<String, SensorStatus> getSensorStatusMap()
    {
        return sensorStatusMap;
    }

    public Map<String, DateTime> getGenericDateTimeAttributes()
    {
        return genericDateTimeAttributes;
    }

    public Map<String, Actor> getGenericActorAttributes()
    {
        return genericActorAttributes;
    }

    public SensorStatus getSensorStatus()
    {
        return sensorStatus;
    }

    public void setGenericActorAttribute(String id, Actor actor)
    {
        genericActorAttributes.put(id, actor);
    }

    public void setSensorStatus(String sensorStatusString)
    {
        if (sensorStatusMap.get(sensorStatusString) == null)
            throw new RuntimeException("Sensor Status not defined: " + sensorStatusString + " at actor " + actorFileName + " known status: " + sensorStatusMap);

        if (sensorStatusMap.get(sensorStatusString) != sensorStatus)
            this.sensorStatus = sensorStatusMap.get(sensorStatusString);
    }

    public void setSensorStatus(SensorStatus sensorStatus)
    {
        this.sensorStatus = sensorStatus;
    }

    public Set<ActorTag> getTags()
    {
        return tags;
    }

    public void setTags(Set<ActorTag> tags)
    {
        this.tags = tags;
    }

    public Map<String, Double> getGenericDoubleAttributes()
    {
        return genericDoubleAttributes;
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

    public DateTime getGenericDateTimeAttribute(String id)
    {
        return genericDateTimeAttributes.get(id);
    }

    public void setGenericDateTimeAttribute(String id, DateTime datetime)
    {
        genericDateTimeAttributes.put(id, datetime);
    }

    public void removeGenericDateTimeAttribute(String id)
    {
        genericDateTimeAttributes.remove(id);
    }

    public void setGenericStringAttribute(String id, String string)
    {
        genericStringAttributes.put(id, string);
    }

    public void removeGenericStringAttribute(String id)
    {
        genericStringAttributes.remove(id);
    }

    public Map<String, String> getGenericStringAttributes()
    {
        return genericStringAttributes;
    }
}
