package Core.Menus.Inventory;

import Core.Actor;
import Core.Collectible;

import java.util.ArrayList;
import java.util.List;

public class Inventory
{
    private static final String CLASSNAME = "Inventory/";
    List<Collectible> itemsList = new ArrayList<>();
    Actor owner;
    private Integer MAX_IDX_ITEMS = 29;

    public Inventory(Actor owner)
    {
        this.owner = owner;
        for(int i=0; i<MAX_IDX_ITEMS;i++)
            itemsList.add(null);
    }

    public void addItemIdx(Collectible collectible, int idx)
    {
        String methodName = "addItem() ";
        if (idx <= MAX_IDX_ITEMS)
            itemsList.add(idx, collectible);
    }

    public boolean addItemNextSlot(Collectible collectible)
    {
        if(hasFreeSlot())
        {
            itemsList.add(nextFreeIdx(), collectible);
            return true;
        }
        return false;
    }

    public void removeItem(Collectible collectible)
    {
        String methodName = "removeItem(String, String) ";
        boolean debug = false;
        itemsList.remove(collectible);
    }


    public boolean contains(Collectible toCheck)
    {
        return itemsList.contains(toCheck);
    }

    public void removeAll(List<Collectible> collectibles)
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

    public List<Collectible> getItemsList()
    {
        return itemsList;
    }

    public int size()
    {
        return itemsList.size();
    }



    public boolean hasFreeSlot()
    {
        for(int i=0; i<MAX_IDX_ITEMS;i++)
            if(itemsList.get(i) == null)
            {
                return true;
            }
        return false;
    }

    public int nextFreeIdx()
    {
        for(int i=0; i<itemsList.size();i++)
            if(itemsList.get(i) == null)
            {
                return i;
            }
        throw new RuntimeException("No free Inventory Slots, check before");
    }
}
