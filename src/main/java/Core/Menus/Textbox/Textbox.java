package Core.Menus.Textbox;

import Core.Actor;
import Core.GameVariables;
import Core.GameWindow;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.Menus.DiscussionGame.DiscussionGame;
import Core.Menus.Personality.PersonalityScreenController;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static Core.Configs.Config.*;

public class Textbox
{
    private static final String CLASSNAME = "Textbox/";
    private static double WIDTH = TEXTBOX_WIDTH;
    private static double HEIGHT = TEXTBOX_HEIGHT;
    private static Point2D SCREEN_POSITION = TEXT_BOX_POSITION;
    private static Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    Canvas textboxCanvas = new Canvas(WIDTH, HEIGHT);
    GraphicsContext textboxGc = textboxCanvas.getGraphicsContext2D();
    WritableImage textboxImage;
    Dialogue readDialogue;
    Element dialogueFileRoot;
    int messageIdx = 0;
    int backgroundOffsetX = 16;
    int backgroundOffsetYDecorationTop = 10;
    int backgroundOffsetYTalkIcon = 50;
    int backgroundOffsetYDecorationBtm = 10;
    Color background = Color.rgb(60, 90, 85);
    final int firstLineOffsetY = backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon + 20;
    final int xOffsetTextLine = 40;
    final int maxDigitsInLine = 38;
    String nextDialogueID = null;
    List<String> lineSplitMessage;
    Integer markedOption = 0;
    Actor actorOfDialogue;
    Point2D mousePosRelativeToTextboxOverlay = null;
    Long lastTimeNewLetterRendered = 0L;
    int maxLettersIdxRendered = 0;

    //TalkIcon
    int talkIconWidth = 280;
    int talkIconHeight = 100;
    Rectangle2D talkIcon = new Rectangle2D(WIDTH - talkIconWidth, 0, talkIconWidth, talkIconHeight);
    boolean isInfoButtonHovered = false;

    Image cornerTopLeft;
    Image cornerBtmRight;
    Image characterButton;

    public Textbox()
    {
        cornerTopLeft = new Image(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = new Image(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        characterButton = new Image(IMAGE_DIRECTORY_PATH + "txtbox/characterMenuButtonTR.png");
    }

    public void startConversation(Actor actorParam)
    {
        String methodName = "startConversation() ";
        actorOfDialogue = actorParam;
        dialogueFileRoot = Utilities.readXMLFile(DIALOGUE_FILE_PATH + actorOfDialogue.getSpriteList().get(0).getDialogueFileName() + ".xml");
        readDialogue = readDialogue(actorOfDialogue.getSpriteList().get(0).getInitDialogueId());
        drawTextbox();

        if (actorOfDialogue.getPersonalityContainer() != null)
            actorOfDialogue.getPersonalityContainer().incrementNumberOfInteraction();
    }

    public void startConversation(String dialogueFile, String dialogueId)
    {
        String methodName = "startConversation(String, String) ";
        actorOfDialogue = null;
        dialogueFileRoot = Utilities.readXMLFile(DIALOGUE_FILE_PATH + dialogueFile + ".xml");
        readDialogue = readDialogue(dialogueId);
        drawTextbox();
    }

    //For Discussion if File is already read, Discussions send next Dialogue
    public Dialogue readDialogue(String dialogueIdentifier)
    {
        return readDialogue(dialogueIdentifier, dialogueFileRoot);
    }

    //If Dialogue is not current file (for analysis)
    private Dialogue readDialogue(String dialogueIdentifier, Element xmlRoot)
    {
        String methodName = "readDialogue() ";
        boolean debug = true;
        boolean dialogueFound = false;
        Dialogue readDialogue = new Dialogue();
        NodeList dialogues = xmlRoot.getElementsByTagName(DIALOGUE_TAG);
        for (int i = 0; i < dialogues.getLength(); i++) //iterate dialogues of file
        {
            //found dialogue with ID
            if (((Element) dialogues.item(i)).getAttribute(ID_TAG).equals(dialogueIdentifier))
            {
                dialogueFound = true;
                Element currentDialogue = ((Element) dialogues.item(i));
                String dialogueType = currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_TYPE);
                NodeList xmlLines = currentDialogue.getElementsByTagName(LINE_TAG);
                readDialogue.setSpriteStatus(currentDialogue.getAttribute(ACTOR_STATUS_TAG));
                readDialogue.setSensorStatus(currentDialogue.getAttribute(SENSOR_STATUS_TAG));

                //check for type normal and decision
                readDialogue.type = dialogueType;
                //Decision
                if (dialogueType.equals(decision_TYPE_ATTRIBUTE))
                {
                    //For all options
                    NodeList optionData = currentDialogue.getElementsByTagName(OPTION_TAG);
                    boolean isOptionVisible = true;
                    for (int optionsIdx = 0; optionsIdx < optionData.getLength(); optionsIdx++)
                    {
                        Node optionNode = optionData.item(optionsIdx);
                        NodeList optionChildNodes = optionNode.getChildNodes();
                        String nextDialogue = null;
                        String visibleLine = null;
                        //Check all elements for relevant data
                        for (int j = 0; j < optionChildNodes.getLength(); j++)
                        {
                            Node node = optionChildNodes.item(j);
                            //Old version with extra next dialogue line, can be deleted once xml are corrected
                            if (node.getNodeName().equals(NEXT_DIALOGUE_TAG))
                            {
                                nextDialogue = node.getTextContent();
                                continue;
                            }
                            else if (node.getNodeName().equals(LINE_TAG))
                            {
                                visibleLine = node.getTextContent();
                                continue;
                            }

                            //new version with next dialogue as attribute
                            if (optionNode.getNodeName().equals(OPTION_TAG))
                            {
                                Element optionNodeElement = (Element) optionNode;
                                if (optionNodeElement.hasAttribute(NEXT_DIALOGUE_TAG))
                                    nextDialogue = optionNodeElement.getAttribute(NEXT_DIALOGUE_TAG);
                                visibleLine = optionNode.getTextContent();

                                if (optionNodeElement.hasAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF) &&
                                        !optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF)
                                                .equals(checkVariableCondition(optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_TYPE), optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME))))
                                    isOptionVisible = false;
                                else
                                    isOptionVisible = true;

                            }
                        }
                        if (isOptionVisible)
                            readDialogue.addOption(visibleLine, nextDialogue);
                    }
                }
                //Discussion Type
                else if (dialogueType.equals(discussion_TYPE_ATTRIBUTE))
                {
                    String discussionGameName = currentDialogue.getAttribute(game_ATTRIBUTE);
                    String successNextMsg = currentDialogue.getAttribute(success_ATTRIBUTE);
                    String defeatNextMsg = currentDialogue.getAttribute(defeat_ATTRIBUTE);
                    readDialogue.addOption(success_ATTRIBUTE, successNextMsg);
                    readDialogue.addOption(defeat_ATTRIBUTE, defeatNextMsg);
                    WorldView.setDiscussionGame(new DiscussionGame(discussionGameName, actorOfDialogue));
                }
                else if (dialogueType.equals(levelchange_TYPE_ATTRIBUTE))
                {
                    String levelname = currentDialogue.getAttribute(level_ATTRIBUTE);
                    String spawnId = currentDialogue.getAttribute(spawnID_ATTRIBUTE);
                    WorldView.getSingleton().saveStage();
                    WorldView.getSingleton().loadStage(levelname, spawnId);
                }
                else if (dialogueType.equals(dayChange_TYPE_ATTRIBUTE))
                {
                    WorldViewController.setWorldViewStatus(WorldViewStatus.DAY_SUMMARY);
                    DaySummaryScreenController.newDay();
                }
                else if (dialogueType.equals(TEXTBOX_ATTRIBUTE_BOOLEAN))
                {
                    // Boolean var = GameVariables.getBooleanWorldVariables().get(currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    String var = GameVariables.getGenericVariableManager().getValue(currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    if (var == null)
                        System.out.println(CLASSNAME + methodName + "variable not set: " + currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    nextDialogueID = Boolean.parseBoolean(var) ? currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_TRUE) : currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_FALSE);
                    return readDialogue(nextDialogueID);
                }
                else
                //Normal Textbox
                {
                    for (int messageIdx = 0; messageIdx < xmlLines.getLength(); messageIdx++) //add lines
                    {
                        String message = xmlLines.item(messageIdx).getTextContent();
                        readDialogue.messages.add(message);//Without formatting the message
                    }
                    if (dialogueType.equals(TEXTBOX_ATTRIBUTE_GET_MONEY))
                    {
                        int amount = Integer.parseInt(currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_VALUE));
                        GameVariables.addPlayerMoney(amount);
                    }
                    if (currentDialogue.hasAttribute(TEXTBOX_ATTRIBUTE_SET))
                    {
                        String varname = currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME);
                        String val = currentDialogue.getAttribute(TEXTBOX_ATTRIBUTE_SET);
                        //GameVariables.getBooleanWorldVariables().put(varname, val);
                        GameVariables.getGenericVariableManager().setValue(varname, val);
                    }
                }

                //Check for further dialogues
                NodeList nextDialogueIdList = currentDialogue.getElementsByTagName(NEXT_DIALOGUE_TAG);
                if (nextDialogueIdList.getLength() > 0)
                {
                    nextDialogueID = nextDialogueIdList.item(0).getTextContent();
                    readDialogue.nextDialogue = nextDialogueIdList.item(0).getTextContent();
                }
                else if (currentDialogue.hasAttribute(NEXT_DIALOGUE_TAG))
                {
                    nextDialogueID = currentDialogue.getAttribute(NEXT_DIALOGUE_TAG);
                }
                else
                {
                    nextDialogueID = null;
                    readDialogue.nextDialogue = null;
                }
                break;
            }
        }
        //Sensor Status Changes once per Dialogue
        if (readDialogue.getSensorStatus() != null)
            actorOfDialogue.setSensorStatus(readDialogue.getSensorStatus());
        if (readDialogue.getSpriteStatus() != null)
        {
            changeActorStatus(readDialogue.getSpriteStatus());
        }

        if (!dialogueFound)
            throw new NullPointerException("Dialogue not found: " + actorOfDialogue.getSpriteList().get(0).getDialogueFileName() + ": " + dialogueIdentifier);

        return readDialogue;
    }

    private String checkVariableCondition(String type, String varName)
    {
        String methodName = "checkVariableCondition() ";
        String eval = "true";
        if (type.equals(TEXTBOX_ATTRIBUTE_BOOLEAN))
            eval = GameVariables.getGenericVariableManager().getValue(varName);
        if (eval == null)
            System.out.println(CLASSNAME + methodName + "variable not set: " + varName);
        return eval;
    }

    public void processKey(ArrayList<String> input, Long currentNanoTime)
    {
        String methodName = "processKey() ";
        int maxMarkedOptionIdx = lineSplitMessage.size() - 1;
        int newMarkedOption = markedOption;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - WorldView.getPlayer().getActor().getLastInteraction()) / 1000000000.0;
        if (!(elapsedTimeSinceLastInteraction > TIME_BETWEEN_DIALOGUE))
            return;

        if (input.contains(KEYBOARD_INTERACT) || input.contains("ENTER") || input.contains("SPACE"))
        {
            nextMessage(currentNanoTime);
            WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
            return;
        }
        else if (input.contains(KEYBOARD_ESCAPE))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
        }

        if (input.contains("W") || input.contains("UP"))
            newMarkedOption--;
        if (input.contains("S") || input.contains("DOWN"))
            newMarkedOption++;

        if (newMarkedOption < 0)
            newMarkedOption = maxMarkedOptionIdx;
        if (newMarkedOption > maxMarkedOptionIdx)
            newMarkedOption = 0;

        if (markedOption != newMarkedOption)
        {
            markedOption = newMarkedOption;
            WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
            drawTextbox();
        }
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked)
    {
        String methodName = "processMouse(Point2D, boolean) ";
        Point2D textboxPosition = WorldView.getTextBoxPosition();
        Rectangle2D textboxPosRelativeToWorldview = new Rectangle2D(textboxPosition.getX(), textboxPosition.getY(), WIDTH, HEIGHT);

        if (textboxPosRelativeToWorldview.contains(mousePosition))
        {
            mousePosRelativeToTextboxOverlay = new Point2D(mousePosition.getX() - textboxPosition.getX(), mousePosition.getY() - textboxPosition.getY());
        }
        else mousePosRelativeToTextboxOverlay = null;

        if (actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null && talkIcon.contains(mousePosRelativeToTextboxOverlay))
        {
            isInfoButtonHovered = true;
        }
        else
            isInfoButtonHovered = false;

        //Check if hovered on Option
        int offsetYTmp = firstLineOffsetY;
        if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE) && GameWindow.getSingleton().isMouseMoved())
        {
            for (int checkedLineIdx = 0; checkedLineIdx < lineSplitMessage.size(); checkedLineIdx++)
            {
                Rectangle2D positionOptionRelativeToWorldView = new Rectangle2D(textboxPosition.getX(), textboxPosition.getY() + offsetYTmp, WIDTH, textboxGc.getFont().getSize());
                offsetYTmp += textboxGc.getFont().getSize();
                //Hovers over Option
                if (positionOptionRelativeToWorldView.contains(mousePosition))
                {
                    if (markedOption != checkedLineIdx)
                    {
                        markedOption = checkedLineIdx;
                        drawTextbox();
                    }
                    break;
                }
            }
            GameWindow.getSingleton().setMouseMoved(false);
        }
        //System.out.println(CLASSNAME + methodName + SCREEN_AREA.contains(mousePosition));
        if (isMouseClicked)
        {
            if (isInfoButtonHovered)
            {
                WorldView.setPersonalityScreenController(new PersonalityScreenController(actorOfDialogue));
                WorldViewController.setWorldViewStatus(WorldViewStatus.PERSONALITY);
            }
            else if (!SCREEN_AREA.contains(mousePosition))
            {
                //Do nothing if not within textbox
            }
            else
                nextMessage(GameWindow.getSingleton().getRenderTime());
        }
    }

    public void groupAnalysis(List<Actor> actorsList, Actor speakingActor)
    {
        String methodName = "groupAnalysis(List<Actor>, Actor) ";
        startConversation(speakingActor);
        for (Actor actor : actorsList)
        {
            Element analysisDialogueFileObserved = Utilities.readXMLFile(DIALOGUE_FILE_PATH + actor.getSpriteList().get(0).getDialogueFileName() + ".xml");
            //Dialogue analysisMessageObserved = readDialogue("analysis-" + actor.getDialogueStatusID(), analysisDialogueFileObserved);
            Dialogue analysisMessageObserved = readDialogue("analysis-" + actor.getSpriteList().get(0).getInitDialogueId(), analysisDialogueFileObserved);
            readDialogue.messages.add(actor.getActorInGameName() + analysisMessageObserved.messages.get(0));
        }
    }

    private boolean hasNextMessage()
    {
        return readDialogue.messages.size() > messageIdx + 1;
    }

    public void nextMessage(Long currentNanoTime)
    {
        String methodName = "nextMessage(Long) ";
        Actor playerActor = WorldView.getPlayer().getActor();

        maxLettersIdxRendered = 0;

        if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE))
        {
            nextDialogueID = readDialogue.options.get(markedOption).nextDialogue;
            markedOption = 0;
        }

        if (hasNextMessage())//More messages in this dialogue
        {
            messageIdx++;
            drawTextbox();
        }
        else if (nextDialogueID != null)//No more messages but nextDialogue defined
        {
            messageIdx = 0;
            readDialogue = readDialogue(nextDialogueID, dialogueFileRoot);
            drawTextbox();
        }
        else //End Textbox
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
            messageIdx = 0;
        }
        playerActor.setLastInteraction(currentNanoTime);

        //for PC screen we want change after each click
        if (readDialogue.getSpriteStatus() != null)
        {
            changeActorStatus(readDialogue.getSpriteStatus());
        }


    }

    private void drawTextbox() throws NullPointerException
    {
        String methodName = "drawTextbox() ";
        boolean debug = false;
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);
        Font font_estrog = Font.loadFont(getClass().getResource(FONT_DIRECTORY_PATH + "estrog__.ttf").toExternalForm(), 30);

        textboxGc.clearRect(0, 0, WIDTH, HEIGHT);

        //testBackground
        if (debug)
        {
            textboxGc.setFill(Color.RED);
            textboxGc.fillRect(0, 0, WIDTH, HEIGHT);
        }

        //Background
        textboxGc.setFill(background);
        textboxGc.setGlobalAlpha(0.9);
        textboxGc.fillRect(backgroundOffsetX, backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetYDecorationTop - backgroundOffsetYTalkIcon - backgroundOffsetYDecorationBtm);

        textboxGc.setFont(font_estrog);
        textboxGc.setTextAlign(TextAlignment.LEFT);
        textboxGc.setTextBaseline(VPos.TOP);

        if (markedOption != null && readDialogue.type.equals(decision_TYPE_ATTRIBUTE))
        {
            textboxGc.setFill(marking);
            textboxGc.fillRect(xOffsetTextLine, firstLineOffsetY + markedOption * textboxGc.getFont().getSize() + 5, WIDTH - 100, textboxGc.getFont().getSize());
        }

        //Decoration of textfield
        textboxGc.setGlobalAlpha(1);
        textboxGc.drawImage(cornerTopLeft, 0, backgroundOffsetYTalkIcon);
        textboxGc.drawImage(cornerBtmRight, WIDTH - cornerBtmRight.getWidth(), HEIGHT - cornerBtmRight.getHeight());

        int yOffsetTextLine = firstLineOffsetY;
        textboxGc.setFill(font);
        //Format Text
        if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE))
        {
            lineSplitMessage = readDialogue.getOptionMessages();
        }
        else if (readDialogue.type.equals(discussion_TYPE_ATTRIBUTE))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.DISCUSSION_GAME);
            lineSplitMessage = wrapText("Discussion ongoing");
        }
        else if (readDialogue.type.equals(levelchange_TYPE_ATTRIBUTE))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
            lineSplitMessage = wrapText("technical");
        }
        else if (readDialogue.type.equals(dayChange_TYPE_ATTRIBUTE))
        {
        }
        //else if(readDialogue.type.equals(TEXTBOX_ATTRIBUTE_BOOLEAN))
//        {
//            //lineSplitMessage = wrapText("technical");
//            //nextMessage(GameWindow.getCurrentNanoRenderTimeGameWindow());
//        }
        else
        {
            String nextMessage = readDialogue.messages.get(messageIdx);
            lineSplitMessage = wrapText(nextMessage);
        }


        for (int lineIdx = 0; lineIdx < lineSplitMessage.size(); lineIdx++)
        {
//            Set<String> regex = new HashSet<>();
//            String regex_default = "%%DE";
//            String regex_red = "%%RD";
//            String regex_green = "%%GR";
//            regex.add(regex_default);
//            regex.add(regex_red);

            String line = lineSplitMessage.get(lineIdx);

            FontManager fontManager = new FontManager(line);
            line = fontManager.removeFontMarkings(line);
//            Queue<Pair<String, Pair<Integer, Integer>>> charSpecialMarkingFound = new LinkedBlockingDeque<Pair<String, Pair<Integer, Integer>>>();
//            int idxFormatRegex = 0;
//            int idxRegexEnd = 0;
//            do
//            {
//                idxFormatRegex = line.indexOf("%%");
//                if (idxFormatRegex >= 0)
//                {
//                    String beginRegex = line.substring(idxFormatRegex, idxFormatRegex + 4);
//                    line = line.replaceFirst(beginRegex, "");
//                    idxRegexEnd = line.indexOf("%%");
//                    line = line.replaceFirst("%%", "");
//                    charSpecialMarkingFound.add(new Pair<String, Pair<Integer, Integer>>(beginRegex, new Pair<>(idxFormatRegex, idxRegexEnd)));
//                }
//            } while (idxFormatRegex >= 0);

            //System.out.println(CLASSNAME + methodName + charSpecialMarkingFound);

            double elapsedTimeSinceLastInteraction = (GameWindow.getCurrentNanoRenderTimeGameWindow() - lastTimeNewLetterRendered) / 1000000000.0;
            if (elapsedTimeSinceLastInteraction > 0.005)
            {
                maxLettersIdxRendered++;
                lastTimeNewLetterRendered = GameWindow.getCurrentNanoRenderTimeGameWindow();
            }

            int lettersRendered = Math.min(maxLettersIdxRendered, line.length());
            String visibleLine = line.substring(0, lettersRendered);

            for (Integer i = 0; i < visibleLine.length(); i++)
            {
                textboxGc.setFill(fontManager.getFontAtLetter(i));
//                if (charSpecialMarkingFound.peek() != null
//                        && i >= charSpecialMarkingFound.peek().getValue().getKey()//begin
//                        && i<charSpecialMarkingFound.peek().getValue().getValue())//end
//                {
//                    //System.out.println(CLASSNAME + methodName + charSpecialMarkingFound);
//                    if (charSpecialMarkingFound.peek().getKey().equals(regex_red))
//                        textboxGc.setFill(Color.RED);
//                    else if(charSpecialMarkingFound.peek().getKey().equals(regex_green))
//                        textboxGc.setFill(Color.GREEN);
//                    else if(charSpecialMarkingFound.peek().getKey().equals(regex_default))
//                        textboxGc.setFill(font);
//
//                    if(i+1 >= charSpecialMarkingFound.peek().getValue().getValue())
//                        charSpecialMarkingFound.remove();
//                }
//                else
//                    textboxGc.setFill(font);

                char c = visibleLine.charAt(i);
                textboxGc.fillText(String.valueOf(c),
                        Math.round(xOffsetTextLine) + textWidth(font_estrog, line.substring(0, i)),
                        Math.round(yOffsetTextLine) + FONT_Y_OFFSET_ESTROG__SIZE30);
            }
            //System.out.println(CLASSNAME + methodName + textWidth(font_estrog, visibleLine));

//            textboxGc.fillText(
//                //    lineSplitMessage.get(lineIdx),
//                    visibleLine,
//                    Math.round(xOffsetTextLine),
//                    Math.round(yOffsetTextLine) + FONT_Y_OFFSET_ESTROG__SIZE30
//            );
            yOffsetTextLine += textboxGc.getFont().getSize();
        }


        //Character Info Button
        if (actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null)
        {
            textboxGc.drawImage(characterButton, talkIcon.getMinX(), talkIcon.getMinY());
        }

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        textboxImage = textboxCanvas.snapshot(transparency, null);
    }


    private static double textWidth(Font font, String s)
    {
        Text text = new Text(s);
        text.setFont(font);
        return text.getBoundsInLocal().getWidth();
    }

    public void setNextDialogueFromDiscussionResult(boolean hasWon)
    {
        if (hasWon)
            nextDialogueID = readDialogue.getOption(success_ATTRIBUTE).nextDialogue;
        else
            nextDialogueID = readDialogue.getOption(defeat_ATTRIBUTE).nextDialogue;
    }

    private void changeActorStatus(String toGeneralStatus)
    {
        String methodName = "changeActorStatus(String) ";
        if (!actorOfDialogue.getGeneralStatus().equals(toGeneralStatus))
            actorOfDialogue.onTextboxSignal(toGeneralStatus);
    }

    private List<String> wrapText(String longMessage)
    {
        String methodName = "wrapText() ";
        List<String> wrapped = new ArrayList<>();
        String[] words = longMessage.split(" ");

        int numberDigits = 0;
        StringBuilder lineBuilder = new StringBuilder();
        for (int wordIdx = 0; wordIdx < words.length; wordIdx++)
        {
            if (numberDigits + words[wordIdx].length() > maxDigitsInLine)
            {
                wrapped.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
                numberDigits = 0;
            }
            numberDigits += words[wordIdx].length();
            lineBuilder.append(words[wordIdx]).append(" ");
        }
        wrapped.add(lineBuilder.toString());

        return wrapped;
    }

    public WritableImage showMessage()
    {
        drawTextbox();
        return textboxImage;
    }

    public static double getTEXT_BOX_WIDTH()
    {
        return WIDTH;
    }

    public static double getTEXT_BOX_HEIGHT()
    {
        return HEIGHT;
    }
}
