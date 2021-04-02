package Core.Utils;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public class FXUtils
{
    private static long lastUpdate = 0;
    private static int index = 0;
    private static double[] frameRates = new double[100];
    private static List<String> measurements = new ArrayList<>();

    static
    {
        AnimationTimer frameRateMeter = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                if (lastUpdate > 0)
                {
                    long nanosElapsed = now - lastUpdate;
                    double frameRate = 1000000000.0 / nanosElapsed;
                    index %= frameRates.length;
                    frameRates[index++] = frameRate;
                }

                lastUpdate = now;
            }
        };

        frameRateMeter.start();
    }

    /**
     * Returns the instantaneous FPS for the last frame rendered.
     *
     * @return
     */
    public static double getInstantFPS()
    {
        return frameRates[index % frameRates.length];
    }

    /**
     * Returns the average FPS for the last 100 frames rendered.
     * @return
     */
    public static double getAverageFPS()
    {
        double total = 0.0d;

        for (int i = 0; i < frameRates.length; i++)
        {
            total += frameRates[i];
        }

        return total / frameRates.length;
    }

    public static String getData()
    {
        StringBuilder stringBuilder = new StringBuilder();
        measurements.forEach(s -> stringBuilder.append(s ).append(" "));
        measurements.clear();
        return stringBuilder.toString();
    }

    public static void addData(String s)
    {
        measurements.add(s);
    }
}