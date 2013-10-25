package it.i88.ca.example.deleteemailbyjavamail;

import com.sun.mail.imap.IMAPFolder;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Example of deleting email using JavaMail API. It is not following the best
 * practice. It is just for example of some of the API functions. In practice
 * you should use command of IMAPProtocol instead.
 * @see <a
 * href="http://it.i88.ca/2013/10/example-of-deleting-email-using.html">it.i88.ca</a>
 */
public class App {

    public static void main(String[] args) throws NoSuchProviderException, MessagingException {
        Properties prop = new Properties();
        try {
            //load a properties file from class path, inside static method
            prop.load(new FileInputStream("newproperties.properties"));
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        Properties props = System.getProperties();
        props.setProperty("mail.pop3.port", prop.getProperty("port"));
        Session session = Session.getInstance(props);

        Store store = session.getStore("imap");

        store.connect(prop.getProperty("host"), prop.getProperty("user"), prop.getProperty("passwd"));

        javax.mail.Folder[] dfolders = store.getDefaultFolder().list("*");
        for (Folder f : dfolders) {
            if (!"INBOX".equalsIgnoreCase(f.getFullName())) {
                continue; //only process inbox folder
            }

            if ((f.getType() & javax.mail.Folder.HOLDS_MESSAGES) == 0) {
                continue; // no problem for inbox folder, but need this for some other folder
            }
            IMAPFolder folder = (IMAPFolder) f;

            int c = folder.getMessageCount();
            for (int i = 1; i < c; i++) {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_WRITE);
                }
                Message message = folder.getMessage(i);
                message.setFlag(Flags.Flag.DELETED, true); //marked this message as deleted
                //folder.expunge(new Message[]{message});
                // for some email server, it throws javax.mail.FolderClosedException: * BYE [ALERT] Fatal error: Cannot allocate memory
                if (folder.isOpen() && i % 10 == 0) {
                    System.out.println(folder.getMessageCount());
                    folder.close(true); //open and close can avoid the FolderClosedException from folder.expunge.
                }            }
            if (folder.isOpen()) {
                folder.close(true);
            }
        }
    }
}
