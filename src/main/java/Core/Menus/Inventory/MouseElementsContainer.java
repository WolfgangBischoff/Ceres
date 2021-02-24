package Core.Menus.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MouseElementsContainer
{
    private List<MouseElement> mouseElements = new ArrayList<>();

    public boolean add(MouseElement mouseElement)
    {
        return mouseElements.add(mouseElement);
    }

    public MouseElement get(String id)
    {
        for (int i = 0; i < mouseElements.size(); i++)
            if (mouseElements.get(i).identifier.equals(id))
                return mouseElements.get(i);

        return null;
    }

    public MouseElement get(int i)
    {
        return mouseElements.get(i);
    }

    public int indexOf(MouseElement mouseElement)
    {
        return mouseElements.indexOf(mouseElement);
    }

    public int size()
    {
        return mouseElements.size();
    }
}
