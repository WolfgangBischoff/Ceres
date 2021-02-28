package Core.Menus.Textbox;

import Core.Utilities;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import static Core.Configs.Config.*;

public class FontManager
{
    static Map<String, Color> regexMap = new HashMap<>();
    static String regex_default = "%%DE";
    Queue<Pair<String, Pair<Integer, Integer>>> fondData;
    String message = "";
    List<String> wrappedMessage = new ArrayList();
    Font font;

    public FontManager()
    {
        //fondData = findFontMarkings(line);
        if(regexMap.isEmpty())
        {
            regexMap.put(regex_default, COLOR_FONT);
            regexMap.put(REGEX_RED, COLOR_RED);
            regexMap.put(REGEX_GREEN, COLOR_GREEN);
            regexMap.put(REGEX_VIOLET, COLOR_VIOLET);
            regexMap.put(REGEX_GOLD, COLOR_GOLD);
        }

    }

    public void parseText(String messages, Font font, double lineWidth)
    {
        fondData = findFontMarkings(messages);
        message = removeFontMarkings(messages);
        this.font = font;
        wrappedMessage = Utilities.wrapText(message, font, lineWidth, null);
    }

    public void parseOptions(List<String> options, Font font, double lineWidth)
    {
        String optionsAsOneString = String.join("", options);
        fondData = findFontMarkings(optionsAsOneString);
        message = removeFontMarkings(optionsAsOneString);
        wrappedMessage = options.stream().map(s -> removeFontMarkings(s)).collect(Collectors.toList());
    }

    public int lettersTo(int maxLettersRendered)
    {
        return Math.min(maxLettersRendered, message.length());
    }

    public char getLetterAt(int idx)
    {
        return message.charAt(idx);
    }

    public int getLineIdx(int letterIdx)
    {
        int sumLetters = 0;
        for(int i=0; i<wrappedMessage.size(); i++)
        {
            sumLetters += wrappedMessage.get(i).length();
            if(letterIdx < sumLetters)
                return i;
        }
        return 0;
    }

    public double getLineXOffset(int lineIdx, int nextLetterIdx)
    {
        for(int i=0; i<lineIdx; i++)
            nextLetterIdx -= wrappedMessage.get(i).length();
       return textWidth(font, wrappedMessage.get(lineIdx).substring(0, nextLetterIdx));
    }

    private static double textWidth(Font font, String s)
    {
        Text text = new Text(s);
        text.setFont(font);
        return text.getBoundsInLocal().getWidth();
    }

    public String getLine(int idx)
    {
        if(idx < wrappedMessage.size())
            return wrappedMessage.get(idx);
        else
            return "";
    }

    public static String removeFontMarkings(String line)
    {
        for (String s : regexMap.keySet())
            line = line.replace(s, "");
        line = line.replace("%%", "");
        return line;
    }

    public Color getFontAtLetter(int idx)
    {
        if (fondData.peek() != null
                && idx >= fondData.peek().getValue().getKey()//begin Color
                && idx < fondData.peek().getValue().getValue())//end Color
        {
            Color color = regexMap.get(fondData.peek().getKey());
            if (idx + 1 >= fondData.peek().getValue().getValue())
                fondData.remove();
            return color;
        }
        else
            return regexMap.get(regex_default);
    }

    private Queue<Pair<String, Pair<Integer, Integer>>> findFontMarkings(String line)
    {
        Queue<Pair<String, Pair<Integer, Integer>>> colorCodeToIdxQueue = new LinkedBlockingDeque<Pair<String, Pair<Integer, Integer>>>();
        int idxStartTag;
        int idxEndTag;
        do {
            idxStartTag = line.indexOf("%%");
            if (idxStartTag >= 0) {
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
