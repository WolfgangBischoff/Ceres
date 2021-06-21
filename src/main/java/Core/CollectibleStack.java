package Core;

import Core.Enums.CollectableType;
import Core.Enums.Direction;
import Core.Sprite.Sprite;
import javafx.scene.image.Image;

import static Core.Configs.Config.GENERIC_STACK_AMOUNT;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class CollectibleStack
{
    int maxNumber;
    private int number;
    Collectible collectible;

    public static CollectibleStack empty()
    {
        return  new CollectibleStack();
    }

    public Sprite createSprite(int x, int y)
    {
        Actor collectibleActor = collectible.actor;
        Sprite sprite = Sprite.createSprite(collectibleActor.getSpriteDataMap().get(collectibleActor.generalStatus).get(0), x, y);
        sprite.setActor(collectibleActor);
        collectibleActor.addSprite(sprite);
        collectibleActor.genericDoubleAttributes.put(GENERIC_STACK_AMOUNT, (double)number);
        return sprite;
    }

    public CollectibleStack()
    {
        this.maxNumber = 5;
        this.number = 0;
        this.collectible = null;
    }

    public CollectibleStack(Collectible collectible)
    {
        this.maxNumber = 5;
        this.number = 1;
        this.collectible = collectible;
    }

    public CollectibleStack(Collectible collectible, int initNumber)
    {
        this.maxNumber = 5;
        this.number = initNumber;
        this.collectible = collectible;
    }


    public boolean add(Collectible addedCollectible)
    {
        if(collectible == null)
        {
            collectible = addedCollectible;
            return true;
        }
        else if (addedCollectible.equals(collectible))
        {
            number++;
            return true;
        }
        return false;
    }
    public void add(CollectibleStack addedCollectible)
    {
        if (!addedCollectible.isEmpty() && addedCollectible.collectible.equals(collectible))
        {
            number += addedCollectible.number;
        }
        else
        {
            collectible = addedCollectible.collectible;
            number = addedCollectible.number;
        }
    }

    public boolean isEmpty()
    {
        return collectible == null;
    }

    public boolean isDefined()
    {
        return !(collectible == null);
    }

    public int remove(int amount)
    {
        int reducedBy = min(number, amount);
        number -= reducedBy;
        if(number <= 0)
            collectible = null;
        return reducedBy;
    }

    public CollectibleStack split(int amount)
    {
        int numberToTransfer = remove(amount);
        return new CollectibleStack(collectible, numberToTransfer);
    }

    public void transferTo(CollectibleStack target, int amount)
    {
        int numberTotransfer = min(amount, number);
        number -= numberTotransfer;
        target.add(split(numberTotransfer));
        if (number == 0)
            collectible = null;
    }

    public String getIngameName()
    {
        if(collectible != null)
        return collectible.ingameName;
        else
            return null;
    }

    public String getTechnicalName()
    {
        if (collectible != null)
            return collectible.spriteStatus;
        else return null;
    }

    public int getNumber()
    {
        return number;
    }

    public Image getImage()
    {
        if(collectible != null)
        return collectible.image;
        else return null;
    }

    public CollectableType getType()
    {
        if (collectible != null)
            return collectible.type;
        else
            return null;
    }

    public int getBaseValue()
    {
        if(collectible != null)
        return collectible.baseValue;
        return 0;
    }

    public String getDescription()
    {
        if(collectible != null)
        return collectible.description;
        else
            return null;
    }

    public Integer getId()
    {
        if (collectible != null)
            return collectible.id;
        else
            return null;

    }

    public Collectible getCollectible()
    {
        return collectible;
    }
}
