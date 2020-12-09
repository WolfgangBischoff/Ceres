package Core;

import Core.ActorSystem.SystemStatus;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Utilities
{
    private static String CLASSNAME = "Utilities/";

    public static boolean doCircleOverlap(Circle a, Circle b)
    {
        //Use distance formula to check if a circle overlaps with another circle.
        double distance = Math.sqrt(Math.pow(a.getCenterX() - b.getCenterX(), 2) + (Math.pow(a.getCenterY() - b.getCenterY(), 2)));
        return distance <= (a.getRadius() + b.getRadius());
    }

    public static Double roundTwoDigits(Double input)
    {
        DecimalFormatSymbols point = new DecimalFormatSymbols();
        point.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##", point);
        return Double.valueOf(df.format(input));
    }

    public static boolean tryParseInt(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static List<String[]> readAllLineFromTxt(String path)
    {
        String row;
        //int linecounter = 0;
        List<String[]> data = new ArrayList<>();
        try
        {
            InputStreamReader isr = new InputStreamReader(Utilities.class.getClassLoader().getResourceAsStream(path));
            BufferedReader csvReader = new BufferedReader(isr);
            while ((row = csvReader.readLine()) != null)
            {
                //Check for comments and blank lines
                if (row.isEmpty() || row.startsWith("#"))
                    continue;
                String[] rawdata = row.split(";");
                String[] trimmed = new String[rawdata.length];
                for (int i = 0; i < rawdata.length; i++)
                    trimmed[i] = rawdata[i].trim();
                data.add(trimmed);
                //linecounter++;
            }
            csvReader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return data;
    }


    public static Element readXMLFile(String path)
    {
        String methodName = "readXMLFile() ";
        //https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
            if(Utilities.class.getClassLoader().getResourceAsStream(path) == null)
                System.out.println(CLASSNAME + methodName + path + " not found.");
            Document doc = builder.parse(Utilities.class.getClassLoader().getResourceAsStream(path));
            return doc.getDocumentElement();
        } catch (ParserConfigurationException | SAXException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            throw new IllegalArgumentException("File does not exist: " + path);
        }

        throw new RuntimeException("Uncatched Exception for path: " + path);
    }

    public static Image readImage(String path)
    {
        String methodName = "readImage() ";
        InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(path);
        if(stream == null)
        {
            System.out.println(CLASSNAME + methodName + path + " not found.");
            return new Image(Utilities.class.getClassLoader().getResourceAsStream("img/notfound_64_64" + ".png"));
        }
        return new Image(stream);
    }

    public static Font readFont(String path)
    {
        InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(path);
        return Font.loadFont(stream, 30);
    }

    public static List<Pair<String, String>> readParameterPairs(String[] arr)
    {
        String methodName = "readParameterPairs() ";
        List<Pair<String, String>> ret = new ArrayList<>();
        try{
            for(int i=0; i<arr.length;)
            {
                ret.add(new Pair<String, String>(arr[i], arr[i+1]));
                i += 2;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println(CLASSNAME + methodName + "Must have a equal number of elements: " + Arrays.toString(arr));
        }
        return ret;
    }

}