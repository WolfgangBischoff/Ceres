package Core.WorldView;

import Core.GameTime.ClockMode;
import Core.GameTime.Time;
import Core.GameTime.TimeSpan;
import Core.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static Core.Configs.Config.DAY_LIGHT_OFF_TIME;
import static Core.Configs.Config.DAY_LIGHT_ON_TIME;
import static Core.GameTime.ClockMode.RUNNING;


public class MapTimeData
{
    static final String CLASSNAME = "MapTimeData/";
    Time lightOnTime, lightOffTime;
    ClockMode clockMode;
    TimeSpan openedTime= null;
    String levelAfterClose= null;
    String spawnPointAfterClose = null;

    public MapTimeData(String[] linedata)
    {
        readData(linedata);
    }



    private MapTimeData()
    {
        lightOnTime = DAY_LIGHT_ON_TIME;
        lightOffTime = DAY_LIGHT_OFF_TIME;
        clockMode = RUNNING;
    }

    public static MapTimeData getDefault()
    {
        return new MapTimeData();
    }

    private void readData(String[] linedata)
    {
        String TIME_TAG = "time";
        String CLOSED_TAG = "closed";
        Element xml = Utilities.readXMLFile(linedata[0]);
        Node time = xml.getElementsByTagName(TIME_TAG).item(0);
        readTime((Element) time);

        Node closedTimes = xml.getElementsByTagName(CLOSED_TAG).item(0);
        if(closedTimes != null)
            readOpenedTimeSpan((Element) closedTimes);

    }

    private void readOpenedTimeSpan(Element closedTimes)
    {
        String openedTimeString = closedTimes.getAttribute("OPENED");
        Time opened = Time.of(openedTimeString);

        String closedTimeString = closedTimes.getAttribute("CLOSED");
        Time closed = Time.of(closedTimeString);
        openedTime = new TimeSpan(opened, closed);

        levelAfterClose = closedTimes.getAttribute("MAP_AFTER_CLOSE");
        spawnPointAfterClose = closedTimes.getAttribute("SPAWN");

        System.out.println(CLASSNAME + " " + openedTime + " " + levelAfterClose);
    }

    private void readTime(Element time)
    {
        String timeOnString = time.getAttribute("DAY_LIGHT_ON_TIME");
        lightOnTime = Time.of(timeOnString);
        if (lightOnTime == null)
            lightOnTime = DAY_LIGHT_ON_TIME;

        String timeOffString = time.getAttribute("DAY_LIGHT_OFF_TIME");
        lightOffTime = Time.of(timeOffString);
        if (lightOffTime == null)
            lightOffTime = DAY_LIGHT_OFF_TIME;

        String timeMode = time.getAttribute("TIME_MODE");
        clockMode = ClockMode.of(timeMode);
        if (clockMode == null)
            clockMode = RUNNING;

        System.out.println(CLASSNAME + " " + lightOnTime + " " + lightOffTime + " " + timeMode);
    }

    public Time getLightOnTime()
    {
        return lightOnTime;
    }

    public Time getLightOffTime()
    {
        return lightOffTime;
    }

    public ClockMode getClockMode()
    {
        return clockMode;
    }

    public TimeSpan getOpenedTime()
    {
        return openedTime;
    }

    public String getLevelAfterClose()
    {
        return levelAfterClose;
    }

    public String getSpawnPointAfterClose()
    {
        return spawnPointAfterClose;
    }
}
