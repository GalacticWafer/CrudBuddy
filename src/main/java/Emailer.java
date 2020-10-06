import java.io.*;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Emailer {
	/** Extract the text content from a message */
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

	/** Recurse into email content until message is reached, and return it */
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
				result.append(getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent()));
			}
		}
		return result.toString();
	}

	/** Prepare a message to be sent */
	private static Message prepareMessage(String subject, String content, Session session,
										  String myAccountEmail, String recipient) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myAccountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject(subject);
			message.setText(content);
			return message;
		}
		catch(Exception ex) {
			Logger.getLogger(Emailer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Process all orders in the inbox, and return a LinkedList<int[]> where each int[] represents
	 * a an array of product that we attempt to sell. Each integer inside the array indicates the
	 * reason for success or failure of that product's order fulfillment.
	 */
	public static LinkedList<int[]> processEmailOrders(Crud crud)
			throws MessagingException, IOException, SQLException {
		Folder inbox = Credentials.getInbox();
		inbox.open(Folder.READ_WRITE);
		Message[] messages = inbox.getMessages();
		LinkedList<int[]> list = new LinkedList<>();
		String prefix, suffix;
		for(Message message: messages) {
			boolean canComplete = true;
			LinkedList<TransactionItem> transactionItemList = new LinkedList<>();
			String customerEmail = "";
			StringBuilder orderString = new StringBuilder();
			String[] orders = getTextFromMessage(message).trim().split("\n");
			if(orders[0].split(",").length == 4) {
				String orderId = TransactionItem.generateId();
				int[] results = new int[orders.length];
				for(int i = 0; i < orders.length; i++) {
					String item = orders[i];
					String senderInfo = message.getFrom()[0].toString();
					TransactionItem transactionItem = new TransactionItem(item.trim(), orderId, senderInfo);
					transactionItem.setDate(message.getSentDate());
					results[i] = crud.isProcessableOrder(transactionItem);
					canComplete = results[i] >= 0 && canComplete;
					orderString.append(transactionItem.getResultString()).append("\n");
					if(i == 0) {
						customerEmail = transactionItem.getEmail();
					}
					transactionItemList.addLast(transactionItem);
				}
				list.addLast(results);
			}
			else {
				message.setFlag(Flags.Flag.DELETED, true);
			}
			if(canComplete) {
				for(TransactionItem transactionItem: transactionItemList) {
					crud.setQuantityFromOrder(transactionItem);
				}
			}
			prefix = (canComplete ?
					"The following products have been processed:\n\nProduct\tAmount\n"
					: "The following products could not be " +
					"processed:\n\nProduct\tStock\tRequested\n");

			suffix = (canComplete ?
					"\nThank you for using our service."
					: "\nWe are currently unable to fulfill this order because of the " +
					"quantities listed. We will email you " +
					"when this product is back in stock. Thank you.");

			String responseSubject = canComplete ? "Order Confirmed" : "Order Canceled";
			String response = prefix + orderString + suffix;
			sendMail(customerEmail, responseSubject, response);
		}
		return list;
	}



	/**Send an email to a customer to indicate order confirmation or cancellation*/
	public static void sendMail(String recipient, String subject, String content)
			throws MessagingException {
		Message message = prepareMessage(subject, content, Credentials.getSession(), Credentials
				.getEmail(), recipient);
		assert message != null;
		Transport.send(message);
		System.out.println();
	}

	public static void sendAttatchedEmail(String email, File reportFile) throws MessagingException {
		Properties properties = new Properties();

		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");

		String myAccountEmail = "mycrowsawftburner@gmail.com";
		String password = "CS3250TEAM4";

		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myAccountEmail, password);
			}
		});

		Message message = prepareAttachedMessage(session, myAccountEmail, email, reportFile);

		Transport.send(message);
		System.out.println("");
	}

	private static Message prepareAttachedMessage(Session session, String myAccountEmail, String recepient, File reportFile){
		try {
			BodyPart messageBodyPart = new MimeBodyPart();

			Multipart multi = new MimeMultipart();
			multi.addBodyPart(messageBodyPart);

			DataSource source = new FileDataSource(reportFile);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myAccountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject("Testing");
			messageBodyPart.setDataHandler(new DataHandler(source));
			message.setFileName(reportFile.toString());


			message.setContent(multi);

			return message;
		} catch (Exception ex){
			Logger.getLogger(Emailer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;

	}

}
