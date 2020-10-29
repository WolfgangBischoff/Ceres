package Core.Menus.DiscussionGame;

import Core.Utilities;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Menus.DiscussionGame.CharacterCoinBuff.BUFF_SLOWED;

public class CharacterCoin
{
    static private String CLASSNAME = "CharacterCoin/";
    Circle collisionCircle;
    Image image;
    CoinType type;
    Point2D startPosition;
    double collisionRadius;
    double initSpeed = 2;
    String movementType;
    int time_spawn;
    int time_max = COIN_DEFAULT_MAX_TIME;
    long lastTimeUpdated;
    double speed = 0;
    double buffSpeedFactor = 1;
    double angle = 0;
    Map<String, Double> genericVariables = new HashMap<>();

    public CharacterCoin(Element xmlNode)
    {
        type = CoinType.of(xmlNode.getAttribute("characteristic"));
        if (xmlNode.hasAttribute(COIN_TAG_INITSPEED))
        {
            initSpeed = Integer.parseInt(xmlNode.getAttribute(COIN_TAG_INITSPEED));
            speed = initSpeed;
        }
        movementType = xmlNode.getAttribute("movementType").toLowerCase();
        time_spawn = Integer.parseInt(xmlNode.getAttribute("time"));
        image = findImage(type.toString());
        int startX = Integer.parseInt(xmlNode.getAttribute("x"));
        int startY = Integer.parseInt(xmlNode.getAttribute("y"));
        collisionRadius = this.image.getWidth() / 2;
        startPosition = new Point2D(startX, startY);
        collisionCircle = new Circle(startPosition.getX(), startPosition.getY(), collisionRadius);

        if (xmlNode.hasAttribute(COIN_ATTRIBUTE_MAX_TIME))
            time_max = Integer.parseInt(xmlNode.getAttribute(COIN_ATTRIBUTE_MAX_TIME));


        if (movementType.equals(COIN_BEHAVIOR_MOVING))
        {
            angle = Integer.parseInt(xmlNode.getAttribute(COIN_TAG_ANGLE));
        }
        else if (movementType.equals(COIN_BEHAVIOR_CIRCLE) || movementType.equals(COIN_BEHAVIOR_SPIRAL))
        {
            Double centrumX = Double.parseDouble(xmlNode.getAttribute("centrumx"));
            Double centrumY = Double.parseDouble(xmlNode.getAttribute("centrumy"));
            Double startangle = Double.parseDouble(xmlNode.getAttribute("startangle"));
            Double radius = Double.parseDouble(xmlNode.getAttribute("radius"));
            Double isclockwise = Double.parseDouble(xmlNode.getAttribute("isclockwise"));
            genericVariables.put("centrumx", centrumX);
            genericVariables.put("centrumy", centrumY);
            genericVariables.put("startangle", startangle);
            genericVariables.put("radius", radius);
            genericVariables.put("isclockwise", isclockwise);
        }
    }

    public static Image findImage(String characteristicOrTrait)
    {
        switch (characteristicOrTrait.toLowerCase())
        {
            case "introversion":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/introvert.png");
            case "extroversion":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/extrovert.png");
            case "intuition":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/intuition.png");
            case "sensing":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/sensing.png");
            case "thinking":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/thinking.png");
            case "feeling":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/feeling.png");
            case "judging":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/judging.png");
            case "perceiving":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/perceiving.png");

            case "buff_slowed":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/buff_slowed.png");
            case "buff_double_reward":
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/buff_double_reward.png");

            default:
                return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/unknown.png");
        }
    }


    @Override
    public String toString()
    {
        return
                "collisionCircle=" + collisionCircle +
                        ", image=" + image +
                        ", characteristic=" + type +
                        ", startPosition=" + startPosition +
                        ", collisionRadius=" + collisionRadius +
                        ", initSpeed=" + initSpeed +
                        ", movementType='" + movementType + '\'' +
                        '}';
    }

    public void move(Long elapsedCoinGameTime)
    {
        String methodName = "move() ";
        if (lastTimeUpdated == 0)
            lastTimeUpdated = elapsedCoinGameTime;
        double elapsedTimeSinceLastUpdate = ((elapsedCoinGameTime - lastTimeUpdated) / 1000000000.0);
        double deltaX = 0, deltaY = 0;
        if (DiscussionGame.getActiveBuffs().containsKey(BUFF_SLOWED.toString()))
            buffSpeedFactor = 0.5;
        else
            buffSpeedFactor = 1;

        switch (movementType)
        {
            case COIN_BEHAVIOR_MOVING:
            {
                //tan(a) = Gegenkathete / Ankathete
                //sin(a) = Gegenkathete / Hypotenuse
                //cos(a) = Ankathete    / Hypotenuse
                //0     => right
                //45    => btm right

                double hypotenuse = initSpeed;
                double angle_rad = Math.toRadians(angle);
                double x = Math.cos(angle_rad) * hypotenuse;
                double y = Math.sin(angle_rad) * hypotenuse;

                deltaX = x * buffSpeedFactor;
                deltaY = y * buffSpeedFactor;
                break;
            }
            case COIN_BEHAVIOR_JUMP:
                double slowFactor = -5 * buffSpeedFactor;
                speed = slowFactor * elapsedTimeSinceLastUpdate + speed;
                deltaY = -speed * buffSpeedFactor;
                break;
            case COIN_BEHAVIOR_CIRCLE:
            case COIN_BEHAVIOR_SPIRAL:
            {
                double angle = genericVariables.get("startangle");
                double radius = genericVariables.get("radius");
                double centrumX = genericVariables.get("centrumx");
                double centrumY = genericVariables.get("centrumy");
                double isclockwise = genericVariables.get("isclockwise");

                if (isclockwise == 1)
                    angle += speed * buffSpeedFactor;
                else
                    angle -= speed * buffSpeedFactor;

                genericVariables.put("startangle", angle);
                if (movementType.equals(COIN_BEHAVIOR_SPIRAL))
                    genericVariables.put("radius", radius + elapsedTimeSinceLastUpdate * 50 * buffSpeedFactor);
                double angle_rad = Math.toRadians(angle);
                double x = Math.cos(angle_rad) * radius;
                double y = Math.sin(angle_rad) * radius;
                deltaX = x - collisionCircle.getCenterX() + centrumX;
                deltaY = y - collisionCircle.getCenterY() + centrumY;
                break;
            }
        }

        lastTimeUpdated = elapsedCoinGameTime;
        collisionCircle.setCenterX(collisionCircle.getCenterX() + deltaX);
        collisionCircle.setCenterY(collisionCircle.getCenterY() + deltaY);

    }
}
