package Core.Menus.Email;

import Core.Utilities;
import javafx.scene.text.Text;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Email
{
    String sender;
    String text;
    boolean sent = false;
    boolean read = false;
    EmailCondition condition;

    public Email(Node mail)
    {
        Node senderNode = ((Element)mail).getElementsByTagName("sender").item(0);
        Node textNode = ((Element)mail).getElementsByTagName("text").item(0);
        text = textNode.getTextContent();
        sender = senderNode.getTextContent();

        String var = ((Element)mail).getAttribute("variable");
        String trueValue = ((Element)mail).getAttribute("value");
        boolean sleepNeeded = Boolean.parseBoolean(((Element)mail).getAttribute("sleep"));
        condition = new EmailCondition(sleepNeeded, var, trueValue);
    }

    public boolean isSendConditionFullfilled(boolean slept)
    {
        return condition.evaluate(slept) && !isSent();
    }

    public String getSender()
    {
        return sender;
    }

    public String getText()
    {
        return text;
    }

    public boolean isSent()
    {
        return sent;
    }
}
