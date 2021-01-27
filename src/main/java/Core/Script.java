package Core;

import Core.Enums.Direction;
import javafx.geometry.Point2D;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.Queue;

import static Core.Utilities.randomDirection;

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
    int IDLE_WAITING_TIME = 3;
    int IDLE_MOVE_TIME = 2;
    ScriptType type;
    Queue<Point2D> route = new LinkedList<>();
    Long idleLastStatusChangeTime = 0L;
    String idleStatus = "NOT SET";
    Direction randomDirection = null;


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
                break;
            case REPEAT:
                repeat(actor);
                break;
            case IDLE:
                idle(actor);
                break;
        }
    }

    private void idle(Actor actor)
    {
        String methodName = "idle() ";
        long currentTime = GameWindow.getCurrentNanoRenderTimeGameWindow();
        double elapsedTime = (currentTime - idleLastStatusChangeTime) / 1000000000.0;

        if (idleStatus.equals("MOVE") && elapsedTime > IDLE_MOVE_TIME)
        {
            idleLastStatusChangeTime = currentTime;
            idleStatus = "WAIT";
        }
        else if (idleStatus.equals("WAIT") && elapsedTime > IDLE_WAITING_TIME)
        {
            idleLastStatusChangeTime = currentTime;
            idleStatus = "MOVE";
        }

        if (idleStatus.equals("MOVE"))
        {
            Point2D currentPos = new Point2D(actor.getSpriteList().get(0).getX(), actor.getSpriteList().get(0).getY());
            if (randomDirection == null)
            {
                randomDirection = randomDirection();
            }
            switch (randomDirection)
            {
                case EAST:
                    actor.setVelocity(60, 0);
                    break;
                case WEST:
                    actor.setVelocity(-60, 0);
                    break;
                case NORTH:
                    actor.setVelocity(0, -60);
                    break;
                case SOUTH:
                    actor.setVelocity(0, 60);
                    break;
            }
        }
        else
        {
            idleStatus = "WAIT";
            actor.setVelocity(0, 0);
            if (randomDirection != null)
            {
                randomDirection = null;
            }
        }

    }

    private void repeat(Actor actor)
    {
        String methodName = "repeat() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek();
        boolean reachedTarget = moveUnchecked(actor, target);
        if (reachedTarget)
            route.add(route.remove());
    }

    private void route(Actor actor)
    {
        String methodName = "route() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek();
        boolean reachedTarget = moveUnchecked(actor, target);
        if (reachedTarget)
            route.remove(target);
//        System.out.println(CLASSNAME + methodName + target.getX() + " " + target.getY());
    }

    private boolean moveUnchecked(Actor actor, Point2D target)
    {
        Point2D currentPos = new Point2D(actor.spriteList.get(0).getX(), actor.spriteList.get(0).getY());
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
        }
        else if (deltaX > moveThreshold)
        {
            addedVelocityX = velocity;
        }
        else xreached = true;

        if (deltaY < -moveThreshold)
        {
            addedVelocityY = -velocity;
        }
        else if (deltaY > moveThreshold)
        {
            addedVelocityY = velocity;
        }
        else yreached = true;

        actor.setVelocity(addedVelocityX, addedVelocityY);

        if (xreached && yreached)
            return true;
        else
            return false;
    }
}
