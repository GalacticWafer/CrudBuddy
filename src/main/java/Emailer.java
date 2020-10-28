import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
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
	public Emailer() {}
	
	private Message createNewMessage(Session session, String myAccountEmail,
									 String recipient, String subject)
	throws MessagingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(myAccountEmail));
		
		message
		 .setRecipient(
		  Message.RecipientType.TO,
		  new InternetAddress(recipient));
		message.setSubject(subject);
		return message;
	}
	
	/** Extract the text content from a message */
	private String getTextFromMessage(Message message)
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
	private String getTextFromMimeMultipart(
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
	
	private Message prepareAttachedMessage(Session session,
										   String myAccountEmail,
										   String recipient,
										   File reportFile, String subject) {
		try {
			BodyPart messageBodyPart = new MimeBodyPart();
			
			Multipart multi = new MimeMultipart();
			multi.addBodyPart(messageBodyPart);
			
			DataSource source = new FileDataSource(reportFile);
			Message message =
			 createNewMessage(session, myAccountEmail, recipient, subject);
			
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
	private Message prepareMessage
	(String subject, String content, Session session,
	 String myAccountEmail, String recipient) {
		try {
			Message message =
			 createNewMessage(session, myAccountEmail, recipient, subject);
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
	 * Looks in the emails, and processes all items in each
	 */
	public void processEmails(Crud crud)
	throws MessagingException, IOException, SQLException {
		crud.setWorkingTable("sales");
		Session session = Credentials.getSession();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SalesProcessor processor = new SalesProcessor(crud);
		Message[] messages = Credentials.getMessages(session);
		for(Message message: messages) {
			Order order = null;
			String[] messageText =
			 getTextFromMessage(message).trim().split("\n");
			LocalDate date = LocalDate.parse(format.format(
			 message.getSentDate()));
			
			Matcher m = Order.EMAIL_PATTERN.matcher(
			 message.getFrom()[0].toString());
			String email = m.find() ? m.group("email") : "";
			
			try {
				for(String textLine: messageText) {
					String[] s = textLine.split(",");
					if(s.length != 4) {
						if(message.getSubject().toUpperCase()
								  .contains("cancel".toUpperCase())) {
							String orderId = s[0];
							Object[][] records = crud.getRecords(
							 "SELECT * FROM sales where order_id = '"
							 + orderId + "'");
							System.out.println(
							 "The following product purchases should be " +
							 "cancelled:\n\n" +
							 Arrays.deepToString(records));
							// Todo  Daniel, roll back the order if it exists.
							break;
						}
						message.setFlag(Flags.Flag.DELETED, true);
						break;
					}
					String productId = s[0].trim();
					int requestedQuantity = Integer.parseInt(s[1].trim());
					boolean isSale = Boolean.parseBoolean(s[2].trim());
					String location = s[3].trim();
					
					if(order == null) {
						order = new Order(date, isSale, location);
						processor.setOrder(order);
					}
					order.add(new Product(productId, requestedQuantity));
				}
				assert order != null;
				order.setEmail(email);
				processor.processOrder();
				sendMail(order.getCustomerEmail(), order.getSubject(), order
				 .getMessageText(), session, null);
				message.setFlag(Flags.Flag.DELETED, true);
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
				// Todo: this email is in an improper format
			}
		}
		processor.updateAndClose();
	}
	
	/**
	 * Send an email to a customer to indicate order confirmation or
	 * cancellation
	 */
	public void sendMail(String toAddress, String subject, String content,
						 Session session, File reportFile)
	throws MessagingException {
		Message message =
		 reportFile == null ?
		  prepareMessage(subject, content, session, Credentials
		   .getEmail(), toAddress)
		  : prepareAttachedMessage(session, Credentials
		  .getEmail(), toAddress, reportFile, subject);
		
		assert message != null;
		Transport.send(message);
		System.out.println();
	}
}
