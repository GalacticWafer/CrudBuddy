import java.io.*;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;

public class EmailReader {
	public static LinkedList<Order[]> readEmailOrders() {
		
		Properties pro = System.getProperties();
		pro.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(pro, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", "mycrowsawftburner@gmail.com",
			 "CS3250TEAM4");
			System.out.println(store);
			
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			
			Message[] messages = inbox.getMessages();
			LinkedList<Order[]> list = new LinkedList<>();
			for(Message message: messages) {
				String[] stingOfOrders = getTextFromMessage(message).trim().split("\n");
				if(stingOfOrders.length == 1) {
					continue;
				}
				if(stingOfOrders[0].split(",").length == 4) {
					Order[] orders = new Order[stingOfOrders.length];
					String orderId = Order.generateId();
					int i = 0;
					for(String item: stingOfOrders) {
						Order order = new Order(item.trim(), orderId);
						order.setEmail(message.getFrom()[0].toString());
						order.setDate(message.getSentDate());
						orders[i++] = order;
					}
					list.add(orders);
				}
				else {
					message.setFlag(Flags.Flag.DELETED, true);
				}
			}
			return list;
		}
		catch(MessagingException | IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return null;
	}
	
	private static String getTextFromMessage(Message message)
	throws MessagingException, IOException {
		
		String result = "";
		if(message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		}
		else if(message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}
	
	private static String getTextFromMimeMultipart(
	 MimeMultipart mimeMultipart) throws MessagingException, IOException {
		
		StringBuilder result = new StringBuilder();
		int count = mimeMultipart.getCount();
		for(int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if(bodyPart.isMimeType("text/plain")) {
				result.append("\n").append(bodyPart.getContent());
				break; // without break same text appears twice in my tests
			}
			else if(bodyPart.isMimeType("text/html")) {
				
				System.out.println("It happens");
			}
			else if(bodyPart.getContent() instanceof MimeMultipart) {
				result
				 .append(getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent()));
			}
		}
		return result.toString();
	}
}
