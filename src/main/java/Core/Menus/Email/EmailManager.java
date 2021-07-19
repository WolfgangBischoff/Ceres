package Core.Menus.Email;

import Core.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class EmailManager
{
    static List<Email> possibleMails = new ArrayList<>();
    static List<Email> inbox = new ArrayList<>();
    static EmailManager instance;

    public static EmailManager getInstance()
    {
        if(instance == null)
            instance = new EmailManager();
        return instance;
    }
    private EmailManager()
    {
        Element e = Utilities.readXMLFile("dialogue_files/emails/investigationStarts");
        NodeList mails = e.getElementsByTagName("mail");
        for(int i= 0; i< mails.getLength(); i++)
        {
            possibleMails.add(new Email(mails.item(i)));
        }
    }

    public static  List<Email> checkNewMails(boolean slept)
    {
        List<Email> newMails = new ArrayList<>();
        for(Email e : possibleMails)
            if(e.isSendConditionFullfilled(slept))
            {
                newMails.add(e);
                e.sent = true;
            }
        inbox.addAll(newMails);
            return newMails;
    }

}
