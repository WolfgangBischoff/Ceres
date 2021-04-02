package Core.Menus.Textbox;

import Core.Actor;
import Core.Enums.Direction;
import Core.GameVariables;
import Core.GameWindow;
import Core.Menus.Personality.PersonalityScreenController;
import Core.Utilities;
import Core.WorldView.WorldView;
import Core.WorldView.WorldViewController;
import Core.WorldView.WorldViewStatus;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static Core.Configs.Config.*;
import static Core.Enums.ActorTag.TURNS_DIRECTION_ONINTERACTION;

public class Textbox
{
    private static final String CLASSNAME = "Textbox/";
    private static final double WIDTH = TEXT_WIDTH;
    private static final double HEIGHT = TEXT_HEIGHT;
    private static final Point2D SCREEN_POSITION = TEXT_BOX_POSITION;
    private static final Rectangle2D SCREEN_AREA = new Rectangle2D(SCREEN_POSITION.getX(), SCREEN_POSITION.getY(), WIDTH, HEIGHT);
    private static final int OFFSET_MARKING_RIGHT = 80;
    private static final int LINE_SPACE = 5;
    private static Font font = FONT_ORBITRON_20;
    final int xOffsetTextLine = (int) SCREEN_POSITION.getX() + 40;
    Dialogue readDialogue;
    Element dialogueFileRoot;
    int messageIdx = 0;
    int backgroundOffsetX = 16;
    int backgroundOffsetYDecorationTop = 10;
    int backgroundOffsetYTalkIcon = 50;
    final int firstLineOffsetY = (int) SCREEN_POSITION.getY() + backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon + 30;
    int backgroundOffsetYDecorationBtm = 10;
    Color background = Color.rgb(60, 90, 85);
    String nextDialogueID = null;
    Integer markedOption = 0;
    Actor actorOfDialogue;
    Long lastTimeNewLetterRendered = 0L;
    int maxLettersIdxRendered = 0;
    FontManager fontManager = new FontManager();

    //TalkIcon
    int talkIconWidth = 280;
    int talkIconHeight = 80;
    Rectangle2D talkIcon = new Rectangle2D(SCREEN_POSITION.getX() + WIDTH - talkIconWidth, SCREEN_POSITION.getY(), talkIconWidth, talkIconHeight);
    boolean isInfoButtonHovered = false;

    Image cornerTopLeft;
    Image cornerBtmRight;
    Image characterButton;

    public Textbox()
    {
        cornerTopLeft = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxTL.png");
        cornerBtmRight = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/textboxBL.png");
        characterButton = Utilities.readImage(IMAGE_DIRECTORY_PATH + "txtbox/characterMenuButtonTR.png");
    }

    public void startConversation(Actor dialogueActor)
    {
        String methodName = "startConversation() ";
        init();
        actorOfDialogue = dialogueActor;
        dialogueFileRoot = Utilities.readXMLFile(actorOfDialogue.getSpriteList().get(0).getDialogueFileName());
        readDialogue = readDialogue(actorOfDialogue.getSpriteList().get(0).getInitDialogueId());

        if (actorOfDialogue.getPersonalityContainer() != null)
            actorOfDialogue.getPersonalityContainer().incrementNumberOfInteraction();

    }

    public void startConversation(String dialogueFile, String dialogueId)
    {
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
                readDialogue = new Dialogue(actorOfDialogue, currentDialogueXML);

                if (readDialogue.type.equals(TEXTBOX_ATTRIBUTE_VALUE_BOOLEAN)) {
                    String var = GameVariables.getGenericVariableManager().getValue(currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    if (var == null)
                        System.out.println(CLASSNAME + methodName + "variable not set: " + currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_VARIABLE_NAME));
                    nextDialogueID = Boolean.parseBoolean(var) ? currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_TRUE) : currentDialogueXML.getAttribute(TEXTBOX_ATTRIBUTE_FALSE);
                    return readDialogue(nextDialogueID);
                }

                nextDialogueID = readDialogue.nextDialogue;

                //Check for changes for other sprites
                NodeList spriteChanges = currentDialogueXML.getElementsByTagName(SPRITECHANGE_TAG);
                if (spriteChanges != null) {
                    Element changeDirective;
                    for (int j = 0; j < spriteChanges.getLength(); j++) {
                        changeDirective = (Element) spriteChanges.item(j);
                        String id = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_SPRITE_ID);
                        String newSensorStatus = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_SENSOR_STATUS);
                        String newStatus = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_NEW_STATUS);
                        String newDialogueId = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_ID);
                        String newDialogueFile = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_FILE);
                        List<Actor> actorToChange = WorldView.getSingleton().getSpritesByName(id);

                        for (Actor a : actorToChange) {
                            if (!newStatus.isEmpty())
                                a.setGeneralStatus(newStatus);
                            if (!newDialogueId.isEmpty())
                                a.setDialogueId(newDialogueId);
                            if (!newDialogueFile.isEmpty())
                                a.setDialogueFile(newDialogueFile);
                            if (!newSensorStatus.isEmpty())
                                a.setSensorStatus(newSensorStatus);
                            a.updateCompoundStatus();
                        }

                    }
                }
                break;
            }
        }

        if (!dialogueFound)
            throw new NullPointerException("Dialogue not found: " + actorOfDialogue.getSpriteList().get(0).getDialogueFileName() + ": " + dialogueIdentifier);

        //Sensor Status Changes once per Dialogue
        if (readDialogue.getSensorStatus() != null)
            actorOfDialogue.setSensorStatus(readDialogue.getSensorStatus());
        if (readDialogue.getSpriteStatus() != null) {
            changeActorStatus(readDialogue.getSpriteStatus());
        }

        return readDialogue;
    }


    public void processKey(ArrayList<String> input, Long currentNanoTime)
    {
        //int maxMarkedOptionIdx = lineSplitMessage.size() - 1;
        int maxMarkedOptionIdx = fontManager.wrappedMessage.size() - 1;
        int newMarkedOption = markedOption;
        double elapsedTimeSinceLastInteraction = (currentNanoTime - WorldView.getPlayer().getActor().getLastInteraction()) / 1000000000.0;
        if (!(elapsedTimeSinceLastInteraction > TIME_BETWEEN_DIALOGUE))
            return;

        if (input.contains(KEYBOARD_INTERACT) || input.contains("ENTER") || input.contains("SPACE")) {
            nextMessage(currentNanoTime);
            WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
            return;
        }
        else if (input.contains(KEYBOARD_ESCAPE)) {
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

        if (markedOption != newMarkedOption) {
            markedOption = newMarkedOption;
            WorldView.getPlayer().getActor().setLastInteraction(currentNanoTime);
        }
    }

    public void processMouse(Point2D mousePosition, boolean isMouseClicked)
    {
        isInfoButtonHovered = actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null && talkIcon.contains(mousePosition);

        if (readDialogue.type.equals(DIALOGUE_TYPE_DECISION) && GameWindow.getSingleton().isMouseMoved()) {
            for (int checkedLineIdx = 0; checkedLineIdx < fontManager.wrappedMessage.size(); checkedLineIdx++) {
                Rectangle2D positionOptionRelativeToWorldView = new Rectangle2D(xOffsetTextLine, firstLineOffsetY + checkedLineIdx * (font.getSize() + LINE_SPACE), WIDTH - OFFSET_MARKING_RIGHT, font.getSize());
                if (positionOptionRelativeToWorldView.contains(mousePosition)) {
                    if (markedOption != checkedLineIdx)
                        markedOption = checkedLineIdx;
                    break;
                }
            }
            GameWindow.getSingleton().setMouseMoved(false);
        }
        if (isMouseClicked) {
            if (isInfoButtonHovered) {
                WorldView.setPersonalityScreenController(new PersonalityScreenController(actorOfDialogue));
                WorldViewController.setWorldViewStatus(WorldViewStatus.PERSONALITY);
            }
            else if (!SCREEN_AREA.contains(mousePosition)) {
                nextMessage(GameWindow.getSingleton().getRenderTime());//Remove if next Msg should just come if you click inside the box
            }
            else
                nextMessage(GameWindow.getSingleton().getRenderTime());
        }
    }

    public void groupAnalysis(List<Actor> actorsList, Actor speakingActor)
    {
        String methodName = "groupAnalysis(List<Actor>, Actor) ";
        startConversation(speakingActor);
        for (Actor actor : actorsList) {
            Element analysisDialogueFileObserved = Utilities.readXMLFile(actor.getSpriteList().get(0).getDialogueFileName());
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

        if (readDialogue.type.equals(DIALOGUE_TYPE_DECISION)) {
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
            if (actorOfDialogue != null && actorOfDialogue.tags.contains(TURNS_DIRECTION_ONINTERACTION)) {
                actorOfDialogue.setDirection(Direction.of(actorOfDialogue.getNumeric_generic_attributes().get("previousDirection").intValue()));
                actorOfDialogue.getNumeric_generic_attributes().remove("previousDirection");
            }
        }
        playerActor.setLastInteraction(currentNanoTime);

        //for PC screen we want change after each click
        if (readDialogue.getSpriteStatus() != null) {
            changeActorStatus(readDialogue.getSpriteStatus());
        }

    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        font = FONT_ORBITRON_20;
        gc.setFont(FONT_ORBITRON_20);

        //Background
        gc.setFill(background);
        gc.setGlobalAlpha(0.9);
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetYDecorationTop - backgroundOffsetYTalkIcon - backgroundOffsetYDecorationBtm);

        if (markedOption != null && readDialogue.type.equals(DIALOGUE_TYPE_DECISION)) {
            gc.setFill(COLOR_MARKING);
            gc.fillRect(xOffsetTextLine, firstLineOffsetY + markedOption * (gc.getFont().getSize() + LINE_SPACE), WIDTH - OFFSET_MARKING_RIGHT, gc.getFont().getSize());
        }

        gc.setGlobalAlpha(1);
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY() + backgroundOffsetYTalkIcon);
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());
        gc.setFill(COLOR_FONT);

        double textLineWidth = WIDTH * 0.9;
        switch (readDialogue.type) {
            case DIALOGUE_TYPE_DECISION:
                fontManager.parseOptions(readDialogue.getOptionMessages(), font, textLineWidth);
                break;
            case DIALOGUE_TYPE_TEXT:
            case DIALOGUE_TYPE_TECHNICAL:
            default:
                String nextMessage = readDialogue.messages.get(messageIdx);
                fontManager.parseText(nextMessage, font, textLineWidth);
                break;
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        double elapsedTimeSinceLastInteraction = (GameWindow.getCurrentNanoRenderTimeGameWindow() - lastTimeNewLetterRendered) / 1000000000.0;
        if (elapsedTimeSinceLastInteraction > 0.01) {
            maxLettersIdxRendered += 2;
            lastTimeNewLetterRendered = GameWindow.getCurrentNanoRenderTimeGameWindow();
        }

        for (int idxLetter = 0; idxLetter < fontManager.lettersTo(maxLettersIdxRendered); idxLetter++) {
            char letter = fontManager.getLetterAt(idxLetter);
            Color f = fontManager.getFontAtLetter(idxLetter);
            int lineIdx = fontManager.getLineIdx(idxLetter);
            double stringOffsetX = fontManager.getLineXOffset(lineIdx, idxLetter);
            gc.setFill(f);
            gc.fillText(String.valueOf(letter),
                    Math.round(xOffsetTextLine) + stringOffsetX,
                    firstLineOffsetY + (gc.getFont().getSize() + LINE_SPACE) * lineIdx);
        }

        //Character Info Button
        if (actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null) {
            gc.drawImage(characterButton, talkIcon.getMinX(), talkIcon.getMinY());
        }
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
}
