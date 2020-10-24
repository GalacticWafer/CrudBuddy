import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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
		} else if(message.isMimeType("multipart/*")) {
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
			} else if(bodyPart.isMimeType("text/html")) {
				System.out.println("It happens");
			} else if(bodyPart.getContent() instanceof MimeMultipart) {
				result.append(getTextFromMimeMultipart((MimeMultipart)bodyPart
				 .getContent()));
			}
		}
		return result.toString();
	}
	
	private static Message prepareAttachedMessage(Session session,
												  String myAccountEmail,
												  String recepient,
												  File reportFile) {
		try {
			BodyPart messageBodyPart = new MimeBodyPart();
			
			Multipart multi = new MimeMultipart();
			multi.addBodyPart(messageBodyPart);
			
			DataSource source = new FileDataSource(reportFile);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myAccountEmail));
			message
			 .setRecipient(
			  Message.RecipientType.TO,
			  new InternetAddress(recepient));
			message.setSubject("Testing");
			messageBodyPart.setDataHandler(new DataHandler(source));
			message.setFileName(reportFile.toString());
			
			message.setContent(multi);
			
			return message;
		}
		catch(Exception ex) {
			Logger.getLogger(Emailer.class.getName())
				  .log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	/** Prepare a message to be sent */
	private static Message prepareMessage(String subject, String content,
										  Session session,
										  String myAccountEmail,
										  String recipient) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myAccountEmail));
			message
			 .setRecipient(
			  Message.RecipientType.TO,
			  new InternetAddress(recipient));
			message.setSubject(subject);
			message.setText(content);
			return message;
		}
		catch(Exception ex) {
			Logger.getLogger(Emailer.class.getName())
				  .log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	/**
	 *
	 */
	public static void processDailyEmails(Crud crud)
	throws MessagingException, IOException, SQLException {
		Folder inbox = Credentials.getInbox();
		inbox.open(Folder.READ_WRITE);
		Message[] messages = inbox.getMessages();
		ArrayList<String> orderIds = new ArrayList<>();
		HashMap<String, Order> todaysOrders = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i = 0; i < messages.length; i++) {
			String[] messageText =
			 getTextFromMessage(messages[i]).trim().split("\n");
			if(messageText[0].split(",").length != 4) {
				messages[i].setFlag(Flags.Flag.DELETED, true);
				continue;
			} else {
				Order order = null;
				LocalDate date = LocalDate.parse(sdf.format(
				 messages[i].getSentDate()));
				
				Matcher m = Order.EMAIL_PATTERN.matcher(
				 messages[i].getFrom()[0].toString());
				String email = m.find() ? m.group("email") : "";
				
				orderIds.add(order.getOrderId());
				for(String textLine: messageText) {
					String[] s = textLine.split(",");
					
					String productId = s[0].trim();
					int requestedQuantity = Integer.parseInt(s[1].trim());
					boolean isSale = Boolean.parseBoolean(s[2].trim());
					String location = s[3].trim();
					
					if(order == null) {
						new Order(date, email, true, location, Order
						 .generateId(), new ArrayList<>());
					}
					order
					 .add(new TransactionItem(productId, requestedQuantity));
				}
				todaysOrders.put(order.getOrderId(), order);
			}
		}
		SalesProcessor salesProcessor = new SalesProcessor(crud);
		for(String orderId: orderIds) {
			Order order = todaysOrders.get(orderId);
			salesProcessor.setOrder(order);
			if(salesProcessor.canProcessOrder().contains(false)) {
				salesProcessor.insertBackOrders(order);
				sendMail(order.getEmail(), "Sorry bruh", "We can't do dis...");
			} else {
				salesProcessor.processOrder(order);
				// Todo: send an email saying we can do it.
				sendMail(order.getEmail(), "I gotcha bruh!", "u want fast or slow shipping?");
			}
		}
		salesProcessor.close();
	}
	
	/**
	 * Return a LinkedList<int[]> where
	 * each int[] represents
	 * a an array of product that we attempt to sell. Each integer inside the
	 * array indicates the
	 * reason for success or failure of that product's order fulfillment.
	 */
	/*
	public static void respondToOrders(
	 ArrayList<Pair<File, Boolean>> orders)
	throws MessagingException, IOException {
		String prefix, suffix;
		
		for(Pair<File, Boolean> order: orders) {
			boolean canComplete = order.getValue();
			File file = order.getKey();
			ArrayList<Object[]> sales = new ArrayList<>();
			if(canComplete) {
				Scanner scanner = new Scanner(file);
				scanner.nextLine();
				while(scanner.hasNextLine()) {
				}
			} else {
				
			}
			
			prefix = (canComplete ?
			 "The following products have been processed:\n\nProduct\tAmount\n"
			 : "The following products could not be " +
			   "processed:\n\nProduct\tStock\tRequested\n");
			suffix = (canComplete ?
			 "\nThank you for using our service."
			 :
			 "\nWe are currently unable to fulfill this order. We will email
			  " +
			 "you " +
			 "when this product is back in stock. Thank you.");
			
			String responseSubject =
			 canComplete ? "Order Confirmed" : "Order Canceled";
			String response = prefix + orderString + suffix;
			sendMail(customerEmail, responseSubject, response);
		}
		return list;
	}
	*/
	public static void sendAttatchedEmail(String email, File reportFile)
	throws MessagingException {
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
		
		Message message =
		 prepareAttachedMessage(session, myAccountEmail, email, reportFile);
		
		Transport.send(message);
		System.out.println("");
	}
	
	/**
	 * Send an email to a customer to indicate order confirmation or
	 * cancellation
	 */
	public static void sendMail(String recipient, String subject,
								String content)
	throws MessagingException {
		Message message =
		 prepareMessage(subject, content, Credentials.getSession(), Credentials
		  .getEmail(), recipient);
		assert message != null;
		Transport.send(message);
		System.out.println();
	}
}
