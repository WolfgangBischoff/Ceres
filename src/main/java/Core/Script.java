package Core;

import Core.Enums.Direction;
import javafx.geometry.Point2D;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.Queue;

enum ScriptType
{
    ROUTE, REPEAT, IDLE, SCHEDULE, REACT;

    public static ScriptType of(String s)
    {
        switch (s.toUpperCase())
        {
            case "ROUTE":
                return ROUTE;
            case "REPEAT":
                return REPEAT;
            case "IDLE":
                return IDLE;
            //case "SCHEDULE": return SCHEDULE;
            //case "REACT": return REACT;
            default:
                throw new RuntimeException("ScriptType unkown: " + s);
        }
    }
}

public class Script
{
    String CLASSNAME = "Script/";
    int IDLE_WATING_TIME = 2;
    ScriptType type;
    Queue<Point2D> route = new LinkedList<>();
    Long beginIdleTime = 0L;


    public Script(Element xmlFile)
    {
        type = ScriptType.of(xmlFile.getAttribute("type"));
        var routePoints = xmlFile.getElementsByTagName("point");
        for (int i = 0; i < routePoints.getLength(); i++)
        {
            int x = Integer.parseInt(((Element) routePoints.item(i)).getAttribute("x"));
            int y = Integer.parseInt(((Element) routePoints.item(i)).getAttribute("y"));
            route.add(new Point2D(x, y));
        }
        //System.out.println(CLASSNAME + type);
        //System.out.println(CLASSNAME + route.toString());
    }

    public void update(Actor actor)
    {
        switch (type)
        {
            case ROUTE:
                route(actor);
            case REPEAT:
                repeat(actor);
            case IDLE:
                idle(actor);
        }
    }

    private void idle(Actor actor)
    {
        String methodName = "idle() ";
        double elapsedTime = (GameWindow.getCurrentNanoRenderTimeGameWindow() - beginIdleTime) / 1000000000.0;
        if (elapsedTime > IDLE_WATING_TIME)
        {
            if (route.isEmpty())
            {
                Point2D currentPos = new Point2D(actor.getSpriteList().get(0).positionX, actor.getSpriteList().get(0).positionY);
                double deltaX = Utilities.randomSignedInt(0,150);
                double deltaY = Utilities.randomSignedInt(0,150);
                route.add(new Point2D(currentPos.getX() + deltaX, currentPos.getY() + deltaY));
            }
            route(actor);
            if (route.isEmpty())
            {
                beginIdleTime = GameWindow.getCurrentNanoRenderTimeGameWindow();
            }
        }


    }

    private void repeat(Actor actor)
    {
        String methodName = "repeat() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek();
        boolean reachedTarget = move(actor, target);
        if (reachedTarget)
            route.add(route.remove());
    }

    private void route(Actor actor)
    {
        String methodName = "route() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek();
        boolean reachedTarget = move(actor, target);
        if (reachedTarget)
            route.remove(target);
//        System.out.println(CLASSNAME + methodName + target.getX() + " " + target.getY());
    }

    private boolean move(Actor actor, Point2D target)
    {
        Point2D currentPos = new Point2D(actor.spriteList.get(0).positionX, actor.spriteList.get(0).positionY);
        double deltaX = target.getX() * 64 - currentPos.getX();
        double deltaY = target.getY() * 64 - currentPos.getY();
        double velocity = 80d;
        double addedVelocityX = 0d;
        double addedVelocityY = 0d;
        double moveThreshold = 5d;
        boolean xreached = false, yreached = false;

        if (deltaX < -moveThreshold)
        {
            addedVelocityX = -velocity;
            actor.setDirection(Direction.WEST);
        }
        else if (deltaX > moveThreshold)
        {
            addedVelocityX = velocity;
            actor.setDirection(Direction.EAST);
        }
        else xreached = true;

        if (deltaY < -moveThreshold)
        {
            addedVelocityY = -velocity;
            actor.setDirection(Direction.NORTH);
        }
        else if (deltaY > moveThreshold)
        {
            addedVelocityY = velocity;
            actor.setDirection(Direction.SOUTH);
        }
        else yreached = true;

        actor.setVelocity(addedVelocityX, addedVelocityY);

        if (xreached && yreached)
            return true;
        else
            return false;
    }
}
