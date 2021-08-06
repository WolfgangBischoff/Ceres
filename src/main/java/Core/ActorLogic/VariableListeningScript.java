package Core.ActorLogic;

import Core.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class VariableListeningScript
{
    private static final String CLASSNAME = "VariableListeningScript/";
    private Map<String, List<InstructionData>> instructionDataMap;

    private VariableListeningScript(Map<String, List<InstructionData>> variablename)
    {
        this.instructionDataMap = variablename;
    }

    public static VariableListeningScript read(String[] linedata)
    {
        Map<String, List<InstructionData>> instructionDataList = new HashMap<>();
        var xml = Utilities.readXMLFile(linedata[1]);
        NodeList vars = xml.getElementsByTagName("variable");
        for(int i=0; i<vars.getLength(); i++)
        {
            String variablename = xml.getAttribute("variablename");
            instructionDataList.put(variablename, readData(vars.item(i)));
        }

        VariableListeningScript ret = new VariableListeningScript(instructionDataList);
        System.out.println(ret);
        return ret;
    }

    private static List<InstructionData> readData(Node variable)
    {
        List<InstructionData> ret = new ArrayList<>();
        NodeList possibleValues = ((Element)variable).getElementsByTagName("possiblevalue");
        for(int i=0; i<possibleValues.getLength();i++)
        {
            Element e = (Element)possibleValues.item(i);
            String listeningvalue = (e).getAttribute("variablevalue");
            String dialoguefile = (e).getAttribute("dialoguefile");
            String dialogueid = (e).getAttribute("dialogueid");
            ret.add(new InstructionData(listeningvalue, dialoguefile, dialogueid));
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return "VariableListeningScript: " + instructionDataMap.toString();
    }
}
