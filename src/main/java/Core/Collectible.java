package Core;

import Core.Enums.CollectableType;
import Core.Enums.Direction;
import Core.Menus.Textbox.Dialogue;
import javafx.scene.image.Image;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Objects;

import static Core.Configs.Config.*;

public class Collectible
{
    static final String CLASSNAME = "Collectible";
    static int collectibleNextId = 0;
    int id;
    String ingameName;
    String technicalName;
    Image image;
    CollectableType type;
    int baseValue;
    String description = "";

    public Collectible(String technicalName, CollectableType type, String nameGame, int baseValue)
    {
        this.technicalName = technicalName;
        this.type = type;
        this.ingameName = nameGame;
        this.baseValue = baseValue;
        id = collectibleNextId++;
    }

    public static Collectible createCollectible(String actorfilepath, String spriteStatus)
    {
        Actor collectibleActor = new Actor(actorfilepath, spriteStatus, spriteStatus, "default", Direction.UNDEFINED);
        String ingameName = collectibleActor.getSpriteDataMap().get(spriteStatus.toLowerCase()).get(0).name;
        Collectible collectible = new Collectible(spriteStatus, CollectableType.getType(collectibleActor.getCollectable_type()), ingameName, (collectibleActor.getGenericDoubleAttributes().get("base_value").intValue()));
        collectible.image = Utilities.readImage(collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0).spriteName + PNG_POSTFIX);

        String path = collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0).dialogieFile;
        if(Utilities.doesXMLFileExist(path))
        {
            Element dialogueFileRoot = Utilities.readXMLFile(collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0).dialogieFile);
            String descId = collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0).dialogueID;
            NodeList dialogues = dialogueFileRoot.getElementsByTagName(DIALOGUE_TAG);
            for(int i = 0; i<dialogues.getLength(); i++)
            {
                if (((Element) dialogues.item(i)).getAttribute(ID_TAG).equals(descId)) {
                    Element currentDialogueXML = ((Element) dialogues.item(i));
                    Dialogue readDialogue = new Dialogue(collectibleActor, currentDialogueXML);
                    collectible.description = readDialogue.getMessages().get(0);
                }
            }
        }
        else
            if(!path.equals("dialogueFile") && !path.equals("none"))
                System.out.println(CLASSNAME + " create Collectible() Path does not exist: " + path);

        return collectible;
    }

    @Override
    public String toString()
    {
        return "{" + technicalName + " " + type +  " value: " + baseValue + "}";
    }

    public String getIngameName()
    {
        return ingameName;
    }

    public String getTechnicalName()
    {
        return technicalName;
    }

    public Image getImage()
    {
        return image;
    }

    public CollectableType getType()
    {
        return type;
    }

    public int getBaseValue()
    {
        return baseValue;
    }

    public String getDescription()
    {
        return description;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Collectible)) return false;
        Collectible that = (Collectible) o;
        return Objects.equals(getIngameName(), that.getIngameName()) &&
                Objects.equals(getTechnicalName(), that.getTechnicalName()) &&
                getType() == that.getType();
    }

}
