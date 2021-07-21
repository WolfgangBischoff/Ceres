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
    static List<Email> newMails = new ArrayList<>();
    static EmailManager instance;

    private EmailManager()
    {
        Element e = Utilities.readXMLFile("dialogue_files/emails/investigationStarts");
        NodeList mails = e.getElementsByTagName("mail");
        for (int i = 0; i < mails.getLength(); i++)
        {
            possibleMails.add(new Email(mails.item(i)));
        }
    }

    public static EmailManager getInstance()
    {
        if (instance == null)
            instance = new EmailManager();
        return instance;
    }

    public static void checkSendMails()
    {
        for (Email e : possibleMails)
            if (e.isSendConditionFullfilled())
            {
                newMails.add(e);
                inbox.add(e);
                e.sent = true;
            }
    }

    public static boolean hasNewEmails()
    {
        return !newMails.isEmpty();
    }

    public static Email readNewEmail()
    {
        if (hasNewEmails())
        {
            Email re = newMails.get(0);
            newMails.remove(re);
            return re;
        }
        return null;
    }

    public static Email readFirstEmail()
    {
        if(inbox.isEmpty())
            return Email.empty();
        else
            return inbox.get(0);
    }


}
