import java.io.*;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class EmailReader {

    public static void main(String[] args){
    Properties pro = System.getProperties();
    pro.setProperty("mail.store.protocol", "imaps");
    try {
        Session session = Session.getDefaultInstance(pro, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", "mycrowsawftburner@gmail.com", "CS3250TEAM4");
        System.out.println(store);

        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);

        Message messages[] = inbox.getMessages();
            for(Message message : messages) {
                String content = getTextFromMessage(message);
                EmailParser parser = new EmailParser(content);
                parser.setEmail();
                parser.setDate(message.getSentDate());
            }


        } catch (MessagingException e){
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private static String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {

                System.out.println("It happens");

            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }


}
