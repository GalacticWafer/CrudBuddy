import java.io.*;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;

public class EmailReader {
	public static LinkedList<int[]> processEmailOrders(CRUDBuddy crud) {
		Properties pro = System.getProperties();
		pro.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(pro, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", "mycrowsawftburner@gmail.com", "CS3250TEAM4");
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			Message[] messages = inbox.getMessages();
			LinkedList<int[]> list = new LinkedList<>();
			String prefix, suffix;
			for(Message message: messages) {
				boolean canComplete = true;
				LinkedList<Order> orderList = new LinkedList<>();
				String customerEmail = "";
				StringBuilder orderString = new StringBuilder();
				String[] orders = getTextFromMessage(message).trim().split("\n");
				if(orders[0].split(",").length == 4) {
					String orderId = Order.generateId();
					int[] results = new int[orders.length];
					for(int i = 0; i < orders.length; i++) {
						String item = orders[i];
						String senderInfo = message.getFrom()[0].toString();
						Order order = new Order(item.trim(), orderId, senderInfo);
						order.setDate(message.getSentDate());
						results[i] = crud.canProcessOrder(order);
						canComplete = results[i] >= 0 && canComplete;
						orderString.append(order.getResultString()).append("\n");
						if(i == 0) {
							customerEmail = order.getEmail();
						}
						orderList.addLast(order);
					}
					list.addLast(results);
				}
				else {
					message.setFlag(Flags.Flag.DELETED, true);
				}
				if(canComplete) {
					for(Order order: orderList) {
						crud.processOrder(order);
					}
				}
				prefix = (canComplete?
				 "The following products have been processed:\n\nProduct\tAmount\n"
				 :"The following products could not be " +
				  "processed:\n\nProduct\tStock\tRequested\n");
				
				suffix = (canComplete?
				 "\nThank you for using our service."
				 :"\nWe are currently unable to fulfill this order because of the " +
				  "quantities listed. We will email you " +
				  "when this product is back in stock. Thank you.");
				
				String responseSubject = canComplete?"Order Confirmed":"Order Canceled";
				String response = prefix + orderString + suffix;
				EmailWriter.sendMail(customerEmail, responseSubject, response);
			}
			return list;
		}
		catch(MessagingException | IOException |
		 SQLException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return null;
	}
	
	// extract the content from a message
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
	
	// recurse into content until message is reached, and return it
	private static String getTextFromMimeMultipart(
	 MimeMultipart mimeMultipart) throws MessagingException, IOException {
		StringBuilder result = new StringBuilder();
		int count = mimeMultipart.getCount();
		for(int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if(bodyPart.isMimeType("text/plain")) {
				result.append("\n").append(bodyPart.getContent());
				break;
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
