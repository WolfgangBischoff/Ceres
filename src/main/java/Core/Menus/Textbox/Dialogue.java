package Core.Menus.Textbox;

import Core.GameVariables;
import Core.WorldView.WorldView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;
import static Core.Configs.Config.SENSOR_STATUS_TAG;

public class Dialogue
{
    private static final String CLASSNAME = "Dialogue/";
    String type;
    String nextDialogue;
    private String spriteStatus;
    private String sensorStatus;
    List<String> messages = new ArrayList<>();
    List<Option> options = new ArrayList<>();

    public Dialogue(Element currentDialogueXML)
    {
        type = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TYPE);
        NodeList xmlLines = currentDialogueXML.getElementsByTagName(LINE_TAG);
        setSpriteStatus(currentDialogueXML.getAttribute(ACTOR_STATUS_TAG));
        setSensorStatus(currentDialogueXML.getAttribute(SENSOR_STATUS_TAG));
        switch (type) {
            case decision_TYPE_ATTRIBUTE:
                //For all options
                NodeList optionData = currentDialogueXML.getElementsByTagName(OPTION_TAG);
                boolean isOptionVisible = true;
                for (int optionsIdx = 0; optionsIdx < optionData.getLength(); optionsIdx++) {
                    Node optionNode = optionData.item(optionsIdx);
                    NodeList optionChildNodes = optionNode.getChildNodes();
                    String nextDialogue = null;
                    String optionText = null;

                    //Check all elements for relevant data
                    for (int j = 0; j < optionChildNodes.getLength(); j++)
                        if (optionNode.getNodeName().equals(OPTION_TAG)) {
                            Element optionNodeElement = (Element) optionNode;
                            if (optionNodeElement.hasAttribute(NEXT_DIALOGUE_TAG))
                                nextDialogue = optionNodeElement.getAttribute(NEXT_DIALOGUE_TAG);
                            optionText = optionNode.getTextContent();

                            isOptionVisible = !optionNodeElement.hasAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF) ||
                                    optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF)
                                            .equals(getVariableCondition(optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_TYPE), optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME)));
                        }
                    if (isOptionVisible || DEBUG_ALL_TEXTBOX_OPTIONS_VISIBLE)
                        addOption(optionText, nextDialogue);
                }
                break;
            default:
                for (int messageIdx = 0; messageIdx < xmlLines.getLength(); messageIdx++) //add lines
                {
                    String message = xmlLines.item(messageIdx).getTextContent();
                    messages.add(message);//Without formatting the message
                }
                break;
        }
        nextDialogue = checkForNextDialogues(currentDialogueXML);
    }

    private String getVariableCondition(String type, String varName)
    {
        String methodName = "checkVariableCondition() ";
        String eval = null;
        if (type.equals("boolean")) {
            eval = GameVariables.getGenericVariableManager().getValue(varName);
            if (eval == null)
                System.out.println(CLASSNAME + methodName + "variable not set: " + varName);
        }
        else if (type.equals("player")) {
            if (varName.equals("spritestatus"))
                return WorldView.getPlayer().getActor().getGeneralStatus();
        }

        return eval;
    }

    private String checkForNextDialogues(Element currentDialogue)
    {
        NodeList nextDialogueIdList = currentDialogue.getElementsByTagName(NEXT_DIALOGUE_TAG);
        if (nextDialogueIdList.getLength() > 0) {
            return nextDialogueIdList.item(0).getTextContent();
        }
        else if (currentDialogue.hasAttribute(NEXT_DIALOGUE_TAG)) {
            return currentDialogue.getAttribute(NEXT_DIALOGUE_TAG);
        }
        else {
            return null;
        }
    }

    public void addOption(String optionMessage, String nextDialogue)
    {
        options.add(new Option(optionMessage, nextDialogue));
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
        for (Option option : options) {
            optionMessages.add(option.optionMessage);
        }
        return optionMessages;
    }

    class Option
    {
        Option(String optionMessage, String nextDialogue)
        {
            this.optionMessage = optionMessage;
            this.nextDialogue = nextDialogue;
        }

        String nextDialogue;
        String optionMessage;

        @Override
        public String toString()
        {
            return "Option{" +
                    "nextDialogue='" + nextDialogue + '\'' +
                    ", optionMessage='" + optionMessage + '\'' +
                    '}';
        }
    }

    public String getSpriteStatus()
    {
        return spriteStatus;
    }

    public String getSensorStatus()
    {
        return sensorStatus;
    }

    public void setSpriteStatus(String spriteStatus)
    {
        if (spriteStatus.trim().isEmpty())
            this.spriteStatus = null;
        else
            this.spriteStatus = spriteStatus;
    }

    public void setSensorStatus(String sensorStatus)
    {
        String methodName = "setSensorStatus() ";
        if (sensorStatus.trim().isEmpty())
            this.sensorStatus = null;
        else
            this.sensorStatus = sensorStatus;
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
