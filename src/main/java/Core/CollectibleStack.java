package Core;

import Core.Enums.CollectableType;
import Core.Sprite.Sprite;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;

import static Core.Configs.Config.GENERIC_STACK_AMOUNT;
import static java.lang.Integer.min;

public class CollectibleStack
{
    int maxAmount;
    private int amount;
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
        collectibleActor.genericDoubleAttributes.put(GENERIC_STACK_AMOUNT, (double) amount);
        return sprite;
    }

    public CollectibleStack()
    {
        this.maxAmount = 5;
        this.amount = 0;
        this.collectible = null;
    }

    public CollectibleStack(Collectible collectible)
    {
        this.maxAmount = 5;
        this.amount = 1;
        this.collectible = collectible;
    }

    public CollectibleStack(Collectible collectible, int initNumber)
    {
        this.maxAmount = 5;
        this.amount = initNumber;
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
            amount++;
            return true;
        }
        return false;
    }
    public void add(CollectibleStack addedCollectible)
    {
        if (!addedCollectible.isEmpty() && addedCollectible.collectible.equals(collectible))
        {
            amount += addedCollectible.amount;
        }
        else
        {
            collectible = addedCollectible.collectible;
            amount = addedCollectible.amount;
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
        int reducedBy = min(this.amount, amount);
        this.amount -= reducedBy;
        if(this.amount <= 0)
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
        int numberTotransfer = min(amount, this.amount);
        this.amount -= numberTotransfer;
        target.add(split(numberTotransfer));
        if (this.amount == 0)
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

    public int getAmount()
    {
        return amount;
    }

    public Image getImage()
    {
        if(collectible != null)
        return collectible.image;
        else return null;
    }

    public Set<CollectableType> getTypes()
    {
        if (collectible != null)
            return collectible.type;
        else
            return new HashSet<>();
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
