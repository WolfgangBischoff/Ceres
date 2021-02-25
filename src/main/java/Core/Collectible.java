package Core;

import Core.Enums.CollectableType;
import Core.Enums.Direction;
import Core.Menus.Textbox.Dialogue;
import javafx.scene.image.Image;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static Core.Configs.Config.*;

public class Collectible
{
    static final String CLASSNAME = "Collectible";
    String ingameName;
    String name;
    Image image;
    CollectableType type;
    int baseValue;
    String description = "";

    public Collectible(String name, CollectableType type, String nameGame, int baseValue)
    {
        this.name = name;
        this.type = type;
        this.ingameName = nameGame;
        this.baseValue = baseValue;
    }

    public static Collectible createCollectible(String actorfilepath, String ingameName, String spriteStatus)
    {
        Actor collectibleActor = new Actor(actorfilepath, ingameName, spriteStatus, "default", Direction.UNDEFINED);
        Collectible collectible = new Collectible(ingameName, CollectableType.getType(collectibleActor.getCollectable_type()), collectibleActor.actorInGameName, (collectibleActor.getNumeric_generic_attributes().get("base_value").intValue()));
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
            System.out.println(CLASSNAME + " create Collectible() Path does not exist: " + path);

        return collectible;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Collectible)) return false;
        Collectible that = (Collectible) o;
        return name.equals(that.name) &&
                type == that.type;
    }

    @Override
    public String toString()
    {
        return "{" + name + " " + type +  " value: " + baseValue + "}";
    }

    public String getIngameName()
    {
        return ingameName;
    }

    public String getName()
    {
        return name;
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
}
