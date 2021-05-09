package Core;

import Core.Enums.Direction;
import Core.GameTime.DateTime;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.Queue;

import static Core.Utilities.randomDirection;

enum ScriptType
{
    ROUTE, REPEAT, IDLE, GROW;

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
            case "GROW":
                return GROW;
            default:
                throw new RuntimeException("ScriptType unkown: " + s);
        }
    }
}

class RoutePoint2D
{
    String parameter;
    Point2D target;

    public RoutePoint2D(double x, double y, String parameter)
    {
        this.parameter = parameter;
        this.target = new Point2D(x, y);
    }

    public RoutePoint2D(double x, double y)
    {
        this.parameter = "not set";
        this.target = new Point2D(x, y);
    }
}

public class Script
{
    String CLASSNAME = "Script/";
    String PARAMETER_ATTRIBUTE = "parameter";

    int IDLE_WAITING_TIME = 3;
    int IDLE_MOVE_TIME = 2;
    ScriptType type;
    Queue<RoutePoint2D> route = new LinkedList<>();
    Long lastStatusChangeTime = 0L;
    Long lastScriptActTime = 0L;
    String executionStatus = "NOT SET";
    Direction randomDirection = null;


    public Script(Element xmlFile)
    {
        type = ScriptType.of(xmlFile.getAttribute("type"));
        var routePoints = xmlFile.getElementsByTagName("point");
        for (int i = 0; i < routePoints.getLength(); i++)
        {
            Element routePoint = (Element) routePoints.item(i);
            int x = Integer.parseInt((routePoint.getAttribute("x")));
            int y = Integer.parseInt((routePoint.getAttribute("y")));
            if ((routePoint.hasAttribute(PARAMETER_ATTRIBUTE)))
            {
                route.add(new RoutePoint2D(x, y, routePoint.getAttribute(PARAMETER_ATTRIBUTE)));
            }
            else
                route.add(new RoutePoint2D(x, y));
        }
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
            case GROW:
                grow(actor);
                break;
        }
    }

    private void idle(Actor actor)
    {
        String methodName = "idle() ";
        long currentTime = GameWindow.getCurrentNanoRenderTimeGameWindow();
        double time = (currentTime - lastScriptActTime) / 1000000000.0;
        double elapsedTimeLastStatusChange = (currentTime - lastStatusChangeTime) / 1000000000.0;
        double IDLE_VELOCITY = 60;

        if (executionStatus.equals("MOVE") && elapsedTimeLastStatusChange > IDLE_MOVE_TIME)
        {
            lastStatusChangeTime = currentTime;
            executionStatus = "WAIT";
        }
        else if (executionStatus.equals("WAIT") && elapsedTimeLastStatusChange > IDLE_WAITING_TIME)
        {
            lastStatusChangeTime = currentTime;
            executionStatus = "MOVE";
        }

        if (executionStatus.equals("MOVE"))
        {
            if (randomDirection == null)
            {
                randomDirection = randomDirection();
            }
            if (!actor.spriteList.get(0).isBlockedByOtherSprites(randomDirection, IDLE_VELOCITY * time))
                switch (randomDirection)
                {
                    case EAST:
                        actor.setVelocity(IDLE_VELOCITY, 0);
                        break;
                    case WEST:
                        actor.setVelocity(-IDLE_VELOCITY, 0);
                        break;
                    case NORTH:
                        actor.setVelocity(0, -IDLE_VELOCITY);
                        break;
                    case SOUTH:
                        actor.setVelocity(0, IDLE_VELOCITY);
                        break;
                }
            else
                actor.setVelocity(0, 0);
        }
        else
        {
            executionStatus = "WAIT";
            actor.setVelocity(0, 0);
            if (randomDirection != null)
            {
                randomDirection = null;
            }
        }

        lastScriptActTime = currentTime;

    }

    private void repeat(Actor actor)
    {
        String methodName = "repeat() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek().target;
        boolean reachedTarget = moveUnchecked(actor, target, route.peek().parameter);
        if (reachedTarget)
            route.add(route.remove());
    }

    private void grow(Actor actor)
    {
        String BUILDTIME = "buildtime";

        if(actor.getGeneralStatus().equals("empty"))
        {
            actor.setGenericDateTimeAttribute(BUILDTIME, null);
        }
        else
        {
            DateTime current = GameVariables.getClock().getCurrentGameTime();
            if (actor.getGenericDateTimeAttribute(BUILDTIME) == null)
                actor.setGenericDateTimeAttribute(BUILDTIME, current);
            if (actor.getGeneralStatus().equals("fuel_seed") && current.compareTo(actor.getGenericDateTimeAttribute(BUILDTIME).add(0,0, 10)) == 1)
            {
                actor.setSpriteStatus("fuel");
                actor.setSensorStatus("readyToHarvest");
            }
        }

    }

    private void route(Actor actor)
    {
        String methodName = "route() ";
        if (route.isEmpty())
            return;
        Point2D target = route.peek().target;
        boolean reachedTarget = moveUnchecked(actor, target, route.peek().parameter);
        if (reachedTarget)
            route.remove(target);
//        System.out.println(CLASSNAME + methodName + target.getX() + " " + target.getY());
    }

    private boolean moveUnchecked(Actor actor, Point2D target, String parameter)
    {
        Point2D currentPos = new Point2D(actor.spriteList.get(0).getX(), actor.spriteList.get(0).getY());
        double deltaX = target.getX() * 64 - currentPos.getX();
        double deltaY = target.getY() * 64 - currentPos.getY();
        double moveThreshold = 5d;
        boolean xreached = false, yreached = false;

        //tan(a) = Gegenkathete / Ankathete
        //sin(a) = Gegenkathete / Hypotenuse
        //cos(a) = Ankathete    / Hypotenuse
        //0     => right
        //45    => btm right
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        double hypotenuse = actor.getVelocity();
        double angle_rad = Math.toRadians(angle);
        double addedVelocityX = 0d;
        double addedVelocityY = 0d;
        //System.out.println("Angel: " + angle + "X: " + velX + " Y: " + velY);

        if (parameter.equals("warp"))
        {
            actor.spriteList.forEach(s -> s.setPosition(target.getX() * 64, target.getY() * 64));
            return true;
        }

        if (Math.abs(deltaX) > moveThreshold)
        {
            addedVelocityX = Math.cos(angle_rad) * hypotenuse;
        }
        else xreached = true;

        if (Math.abs(deltaY) > moveThreshold)
            addedVelocityY = Math.sin(angle_rad) * hypotenuse;
        else yreached = true;

        actor.setVelocity(addedVelocityX, addedVelocityY);

        if (xreached && yreached)
            return true;
        else
            return false;
    }
}
