package Core.Menus.Textbox;

import javafx.util.Pair;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class FontManager
{



    private Queue<Pair<String, Pair<Integer, Integer>>> findFontMarkings(String line)
    {
        Queue<Pair<String, Pair<Integer, Integer>>> formattingPositionQueue = new LinkedBlockingDeque<Pair<String, Pair<Integer, Integer>>>();
        int idxRegexBegin = 0;
        int idxRegexEnd = 0;
        do
        {
            idxRegexBegin = line.indexOf("%%");
            if (idxRegexBegin >= 0)
            {
                String regexFormat = line.substring(idxRegexBegin, idxRegexBegin + 4);
                line = line.replaceFirst(regexFormat, "");
                idxRegexEnd = line.indexOf("%%");
                line = line.replaceFirst("%%", "");
                formattingPositionQueue.add(new Pair<String, Pair<Integer, Integer>>(regexFormat, new Pair<>(idxRegexBegin, idxRegexEnd)));
            }
        } while (idxRegexBegin >= 0);
        return formattingPositionQueue;
    }
}
