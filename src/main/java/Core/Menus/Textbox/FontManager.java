package Core.Menus.Textbox;

import Core.Configs.Config;
import javafx.scene.paint.Color;
import javafx.util.Pair;


import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static Core.Configs.Config.*;

public class FontManager
{
    static Map<String, Color> regex = new HashMap<>();
    static String regex_default = "%%DE";
    Queue<Pair<String, Pair<Integer, Integer>>> fondData;

    public FontManager(String line)
    {
        fondData = findFontMarkings(line);
        Color background = Config.COLOR_BACKGROUND_BLUE;
        double hue = background.getHue();
        double sat = background.getSaturation();
        double brig = background.getBrightness();
        Color marking = Color.hsb(hue, sat - 0.2, brig + 0.2);
        Color font = Color.hsb(hue, sat + 0.15, brig + 0.4);
        regex.put(regex_default, font);
        regex.put(REGEX_RED, COLOR_RED);
        regex.put(REGEX_GREEN, COLOR_GREEN);
        regex.put(REGEX_VIOLET, COLOR_VIOLET);
        regex.put(REGEX_GOLD, COLOR_GOLD);
    }

    public Color getFontAtLetter(int idx)
    {
        if (fondData.peek() != null
                && idx >= fondData.peek().getValue().getKey()//begin Color
                && idx<fondData.peek().getValue().getValue())//end Color
        {
            //System.out.println(CLASSNAME + methodName + charSpecialMarkingFound);
            Color color = regex.get(fondData.peek().getKey());
            if(idx+1 >= fondData.peek().getValue().getValue())
                fondData.remove();
            return color;
        }
        else
            return regex.get(regex_default);
    }

    public static String removeFontMarkings(String line)
    {
        for (String s: regex.keySet())
            line = line.replace(s,"");
        line = line.replace("%%", "");
        return line;
    }

    private Queue<Pair<String, Pair<Integer, Integer>>> findFontMarkings(String line)
    {
        Queue<Pair<String, Pair<Integer, Integer>>> colorCodeToIdxQueue = new LinkedBlockingDeque<Pair<String, Pair<Integer, Integer>>>();
        int idxStartTag;
        int idxEndTag;
        do
        {
            idxStartTag = line.indexOf("%%");
            if (idxStartTag >= 0)
            {
                String regexColorCode = line.substring(idxStartTag, idxStartTag + 4);
                line = line.replaceFirst(regexColorCode, "");
                idxEndTag = line.indexOf("%%");
                line = line.replaceFirst("%%", "");
                colorCodeToIdxQueue.add(new Pair<String, Pair<Integer, Integer>>(regexColorCode, new Pair<>(idxStartTag, idxEndTag)));
            }
        } while (idxStartTag >= 0);
        return colorCodeToIdxQueue;
    }
}