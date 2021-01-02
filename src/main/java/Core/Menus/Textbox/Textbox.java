package Core.Menus.Textbox;

import Core.*;
import Core.Enums.Knowledge;
import Core.Menus.AchievmentLog.NewMessageOverlay;
import Core.Menus.CoinGame.CoinGame;
import Core.Menus.DaySummary.DaySummaryScreenController;
import Core.Menus.Personality.PersonalityScreenController;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;

public class Textbox
{
    private static final String CLASSNAME = "Textbox/";
    private static double WIDTH = TEXTBOX_WIDTH;
    private static double HEIGHT = TEXTBOX_HEIGHT;
    private static Point2D SCREEN_POSITION = TEXT_BOX_POSITION;
    private static Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    Canvas textboxCanvas = new Canvas(WIDTH, HEIGHT);
    GraphicsContext gc = textboxCanvas.getGraphicsContext2D();
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
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        characterButton = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/characterMenuButtonTR.png");
        gc.setFont(FONT_ESTROG_30_DEFAULT);
    }

    public void startConversation(Actor actorParam)
    {
        String methodName = "startConversation() ";
        init();
        actorOfDialogue = actorParam;
        dialogueFileRoot = Utilities.readXMLFile(actorOfDialogue.getSpriteList().get(0).getDialogueFileName());
        readDialogue = readDialogue(actorOfDialogue.getSpriteList().get(0).getInitDialogueId());

        if (actorOfDialogue.getPersonalityContainer() != null)
            actorOfDialogue.getPersonalityContainer().incrementNumberOfInteraction();
    }

    public void startConversation(String dialogueFile, String dialogueId)
    {
        String methodName = "startConversation(String, String) ";
        init();
        actorOfDialogue = null;
        dialogueFileRoot = Utilities.readXMLFile(dialogueFile);
        readDialogue = readDialogue(dialogueId);
    }

    private void init()
    {
        messageIdx = 0;
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
        boolean dialogueFound = false;
        Dialogue readDialogue = null;
        NodeList dialogues = xmlRoot.getElementsByTagName(DIALOGUE_TAG);
        for (int i = 0; i < dialogues.getLength(); i++) //iterate dialogues of file
        {
            //found dialogue with ID
            if (((Element) dialogues.item(i)).getAttribute(ID_TAG).equals(dialogueIdentifier)) {
                dialogueFound = true;
                Element currentDialogueXML = ((Element) dialogues.item(i));
                readDialogue = new Dialogue(currentDialogueXML);
                String dialogueType = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TYPE);
                //NodeList xmlLines = currentDialogueXML.getElementsByTagName(LINE_TAG);
                //readDialogue.setSpriteStatus(currentDialogueXML.getAttribute(ACTOR_STATUS_TAG));
                //readDialogue.setSensorStatus(currentDialogueXML.getAttribute(SENSOR_STATUS_TAG));
                //readDialogue.type = dialogueType;

                if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE)) {
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
                            if (optionNode.getNodeName().equals(OPTION_TAG))
                            {
                                Element optionNodeElement = (Element) optionNode;
                                if (optionNodeElement.hasAttribute(NEXT_DIALOGUE_TAG))
                                    nextDialogue = optionNodeElement.getAttribute(NEXT_DIALOGUE_TAG);
                                optionText = optionNode.getTextContent();

                                isOptionVisible = !optionNodeElement.hasAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF) ||
                                        optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VISIBLE_IF)
                                                .equals(getVariableCondition(optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_TYPE), optionNodeElement.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME)));
                            }
                        if (isOptionVisible || DEBUG_ALL_TEXTBOX_OPTIONS_VISIBLE)
                            readDialogue.addOption(optionText, nextDialogue);
                    }
                }
                //Discussion Type
                else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_DISCUSSION)) {
                    String discussionGameName = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_GAME);
                    String successNextMsg = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SUCCESS);
                    String defeatNextMsg = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DEFEAT);
                    readDialogue.addOption(TEXTBOX_ATTRIBUTE_SUCCESS, successNextMsg);
                    readDialogue.addOption(TEXTBOX_ATTRIBUTE_DEFEAT, defeatNextMsg);
                    WorldView.setDiscussionGame(new CoinGame(discussionGameName, actorOfDialogue));
                }
                else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_LEVELCHANGE)) {
                    String levelname = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_LEVEL);
                    String spawnId = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SPAWN_ID);
                    WorldView.getSingleton().saveStage();
                    WorldView.getSingleton().loadStage(levelname, spawnId);
                }
                else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_DAY_CHANGE)) {
                    WorldViewController.setWorldViewStatus(WorldViewStatus.DAY_SUMMARY);
                    DaySummaryScreenController.newDay();
                }
                else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_VALUE_BOOLEAN)) {
                    String var = GameVariables.getGenericVariableManager().getValue(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    if (var == null)
                        System.out.println(CLASSNAME + methodName + "variable not set: " + currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    nextDialogueID = Boolean.parseBoolean(var) ? currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TRUE) : currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_FALSE);
                    return readDialogue(nextDialogueID);
                }
                else
                //Normal Textbox
                {
                    //for (int messageIdx = 0; messageIdx < xmlLines.getLength(); messageIdx++) //add lines
                    //{
                    //    String message = xmlLines.item(messageIdx).getTextContent();
                    //    readDialogue.messages.add(message);//Without formatting the message
                    //}
                    if (dialogueType.equals(TEXTBOX_ATTRIBUTE_GET_MONEY)) {
                        int amount = Integer.parseInt(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VALUE));
                        GameVariables.addPlayerMoney(amount);
                        NewMessageOverlay.showMsg("received " + amount + " GSC!");
                    }
                    if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_SET)) {
                        String varname = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME);
                        String val = currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SET);
                        GameVariables.setGenericVariable(varname, val);
                    }
                    if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_BUMP)) {
                        WorldView.getSingleton().activateBump();
                    }
                    if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_ITEM_ACTOR)) {
                        Collectible collectible = Collectible.createCollectible(
                                currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEM_ACTOR)
                                , currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEM_NAME)
                                , currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_ITEM_STATUS));
                        WorldView.getPlayer().getActor().getInventory().addItem(collectible);
                        NewMessageOverlay.showMsg("New " + collectible.getIngameName() + "!");
                    }
                    if (currentDialogueXML.hasAttribute(TEXTBOX_ATTRIBUTE_KNOWLEDGE)) {
                        GameVariables.addPlayerKnowledge(Knowledge.of(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_KNOWLEDGE)));
                    }
                    if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_DIALOGUE_FILE))) {
                        actorOfDialogue.setDialogueFile(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_FILE));
                    }
                    if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_DIALOGUE_ID))) {
                        actorOfDialogue.setDialogueId(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_ID));
                    }
                    if (currentDialogueXML.hasAttribute((TEXTBOX_ATTRIBUTE_SET_WORLD_LIGHT))) {
                        if (currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_SET_WORLD_LIGHT).equals("night"))
                            WorldView.getSingleton().setShadowColor(COLOR_NIGHT);
                        else
                            WorldView.getSingleton().setShadowColor(null);
                    }

                }

                nextDialogueID = readDialogue.nextDialogue;
                //checkForNextDialogues(readDialogue, currentDialogueXML);

                //Check for changes for other sprites
                NodeList spriteChanges = currentDialogueXML.getElementsByTagName(SPRITECHANGE_TAG);
                if (spriteChanges != null) {
                    Element changeDirective;
                    for (int j = 0; j < spriteChanges.getLength(); j++) {
                        changeDirective = (Element) spriteChanges.item(j);
                        String id = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_SPRITE_ID);
                        String newStatus = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_NEW_STATUS);
                        String newDialogueId = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_ID);
                        Actor actor = WorldView.getSingleton().getSpriteByName(id);
                        if (!newStatus.isEmpty())
                            actor.setGeneralStatus(newStatus);
                        if (!newDialogueId.isEmpty())
                            actor.setDialogueId(newDialogueId);
                        actor.updateCompoundStatus();
                    }
                }
                break;
            }
        }
        //Sensor Status Changes once per Dialogue
        if (readDialogue.getSensorStatus() != null)
            actorOfDialogue.setSensorStatus(readDialogue.getSensorStatus());
        if (readDialogue.getSpriteStatus() != null) {
            changeActorStatus(readDialogue.getSpriteStatus());
        }

        if (!dialogueFound)
            throw new NullPointerException("Dialogue not found: " + actorOfDialogue.getSpriteList().get(0).getDialogueFileName() + ": " + dialogueIdentifier);

        return readDialogue;
    }

    // private void checkForNextDialogues(Dialogue readDialogue, Element currentDialogue)
    // {
    //     //Check for further dialogues
    //     NodeList nextDialogueIdList = currentDialogue.getElementsByTagName(NEXT_DIALOGUE_TAG);
    //     if (nextDialogueIdList.getLength() > 0) {
    //         nextDialogueID = nextDialogueIdList.item(0).getTextContent();
    //         readDialogue.nextDialogue = nextDialogueIdList.item(0).getTextContent();
    //     }
    //     else if (currentDialogue.hasAttribute(NEXT_DIALOGUE_TAG)) {
    //         nextDialogueID = currentDialogue.getAttribute(NEXT_DIALOGUE_TAG);
    //     }
    //     else {
    //         nextDialogueID = null;
    //         readDialogue.nextDialogue = null;
    //     }
    // }

    private String getVariableCondition(String type, String varName)
    {
        String methodName = "checkVariableCondition() ";
        String eval = null;
        if (type.equals("boolean")) {
            eval = GameVariables.getGenericVariableManager().getValue(varName);
            if (eval == null)
                System.out.println(CLASSNAME + methodName + "variable not set: " + varName);
            //return eval;
        }
        else if (type.equals("player"))
        {
            if (varName.equals("spritestatus"))
                return WorldView.getPlayer().getActor().getGeneralStatus();
        }

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

        isInfoButtonHovered = actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null && talkIcon.contains(mousePosRelativeToTextboxOverlay);

        //Check if hovered on Option
        int offsetYTmp = firstLineOffsetY;
        if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE) && GameWindow.getSingleton().isMouseMoved())
        {
            for (int checkedLineIdx = 0; checkedLineIdx < lineSplitMessage.size(); checkedLineIdx++)
            {
                Rectangle2D positionOptionRelativeToWorldView = new Rectangle2D(textboxPosition.getX(), textboxPosition.getY() + offsetYTmp, WIDTH, gc.getFont().getSize());
                offsetYTmp += gc.getFont().getSize();
                //Hovers over Option
                if (positionOptionRelativeToWorldView.contains(mousePosition))
                {
                    if (markedOption != checkedLineIdx)
                    {
                        markedOption = checkedLineIdx;
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
            Element analysisDialogueFileObserved = Utilities.readXMLFile(actor.getSpriteList().get(0).getDialogueFileName() + ".xml");
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
        }
        else if (nextDialogueID != null)//No more messages but nextDialogue defined
        {
            messageIdx = 0;
            readDialogue = readDialogue(nextDialogueID, dialogueFileRoot);
        }
        else //End Textbox
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
            //messageIdx = 0;
        }
        playerActor.setLastInteraction(currentNanoTime);

        //for PC screen we want change after each click
        if (readDialogue.getSpriteStatus() != null)
        {
            changeActorStatus(readDialogue.getSpriteStatus());
        }


    }

    public WritableImage render() throws NullPointerException
    {
        String methodName = "drawTextbox() ";
        boolean debug = false;
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        //testBackground
        if (debug)
        {
            gc.setFill(Color.RED);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }

        //Background
        gc.setFill(background);
        gc.setGlobalAlpha(0.9);
        gc.fillRect(backgroundOffsetX, backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetYDecorationTop - backgroundOffsetYTalkIcon - backgroundOffsetYDecorationBtm);


        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        if (markedOption != null && readDialogue.type.equals(decision_TYPE_ATTRIBUTE))
        {
            gc.setFill(marking);
            gc.fillRect(xOffsetTextLine, firstLineOffsetY + markedOption * gc.getFont().getSize() + 5, WIDTH - 100, gc.getFont().getSize());
        }

        //Decoration of textfield
        gc.setGlobalAlpha(1);
        gc.drawImage(cornerTopLeft, 0, backgroundOffsetYTalkIcon);
        gc.drawImage(cornerBtmRight, WIDTH - cornerBtmRight.getWidth(), HEIGHT - cornerBtmRight.getHeight());

        int yOffsetTextLine = firstLineOffsetY;
        gc.setFill(font);
        //Format Text
        if (readDialogue.type.equals(decision_TYPE_ATTRIBUTE))
        {
            lineSplitMessage = readDialogue.getOptionMessages();
        }
        else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_DISCUSSION))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.DISCUSSION_GAME);
            lineSplitMessage = wrapText("Discussion ongoing");
        }
        else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_LEVELCHANGE))
        {
            WorldViewController.setWorldViewStatus(WorldViewStatus.WORLD);
            lineSplitMessage = wrapText("technical");
        }
        else if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_DAY_CHANGE))
        {
        }
        else
        {
            String nextMessage = readDialogue.messages.get(messageIdx);
            lineSplitMessage = wrapText(nextMessage);
        }

        for (int lineIdx = 0; lineIdx < lineSplitMessage.size(); lineIdx++)
        {
            String line = lineSplitMessage.get(lineIdx);
            FontManager fontManager = new FontManager(line);
            line = FontManager.removeFontMarkings(line);
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
                gc.setFill(fontManager.getFontAtLetter(i));
                char c = visibleLine.charAt(i);
                gc.fillText(String.valueOf(c),
                        Math.round(xOffsetTextLine) + textWidth(gc.getFont(), line.substring(0, i)),
                        Math.round(yOffsetTextLine) + FONT_Y_OFFSET_ESTROG__SIZE30);
            }
            yOffsetTextLine += gc.getFont().getSize();
        }

        //Character Info Button
        if (actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null)
        {
            gc.drawImage(characterButton, talkIcon.getMinX(), talkIcon.getMinY());
        }

        SnapshotParameters transparency = new SnapshotParameters();
        transparency.setFill(Color.TRANSPARENT);
        return textboxCanvas.snapshot(transparency, null);
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
            nextDialogueID = readDialogue.getOption(TEXTBOX_ATTRIBUTE_SUCCESS).nextDialogue;
        else
            nextDialogueID = readDialogue.getOption(TEXTBOX_ATTRIBUTE_DEFEAT).nextDialogue;
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
            int numberDigitsWithoutFontRegex = FontManager.removeFontMarkings(words[wordIdx]).length();
            if (numberDigits + numberDigitsWithoutFontRegex > TEXTBOX_MAX_LINE_LETTERS)
            {
                wrapped.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
                numberDigits = 0;
            }
            numberDigits += numberDigitsWithoutFontRegex + 1;
            lineBuilder.append(words[wordIdx]).append(" ");
        }
        wrapped.add(lineBuilder.toString());
        return wrapped;
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
