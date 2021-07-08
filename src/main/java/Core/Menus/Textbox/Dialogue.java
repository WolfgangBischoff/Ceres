package Core.Menus.Textbox;

import Core.*;
import Core.Enums.CollectableType;
import Core.Enums.Knowledge;
import Core.Menus.AchievmentLog.CentralMessageOverlay;
import Core.Menus.CoinGame.CoinGame;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class Dialogue
{
    private static final String CLASSNAME = "Dialogue/";
    private final Actor actorOfDialogue;
    String type;
    String nextDialogue;
    List<String> messages = new ArrayList<>();
    List<Option> options = new ArrayList<>();
    private String spriteStatus;
    private String sensorStatus;

    public Dialogue(Actor actorOfDialogue, Element currentDialogueXML)
    {
        type = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TYPE);
        this.actorOfDialogue = actorOfDialogue;
        NodeList xmlLines = currentDialogueXML.getElementsByTagName(LINE_TAG);
        setSpriteStatus(currentDialogueXML.getAttribute(ACTOR_STATUS_TAG));
        setSensorStatus(currentDialogueXML.getAttribute(SENSOR_STATUS_TAG));
        switch (type)
        {

            case TEXTBOX_ATTRIBUTE_VALUE_BOOLEAN:
                break;
            case DIALOGUE_TYPE_DECISION:
                readOptions(currentDialogueXML);
            default:
                for (int messageIdx = 0; messageIdx < xmlLines.getLength(); messageIdx++) //add lines
                {
                    String message = Utilities.removeAllBlanksExceptOne(xmlLines.item(messageIdx).getTextContent());
                    messages.add(message);//Without formatting the message
                }

                if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_GET_MONEY)))
                {
                    int amount = Integer.parseInt(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_GET_MONEY));
                    GameVariables.addPlayerMoney(amount);
                    CentralMessageOverlay.showMsg("received " + amount + " GSC!");
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_COIN_GAME))
                {
                    String discussionGameName = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_COIN_GAME);
                    String successNextMsg = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SUCCESS);
                    String defeatNextMsg = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DEFEAT);
                    OptionConditionData gameData = new OptionConditionData();
                    gameData.conditionType = TEXTBOX_ATTRIBUTE_COIN_GAME;
                    gameData.messageOutcomes.put(TEXTBOX_ATTRIBUTE_SUCCESS, successNextMsg);
                    gameData.messageOutcomes.put(TEXTBOX_ATTRIBUTE_DEFEAT, defeatNextMsg);
                    addOption(new Option(TEXTBOX_ATTRIBUTE_COIN_GAME, "none", gameData));
                    //addOption(TEXTBOX_ATTRIBUTE_SUCCESS, successNextMsg);
                    //addOption(TEXTBOX_ATTRIBUTE_DEFEAT, defeatNextMsg);
                    WorldView.setDiscussionGame(new CoinGame(discussionGameName, actorOfDialogue));
                    WorldViewController.setWorldViewStatus(WorldViewStatus.COIN_GAME);
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_SET))
                {
                    String varname = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME);
                    String val = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SET);
                    GameVariables.setGenericVariable(varname, val);
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_TIME_CHANGE))
                {
                    GameVariables.getClock().addTime(Integer.parseInt(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TIME_CHANGE)));
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_DAY_CHANGE))
                {
                    WorldViewController.setWorldViewStatus(WorldViewStatus.DAY_SUMMARY);
                    DaySummaryScreenController.newDay();
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_FADE))
                {
                    WorldView.getSingleton().setFadedOut(Boolean.parseBoolean(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_FADE)));
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_LEVEL_CHANGE))
                {
                    //TODO BUG, stage wird auch gespeichtert am schon inkrementierten tag, deshalb wird nicht vom file geladen. Vorher wurde nur geladen
                    //TODO Konzeptionelles Problem mit Fileload nur bei neuem Tag => Soll nur Spawned items nicht sändig laden
                    WorldView.getSingleton().changeStage(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_LEVEL_CHANGE), currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SPAWN_ID), currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_INVALID_STAGES));
                    WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
                }

                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_BUMP))
                {
                    WorldView.getSingleton().activateBump();
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_ITEM_ACTOR))
                {
                    Collectible collectible = Collectible.createCollectible(
                            currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEM_ACTOR)
                            , currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEM_STATUS));
                    if (WorldView.getPlayer().getActor().getInventory().addCollectibleStackNextSlot(new CollectibleStack(collectible)))
                        CentralMessageOverlay.showMsg("New " + collectible.getIngameName() + "!");
                    else
                        System.out.println(CLASSNAME + "TODO Item could not be added to Inventory");
                }
                if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_KNOWLEDGE))
                {
                    GameVariables.addPlayerKnowledge(Knowledge.of(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_KNOWLEDGE)));
                }
                if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_DIALOGUE_FILE)))
                {
                    actorOfDialogue.setDialogueFile(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_FILE));
                }
                if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_DIALOGUE_ID)))
                {
                    actorOfDialogue.setDialogueId(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_ID));
                }

                if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_SET_WORLD_LIGHT)))
                {
                    if (currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SET_WORLD_LIGHT).equals("night"))
                        WorldView.getSingleton().setShadowColor(COLOR_EMERGENCY_LIGHT);
                    else
                        WorldView.getSingleton().setShadowColor(null);
                }
                break;
        }
        nextDialogue = checkForNextDialogues(currentDialogueXML);
    }

    private void readOptions(Element currentDialogueXML)
    {
        NodeList optionData = currentDialogueXML.getElementsByTagName(OPTION_TAG);
        for (int optionsIdx = 0; optionsIdx < optionData.getLength(); optionsIdx++)
        {
            Element optionNode = (Element) optionData.item(optionsIdx);
            String nextDialogue = null;
            String optionText;
            OptionConditionData conditionData = readOptionConditions(optionNode);

            if (optionNode.hasAttribute(NEXT_DIALOGUE_TAG))
                nextDialogue = optionNode.getAttribute(NEXT_DIALOGUE_TAG);
            optionText = Utilities.removeAllBlanksExceptOne(optionNode.getTextContent());

            boolean isOptionVisible = !optionNode.hasAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF) ||
                    optionNode.getAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF)
                            .equals(getVariableCondition(optionNode.getAttribute(TEXTBOX_ATTRIBUTE_TYPE), optionNode.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME)));

            Option option = new Option(optionText, nextDialogue, conditionData);
            if (isOptionVisible || DEBUG_ALL_TEXT_OPTIONS_VISIBLE)
                addOption(option);
        }
    }

    private OptionConditionData readOptionConditions(Element currentDialogueXML)
    {
        OptionConditionData optionConditionData = new OptionConditionData();
        if (currentDialogueXML.hasAttribute(TEXTBOX_OPTION_ATTRIBUTE_CONSUME_ITEMS))
        {
            optionConditionData.conditionType = TEXTBOX_OPTION_ATTRIBUTE_CONSUME_ITEMS;
            optionConditionData.collectableTechnicalName = currentDialogueXML.getAttribute(TEXTBOX_OPTION_ATTRIBUTE_CONSUME_ITEMS);
            optionConditionData.amount = Integer.parseInt(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_AMOUNT));
            optionConditionData.collectableType = CollectableType.getType(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEMTYPE));
            optionConditionData.messageOutcomes.put(TEXTBOX_ATTRIBUTE_SUCCESS, currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SUCCESS));
            optionConditionData.messageOutcomes.put(TEXTBOX_ATTRIBUTE_DEFEAT, currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DEFEAT));
        }
        return optionConditionData;
    }

    private String getVariableCondition(String type, String varName)
    {
        String methodName = "checkVariableCondition() ";
        String eval = null;
        if (type.equals("boolean"))
        {
            eval = GameVariables.getGenericVariableManager().getValue(varName);
            if (eval == null)
                System.out.println(CLASSNAME + methodName + "variable not set: " + varName);
        }
        else if (type.equals("player"))
        {
            if (varName.equals("spritestatus"))
                return WorldView.getPlayer().getActor().getGeneralStatus();
        }

        return eval;
    }

    private String checkForNextDialogues(Element currentDialogue)
    {
        NodeList nextDialogueIdList = currentDialogue.getElementsByTagName(NEXT_DIALOGUE_TAG);
        if (nextDialogueIdList.getLength() > 0)
        {
            return nextDialogueIdList.item(0).getTextContent();
        }
        else if (currentDialogue.hasAttribute(NEXT_DIALOGUE_TAG))
        {
            return currentDialogue.getAttribute(NEXT_DIALOGUE_TAG);
        }
        else
        {
            return null;
        }
    }

    public void addOption(Option option)
    {
        options.add(option);
    }

    public Option getOption(String optionMsg)
    {
        for (Option option : options)
            if (option.optionMessage.equals(optionMsg))
                return option;
        return null;
    }

    public List<String> getOptionMessages()
    {
        List<String> optionMessages = new ArrayList<>();
        for (Option option : options)
        {
            optionMessages.add(option.optionMessage);
        }
        return optionMessages;
    }

    public String getSpriteStatus()
    {
        return spriteStatus;
    }

    public void setSpriteStatus(String spriteStatus)
    {
        if (spriteStatus.trim().isEmpty())
            this.spriteStatus = null;
        else
            this.spriteStatus = spriteStatus;
    }

    public String getSensorStatus()
    {
        return sensorStatus;
    }

    public void setSensorStatus(String sensorStatus)
    {
        if (sensorStatus.trim().isEmpty())
            this.sensorStatus = null;
        else
            this.sensorStatus = sensorStatus;
    }

    public List<String> getMessages()
    {
        return messages;
    }

    @Override
    public String toString()
    {
        return "Dialogue{" +
                "type='" + type + '\'' +
                ", nextDialogue='" + nextDialogue + '\'' +
                ", spriteStatus='" + spriteStatus + '\'' +
                ", sensorStatus='" + sensorStatus + '\'' +
                ", messages=" + messages +
                ", options=" + options +
                '}';
    }

}
