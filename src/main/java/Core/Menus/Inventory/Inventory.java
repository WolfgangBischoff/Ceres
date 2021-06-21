package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.CollectibleStack;
import Core.Enums.CollectableType;

import java.util.ArrayList;
import java.util.List;

public class Inventory
{
    private static final String CLASSNAME = "Inventory/";
    private final Integer MAX_IDX_ITEMS = 30;
    List<CollectibleStack> itemsList = new ArrayList<>();
    Actor owner;

    public Inventory(Actor owner)
    {
        this.owner = owner;
        for (int i = 0; i < MAX_IDX_ITEMS; i++)
            itemsList.add(CollectibleStack.empty());
    }

    public boolean hasCollectibleStackOfType(String technicalName, CollectableType type)
    {
        for (CollectibleStack c : itemsList)
            if (c != null && c.getType() == type && c.getTechnicalName().equals(technicalName))
                return true;
        return false;
    }

    public void addCollectibleStackIdx(CollectibleStack collectible, int idx)
    {
        if (idx < MAX_IDX_ITEMS)
            itemsList.get(idx).add(collectible);
    }

    public boolean addCollectibleStackNextSlot(CollectibleStack collectible)
    {
        if (hasFreeSlot())
        {
            addCollectibleStackIdx(collectible, nextFreeIdx());
            return true;
        }
        return false;
    }

    public boolean addNumberOfCollectibleNextSlot(CollectibleStack from, int number)
    {
        if (hasFreeSlot())
        {
            addCollectibleStackIdx(from.split(number), nextFreeIdx());
            return true;
        }
        return false;
    }

    public void removeCollectibleStack(CollectibleStack collectible)
    {
        if(!collectible.isEmpty())
            removeCollectibleStack(itemsList.indexOf(collectible));
    }

    public void removeCollectibleStack(int idx)
    {
        if (idx >= 0)
            itemsList.set(idx, CollectibleStack.empty());
    }

    public CollectibleStack getCollectibeStack(int idx)
    {
        return itemsList.get(idx);
    }

    public boolean contains(Collectible toCheck)
    {
        return itemsList.contains(toCheck);
    }

    public void removeAll(List<CollectibleStack> collectibles)
    {
        itemsList.removeAll(collectibles);
    }

    @Override
    public String toString()
    {
        return owner.getActorInGameName() +
                " inv: " + itemsList.toString()
                ;
    }

    public List<CollectibleStack> getItemsList()
    {
        return itemsList;
    }

    public int size()
    {
        return itemsList.size();
    }


    public boolean hasFreeSlot()
    {
        for (int i = 0; i < MAX_IDX_ITEMS; i++)
            if (itemsList.get(i).isEmpty())
            {
                return true;
            }
        return false;
    }

    public int nextFreeIdx()
    {
        for (int i = 0; i < itemsList.size(); i++)
            if (itemsList.get(i).isEmpty())
            {
                return i;
            }
        throw new RuntimeException("No free Inventory Slots, check before");
    }
}
