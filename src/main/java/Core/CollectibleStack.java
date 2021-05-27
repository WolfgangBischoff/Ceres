package Core;

import Core.Enums.CollectableType;
import javafx.scene.image.Image;

public class CollectibleStack
{
    int maxNumber;
    int number;
    Collectible collectible;

    public static CollectibleStack empty()
    {
        return  new CollectibleStack();
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

    public void remove()
    {
        if (number > 0)
            number--;
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
            return collectible.technicalName;
        else return null;
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
