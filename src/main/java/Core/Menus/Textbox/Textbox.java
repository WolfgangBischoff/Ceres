package Core.Menus.Textbox;

import Core.*;
import Core.Enums.Direction;
import Core.Menus.Personality.PersonalityScreenController;
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
import javafx.scene.text.Text;
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
    private static final Font font = FONT_ESTROG_30_DEFAULT;
    Dialogue readDialogue;
    Element dialogueFileRoot;
    int messageIdx = 0;
    int backgroundOffsetX = 16;
    int backgroundOffsetYDecorationTop = 10;
    int backgroundOffsetYTalkIcon = 50;
    int backgroundOffsetYDecorationBtm = 10;
    Color background = Color.rgb(60, 90, 85);
    final int firstLineOffsetY = (int) SCREEN_POSITION.getY() + backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon + 20;
    final int xOffsetTextLine = (int) SCREEN_POSITION.getX() + 40;
    String nextDialogueID = null;
    List<String> lineSplitMessage;
    Integer markedOption = 0;
    Actor actorOfDialogue;
    Long lastTimeNewLetterRendered = 0L;
    int maxLettersIdxRendered = 0;

    //TalkIcon
    int talkIconWidth = 280;
    int talkIconHeight = 100;
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
                        String newStatus = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_NEW_STATUS);
                        String newDialogueId = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_ID);
                        String newDialogueFile = changeDirective.getAttribute(TEXTBOX_ATTRIBUTE_DIALOGUE_FILE);
                        Actor actor = WorldView.getSingleton().getSpriteByName(id);
                        if (!newStatus.isEmpty())
                            actor.setGeneralStatus(newStatus);
                        if (!newDialogueId.isEmpty())
                            actor.setDialogueId(newDialogueId);
                        if (!newDialogueFile.isEmpty())
                            actor.setDialogueFile(newDialogueFile);
                        actor.updateCompoundStatus();
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
        isInfoButtonHovered = actorOfDialogue != null && actorOfDialogue.getPersonalityContainer() != null && talkIcon.contains(mousePosition);

        //System.out.println(CLASSNAME + methodName + mousePosition.toString());
        if (readDialogue.type.equals(DIALOGUE_TYPE_DECISION) && GameWindow.getSingleton().isMouseMoved()) {
            for (int checkedLineIdx = 0; checkedLineIdx < lineSplitMessage.size(); checkedLineIdx++) {
                Rectangle2D positionOptionRelativeToWorldView = new Rectangle2D(xOffsetTextLine, firstLineOffsetY + (checkedLineIdx * font.getSize()), WIDTH - OFFSET_MARKING_RIGHT, font.getSize());
                if (positionOptionRelativeToWorldView.contains(mousePosition)) {
                    if (markedOption != checkedLineIdx)
                        markedOption = checkedLineIdx;
                    break;
                }
            }
            GameWindow.getSingleton().setMouseMoved(false);
        }
        if (isMouseClicked)
        {
            if (isInfoButtonHovered)
            {
                WorldView.setPersonalityScreenController(new PersonalityScreenController(actorOfDialogue));
                WorldViewController.setWorldViewStatus(WorldViewStatus.PERSONALITY);
            }
            else if (!SCREEN_AREA.contains(mousePosition))
            {
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
        for (Actor actor : actorsList)
        {
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
        if (readDialogue.getSpriteStatus() != null)
        {
            changeActorStatus(readDialogue.getSpriteStatus());
        }


    }

    public void render(GraphicsContext gc) throws NullPointerException
    {
        String methodName = "render() ";
        gc.setFont(FONT_ESTROG_30_DEFAULT);

        //Background
        gc.setFill(background);
        gc.setGlobalAlpha(0.9);
        gc.fillRect(SCREEN_POSITION.getX() + backgroundOffsetX, SCREEN_POSITION.getY() + backgroundOffsetYDecorationTop + backgroundOffsetYTalkIcon, WIDTH - backgroundOffsetX * 2, HEIGHT - backgroundOffsetYDecorationTop - backgroundOffsetYTalkIcon - backgroundOffsetYDecorationBtm);

        if (markedOption != null && readDialogue.type.equals(DIALOGUE_TYPE_DECISION)) {
            gc.setFill(COLOR_MARKING);
            gc.fillRect(xOffsetTextLine, firstLineOffsetY + markedOption * gc.getFont().getSize() + 5, WIDTH - OFFSET_MARKING_RIGHT, gc.getFont().getSize());
        }

        gc.setGlobalAlpha(1);
        gc.drawImage(cornerTopLeft, SCREEN_POSITION.getX(), SCREEN_POSITION.getY() + backgroundOffsetYTalkIcon);
        gc.drawImage(cornerBtmRight, SCREEN_POSITION.getX() + WIDTH - cornerBtmRight.getWidth(), SCREEN_POSITION.getY() + HEIGHT - cornerBtmRight.getHeight());

        int yOffsetTextLine = firstLineOffsetY;
        gc.setFill(COLOR_FONT);

        switch (readDialogue.type) {
            case DIALOGUE_TYPE_DECISION:
                lineSplitMessage = readDialogue.getOptionMessages();
                break;
            case DIALOGUE_TYPE_COIN_GAME:
                WorldViewController.setWorldViewStatus(WorldViewStatus.COIN_GAME);
                lineSplitMessage = wrapText("Discussion ongoing");
                break;
            case DIALOGUE_TYPE_TECHNICAL:
                lineSplitMessage = wrapText("technical");
                break;
            default:
                String nextMessage = readDialogue.messages.get(messageIdx);
                lineSplitMessage = wrapText(nextMessage);
                break;
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        for (int lineIdx = 0; lineIdx < lineSplitMessage.size(); lineIdx++) {
            String line = lineSplitMessage.get(lineIdx);
            FontManager fontManager = new FontManager(line);
            line = FontManager.removeFontMarkings(line);
            double elapsedTimeSinceLastInteraction = (GameWindow.getCurrentNanoRenderTimeGameWindow() - lastTimeNewLetterRendered) / 1000000000.0;
            if (elapsedTimeSinceLastInteraction > 0.005) {
                maxLettersIdxRendered++;
                lastTimeNewLetterRendered = GameWindow.getCurrentNanoRenderTimeGameWindow();
            }

            int lettersRendered = Math.min(maxLettersIdxRendered, line.length());
            String visibleLine = line.substring(0, lettersRendered);

            for (int i = 0; i < visibleLine.length(); i++) {
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
            if (numberDigits + numberDigitsWithoutFontRegex > TEXT_MAX_LINE_LETTERS)
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
}
