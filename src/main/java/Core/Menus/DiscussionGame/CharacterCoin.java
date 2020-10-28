package Core.Menus.DiscussionGame;

import Core.Utilities;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static Core.Configs.Config.*;
import static Core.Configs.Config.COIN_BEHAVIOR_CIRCLE;
import static Core.Configs.Config.COIN_BEHAVIOR_JUMP;
import static Core.Configs.Config.COIN_BEHAVIOR_MOVING;
import static Core.Configs.Config.COIN_BEHAVIOR_SPIRAL;
import static Core.Configs.Config.COIN_TAG_ANGLE;
import static Core.Configs.Config.COIN_TAG_INITSPEED;
import static Core.Menus.DiscussionGame.CharacterCoinBuff.SLOWED;

public class CharacterCoin
{
    static private String CLASSNAME = "CharacterCoin/";
    Circle collisionCircle;
    Image image;
    //PersonalityTrait characteristic;
    CoinType type;
    Point2D startPosition;
    double collisionRadius;
    double initSpeed = 2;
    String movementType;
    int time_spawn;
    int time_max = COIN_DEFAULT_MAX_TIME;
    //Circle circle;

    //Jump
    double speed = 0;

    //Move
    double angle = 0;

    Map<String, Double> genericVariables = new HashMap<>();


//    //Maybe for traits??
//    public CharacterCoin(PersonalityTrait characteristic, Point2D startPosition, int collisionRadius, int speed, String movementType, int time)
//    {
//        this.collisionRadius = collisionRadius;
//        this.image = findImage(characteristic.toString());
//        this.characteristic = characteristic;
//        this.startPosition = startPosition;
//        this.initSpeed = speed;
//        this.movementType = movementType;
//        collisionCircle = new Circle(startPosition.getX(),startPosition.getY(),collisionRadius);
//        this.time_spawn = time;
//    }

    public CharacterCoin(Element xmlNode)
    {
        //this.characteristic = PersonalityTrait.of(xmlNode.getAttribute("characteristic"));
        this.type = CoinType.of(xmlNode.getAttribute("characteristic"));
        if(xmlNode.hasAttribute(COIN_TAG_INITSPEED))
            this.initSpeed = Integer.parseInt(xmlNode.getAttribute(COIN_TAG_INITSPEED));
        this.movementType = xmlNode.getAttribute("movementType").toLowerCase();
        this.time_spawn = Integer.parseInt(xmlNode.getAttribute("time"));
        this.image = findImage(type.toString());
        int startX = Integer.parseInt(xmlNode.getAttribute("x"));
        int startY = Integer.parseInt(xmlNode.getAttribute("y"));
        this.collisionRadius = this.image.getWidth() / 2;
        this.startPosition = new Point2D(startX,startY);
        collisionCircle = new Circle(startPosition.getX(),startPosition.getY(),collisionRadius);

        if(xmlNode.hasAttribute(COIN_ATTRIBUTE_MAX_TIME))
            time_max = Integer.parseInt(xmlNode.getAttribute(COIN_ATTRIBUTE_MAX_TIME));

        if(movementType.equals(COIN_BEHAVIOR_JUMP))
        {
            //this.speed = Integer.parseInt(xmlNode.getAttribute("relative_jumpheight"));
        }
        else if(movementType.equals(COIN_BEHAVIOR_MOVING))
        {
            this.angle = Integer.parseInt(xmlNode.getAttribute(COIN_TAG_ANGLE));
        }
        else if(movementType.equals(COIN_BEHAVIOR_CIRCLE) || movementType.equals(COIN_BEHAVIOR_SPIRAL))
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
            case "introversion":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/introvert.png");
            case "extroversion":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/extrovert.png");
            case "intuition":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/intuition.png");
            case "sensing":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/sensing.png");
            case "thinking":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/thinking.png");
            case "feeling":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/feeling.png");
            case "judging":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/judging.png");
            case "perceiving":return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/perceiving.png");

            default: return Utilities.readImage("../../../" + COINGAME_DIRECTORY_PATH + "img/unknown.png");
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
        double elapsedTimeSinceSpawn = (elapsedCoinGameTime / 1000000000.0) - time_spawn;
        double deltaX = 0, deltaY = 0;
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

                deltaX = x;
                deltaY = y;
                break;
            }
            case COIN_BEHAVIOR_JUMP:
                double slowFactor = -5;
                speed = slowFactor * elapsedTimeSinceSpawn + initSpeed;
                deltaY = -speed;
                break;
            case COIN_BEHAVIOR_SPIRAL:
            {
                double angle = genericVariables.get("startangle");
                double radius = genericVariables.get("radius");
                double centrumX = genericVariables.get("centrumx");
                double centrumY = genericVariables.get("centrumy");
                double isclockwise = genericVariables.get("isclockwise");

                if (isclockwise == 1)
                    angle += initSpeed;
                else
                    angle -= initSpeed;
                genericVariables.put("startangle", angle);
                double angle_rad = Math.toRadians(angle);
                double x = Math.cos(angle_rad) * radius * elapsedTimeSinceSpawn;
                double y = Math.sin(angle_rad) * radius * elapsedTimeSinceSpawn;
                deltaX = x - collisionCircle.getCenterX() + centrumX;
                deltaY = y - collisionCircle.getCenterY() + centrumY;
                break;
            }
            case COIN_BEHAVIOR_CIRCLE:
            {
                double angle = genericVariables.get("startangle");
                double radius = genericVariables.get("radius");
                double centrumX = genericVariables.get("centrumx");
                double centrumY = genericVariables.get("centrumy");
                double isclockwise = genericVariables.get("isclockwise");

                if (isclockwise == 1)
                    angle += initSpeed;
                else
                    angle -= initSpeed;
                genericVariables.put("startangle", angle);
                double angle_rad = Math.toRadians(angle);
                double x = Math.cos(angle_rad) * radius ;
                double y = Math.sin(angle_rad) * radius ;
                deltaX = x - collisionCircle.getCenterX() + centrumX;
                deltaY = y - collisionCircle.getCenterY() + centrumY;
                break;
            }
        }

        if(DiscussionGame.getActiveBuffs().contains(SLOWED))
        {
            System.out.println(CLASSNAME + methodName + "slowed down " + deltaX + " " + deltaX / 2 );
            deltaX /= 2;
            deltaY /= 2;
//            System.out.println(CLASSNAME + methodName + time_max);
//            time_max += (time_max - elapsedTimeSinceSpawn) * 2;
//            System.out.println(CLASSNAME + methodName + "new: " + time_max);
        }

        collisionCircle.setCenterX(collisionCircle.getCenterX() + deltaX);
        collisionCircle.setCenterY(collisionCircle.getCenterY() + deltaY);

    }
}
