package Core.Menus.StatusOverlay;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class BarStatusConfig
{
    String imagePath;
    Color backgroundColor;
    Color fillColor;
    int WIDTH, HEIGHT, maxValue;
    IntegerProperty integerProperty;
    Point2D SCREEN_POSITION;

    public BarStatusConfig(String imagePath, Color backgroundColor, Color fillColor, int WIDTH, int HEIGHT, int maxValue, IntegerProperty integerProperty, Point2D SCREEN_POSITION)
    {
        this.imagePath = imagePath;
        this.backgroundColor = backgroundColor;
        this.fillColor = fillColor;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.maxValue = maxValue;
        this.integerProperty = integerProperty;
        this.SCREEN_POSITION = SCREEN_POSITION;
    }

}
