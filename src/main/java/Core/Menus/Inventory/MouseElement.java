package Core.Menus.Inventory;

import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MouseElement
{
    public Shape position;
    public String identifier;
    public Set<MouseInteractionType> reactiveTypes;

    public MouseElement(Shape position, String identifier, Set<MouseInteractionType> reactiveTypes)
    {
        this.position = position;
        this.identifier = identifier;
        this.reactiveTypes = reactiveTypes;
    }

    public MouseElement(Shape position, String identifier, MouseInteractionType reactiveTypes)
    {
        this.position = position;
        this.identifier = identifier;
        this.reactiveTypes = new HashSet<>();
        this.reactiveTypes.add(reactiveTypes);
    }

    public Shape getPosition()
    {
        return position;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public Set<MouseInteractionType> getReactiveTypes()
    {
        return reactiveTypes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MouseElement)) return false;
        MouseElement that = (MouseElement) o;
        return Objects.equals(getPosition(), that.getPosition()) &&
                Objects.equals(getIdentifier(), that.getIdentifier()) &&
                Objects.equals(getReactiveTypes(), that.getReactiveTypes());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getPosition(), getIdentifier(), getReactiveTypes());
    }
}
