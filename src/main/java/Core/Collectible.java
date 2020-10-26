package Core;

import Core.Enums.CollectableType;
import Core.Enums.Direction;
import javafx.scene.image.Image;

import static Core.Configs.Config.IMAGE_DIRECTORY_PATH;
import static Core.Configs.Config.PNG_POSTFIX;

public class Collectible
{
    String ingameName;
    String name;
    Image image;
    CollectableType type;
    int baseValue;

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
        collectible.image = new Image(IMAGE_DIRECTORY_PATH + collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0).spriteName + PNG_POSTFIX);
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
}
