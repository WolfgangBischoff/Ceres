package Core;

import Core.Enums.Direction;
import javafx.scene.image.Image;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static Core.Configs.Config.FONT_ORBITRON_12;

public class Utilities
{
    private static String CLASSNAME = "Utilities/";
    private static Random random = new Random();

    public static float randomFloat()
    {
        return random.nextFloat();
    }

    public static int randomSignedInt(int min, int max)
    {
        int number = random.nextInt(max - min) + min;
        return random.nextBoolean() ? number : -number;
    }

    public static int randomInt(int min, int max)
    {
        return random.nextInt(max - min) + min;
    }

    public static Direction randomDirection()
    {
        return Direction.of(random.nextInt(4));
    }

    public static String removeAllBlanksExceptOne(String s)
    {
        return s.replace("\n", "").replaceAll("\\s{2,}", " ").replaceFirst("^\\s*", "");
    }

    public static boolean doCircleOverlap(Circle a, Circle b)
    {
        //Use distance formula to check if a circle overlaps with another circle.
        double distance = Math.sqrt(Math.pow(a.getCenterX() - b.getCenterX(), 2) + (Math.pow(a.getCenterY() - b.getCenterY(), 2)));
        return distance <= (a.getRadius() + b.getRadius());
    }

    public static String roundTwoDigits(Double input)
    {
        DecimalFormatSymbols point = new DecimalFormatSymbols();
        point.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", point);
        return df.format(input);
    }

    public static boolean tryParseInt(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static List<String[]> readAllLineFromTxt(String path)
    {
        String methodName = "readAllLineFromTxt() ";
        String row;
        List<String[]> data = new ArrayList<>();
        try
        {
            InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(Utilities.class.getClassLoader().getResourceAsStream(path)));
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
            }
            csvReader.close();
        }
        catch (IOException | NullPointerException e)
        {
            throw new RuntimeException(path + " not found.");
        }
        return data;
    }

    public static boolean doesXMLFileExist(String path)
    {
        return (Utilities.class.getClassLoader().getResourceAsStream(path + ".xml") != null);
    }

    public static Element readXMLFile(String path)
    {
        String methodName = "readXMLFile() ";
        String xmlPath = path + ".xml";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (Utilities.class.getClassLoader().getResourceAsStream(xmlPath) == null)
                throw new IOException(CLASSNAME + methodName + xmlPath + " not found.");
            Document doc = builder.parse(Objects.requireNonNull(Utilities.class.getClassLoader().getResourceAsStream(xmlPath)));
            return doc.getDocumentElement();
        }
        catch (ParserConfigurationException | SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("File does not exist: " + xmlPath);
        }

        throw new RuntimeException("Uncatched Exception for path: " + xmlPath);
    }

    public static Image readImage(String path)
    {
        String methodName = "readImage() ";
        InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(path);
        if (stream == null)
        {
            System.out.println(CLASSNAME + methodName + path + " not found.");
            return new Image(Objects.requireNonNull(Utilities.class.getClassLoader().getResourceAsStream("img/notfound_64_64" + ".png")));
        }
        return new Image(stream);
    }

    public static Font readFont(String path, int size)
    {
        InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(path);
        return Font.loadFont(stream, size);
    }

    public static List<Pair<String, String>> readParameterPairs(String[] arr)
    {
        String methodName = "readParameterPairs() ";
        List<Pair<String, String>> ret = new ArrayList<>();
        try
        {
            for (int i = 0; i < arr.length; )
            {
                ret.add(new Pair<>(arr[i], arr[i + 1]));
                i += 2;
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println(CLASSNAME + methodName + "Must have a equal number of elements: " + Arrays.toString(arr));
        }
        return ret;
    }

    public static List<String> wrapText(String s, Font font, double lineWidth)
    {
        List<String> lines = new ArrayList<>();
        String[] words = s.split(" ");
        int i = 0;
        String checkIfTooLong = "";
        String fits = "";
        while (words.length > i)
        {
            fits = checkIfTooLong;
            checkIfTooLong = fits + " " + words[i];
            Text text = new Text(checkIfTooLong);
            text.setFont(font);
            double width = text.getBoundsInLocal().getWidth();
            if (width > lineWidth)
            {
                lines.add(fits);
                checkIfTooLong = "";
                words = Arrays.copyOfRange(words, i, words.length);
                i = 0;
            }
            else
                i++;
        }
        if(!checkIfTooLong.equals(""))
            lines.add(checkIfTooLong);
        return lines;
    }

}