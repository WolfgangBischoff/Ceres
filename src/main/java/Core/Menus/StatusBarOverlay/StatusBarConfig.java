package Core.Menus.StatusBarOverlay;

import javafx.beans.property.IntegerProperty;
import javafx.scene.paint.Color;

public class StatusBarConfig
{
    String imagePath;
    Color backgroundColor;
    Color fillColor;
    int WIDTH, HEIGHT, maxValue;
    IntegerProperty integerProperty;

    public StatusBarConfig(String imagePath, Color backgroundColor, Color fillColor, int WIDTH, int HEIGHT, int maxValue, IntegerProperty integerProperty)
    {
        this.imagePath = imagePath;
        this.backgroundColor = backgroundColor;
        this.fillColor = fillColor;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.maxValue = maxValue;
        this.integerProperty = integerProperty;
    }

}
