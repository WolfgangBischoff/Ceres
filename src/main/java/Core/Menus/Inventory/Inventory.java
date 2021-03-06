package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;
import Core.CollectibleStack;
import Core.Enums.CollectableType;
import Core.WorldView.WorldView;

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

    public static Inventory getPlayerInventory()
    {
        return WorldView.getPlayer().getActor().getInventory();
    }

    public boolean hasCollectibleStackOfType(String technicalName, CollectableType type)
    {
        for (CollectibleStack c : itemsList)
            if (c.isDefined() && c.getTypes().contains(type) && c.getTechnicalName().equals(technicalName))
                return true;
        return false;
    }

    public boolean hasCollectibleStackOfTypeAndNumber(String technicalName, CollectableType type, int amount)
    {
        for (CollectibleStack c : itemsList)
            if (c.isDefined() && c.getTypes().contains(type) && c.getTechnicalName().equals(technicalName) && c.getAmount() >= amount)
                return true;
        return false;
    }

    public CollectibleStack getCollectibleStackOfTypeOfTypeAndNumber(String technicalName, CollectableType type, int amount)
    {
        for (CollectibleStack c : itemsList)
            if (c.isDefined() && c.getTypes().contains(type) && c.getTechnicalName().equals(technicalName) && c.getAmount() >= amount)
                return c;
        return CollectibleStack.empty();
    }

    public CollectibleStack getCollectibleStackOfType(String technicalName, CollectableType type)
    {
        for (CollectibleStack c : itemsList)
            if (c.isDefined() && c.getTypes().contains(type) && c.getTechnicalName().equals(technicalName))
                return c;
        return CollectibleStack.empty();
    }

    public void addCollectibleStackIdx(CollectibleStack collectible, int idx)
    {
        if (idx < MAX_IDX_ITEMS)
            itemsList.get(idx).add(collectible);
    }

    public boolean addCollectibleStackNextSlot(CollectibleStack collectible)
    {
        if (getStackOfSameType(collectible) != null)
        {
            getStackOfSameType(collectible).add(collectible);
            return true;
        }
        else if (hasFreeSlot())
        {
            addCollectibleStackIdx(collectible, nextFreeIdx());
            return true;
        }
        return false;
    }

    private CollectibleStack getStackOfSameType(CollectibleStack collectible)
    {
        for (CollectibleStack slot : itemsList)
        {
            if (slot.isDefined()
                    && slot.getCollectible().equals(collectible.getCollectible())
                    && slot.getFreeSpace() >= collectible.getAmount())
                return slot;
        }
        return null;
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
        if (!collectible.isEmpty())
            removeCollectibleStack(itemsList.indexOf(collectible));
    }

    public void reduceCollectibleStack(CollectibleStack collectible, int amount)
    {
        collectible.remove(amount);
        if (collectible.isEmpty())
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

    public void flush()
    {
        for (int i = 0; i < MAX_IDX_ITEMS; i++)
            itemsList.set(i, CollectibleStack.empty());
    }

    public void removeAll(List<CollectibleStack> collectibles)
    {
        itemsList.removeAll(collectibles);
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        itemsList.forEach(s ->
        {
            if (s.isDefined())
                stringBuilder.append(s.getCollectible().getSpriteStatus() + " " + s.getAmount());
        });
        return owner.getActorInGameName() +
                " " + stringBuilder.toString()
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
