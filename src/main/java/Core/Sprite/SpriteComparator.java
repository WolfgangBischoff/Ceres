package Core.Sprite;

import Core.Sprite.Sprite;

import java.util.Comparator;

public class SpriteComparator implements Comparator<Sprite>
{
    final static String CLASSNAME = "SpriteComparator/";

    @Override
    public int compare(Sprite object1, Sprite object2)
    {
        String methodName = "compare() ";
        return Double.compare(object1.getPositionY() + object1.getHitBoxOffsetY(), object2.getPositionY() + object2.getHitBoxOffsetY());
    }
}
