package Core.Menus.Textbox;

import Core.Configs.Config;
import javafx.scene.paint.Color;
import javafx.util.Pair;


import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static Core.Configs.Config.COLOR_GREEN;
import static Core.Configs.Config.COLOR_RED;

public class FontManager
{
    static Map<String, Color> regex = new HashMap<>();
    static String regex_default = "%%DE";
    static String regex_red = "%%RD";
    static String regex_green = "%%GR";
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
        regex.put(regex_red, COLOR_RED);
        regex.put(regex_green, COLOR_GREEN);
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

    public String removeFontMarkings(String line)
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
