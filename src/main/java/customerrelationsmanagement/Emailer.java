package customerrelationsmanagement;

import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
	Credentials credentials;
	private Session session;
	
	public Emailer(
	 Credentials credentials) {
		this.credentials = credentials;
		session = credentials.getSession();
	}
	
	private Message createNewMessage
	 (Session session, String myAccountEmail, String recipient, String subject)
	throws MessagingException {
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(myAccountEmail));
		
		message.setRecipient(
		 Message.RecipientType.TO,
		 new InternetAddress(recipient)
		);
		
		message.setSubject(subject);
		return message;
	} // End createNewMessage
	
	/** Extract the text content from a message */
	private String getTextFromMessage(Message message)
	throws MessagingException, IOException {
		
		String result = "";
		
		if(message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if(message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		} // End if
		return result;
	} // End getTextFromMessage
	
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
			} // End if
		} // End for
		return result.toString();
	} // End getTextFromMimeMultipart
	
	private Message prepareAttachedMessage
	 (Session session, String myAccountEmail, String recipient,
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
		} catch(Exception ex) {
			Logger.getLogger(Emailer.class.getName())
				  .log(Level.SEVERE, null, ex);
		} // End try-catch
		return null;
	} // End prepareAttachedMessage
	
	/** Prepare a message to be sent */
	private Message prepareMessage
	(String subject, String content, Session session,
	 String myAccountEmail, String recipient) {
		
		try {
			Message message =
			 createNewMessage(session, myAccountEmail, recipient, subject);
			message.setText(content);
			return message;
		} catch(Exception ex) {
			Logger.getLogger(Emailer.class.getName())
				  .log(Level.SEVERE, null, ex);
		} // End try-catch
		return null;
	} // End prepareMessage
	
	/**
	 * <p>
	 * Looks in the company inbox, and processes all product requests in each
	 * message body.
	 * Valid email orders should have each line in the csv format with
	 * <code>date,email,location,product_id,quantity</code>.
	 * for example:
	 * <code>2020-01-02,saust@hotmail.com,38813,3R8YXZCS820Y,2</code>
	 * </p>
	 * <p>
	 *
	 * @param crud
	 *  the Crud object to use for calls to the database.
	 *  </p>
	 */
	public void processEmails(Crud crud)
	throws MessagingException, IOException, SQLException {
		
		crud.setWorkingTable("sales");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		OrderProcessor orderProcessor = new OrderProcessor(crud);
		Message[] messages = credentials.getMessages(session);
		
		for(Message currentMessage: messages) {
			Order order = null;
			
			String[] messageText =
			 getTextFromMessage(currentMessage).trim().split("\n");
			
			Timestamp timestamp = new Timestamp(currentMessage.getSentDate().getTime());
			
			Matcher m = Order.EMAIL_PATTERN.matcher(
			 currentMessage.getFrom()[0].toString());
			
			String email = m.find() ? m.group("email") : "";
			
			try {
				for(String textLine: messageText) {
					String[] s = textLine.split(",");
					if(s.length != 4) {
						if(currentMessage.getSubject().toUpperCase()
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
						} // End if
						currentMessage.setFlag(Flags.Flag.DELETED, true);
						break;
					} // End if
					
					String productId = s[0].trim();
					int requestedQuantity = Integer.parseInt(s[1].trim());
					boolean isSale = Boolean.parseBoolean(s[2].trim());
					String location = s[3].trim();
					
					if(order == null) {
						order = new Order(timestamp, isSale, location);
						orderProcessor.setCurrentOrder(order);
					} // End if
					
					order.addProduct(new Product(
					 productId,
					 requestedQuantity));
				} // End for
				assert order != null;
				order.setEmail(email);
				orderProcessor.processOrder();
				
				sendMail(order.getCustomerEmail(), order
				 .getResponseSubject(), order
				 .getMessageText(), session, null);
				
				currentMessage.setFlag(Flags.Flag.DELETED, true);
			} catch(Exception e) {
				System.out.println(e.getMessage());
				// Todo: this email is in an improper format
			} // End try-catch
		} // End for
		orderProcessor.updateAndClose();
	}
	
	/**
	 * Send an email to a customer to indicate order confirmation or
	 * cancellation
	 *
	 * @param recipientAddress
	 *  the email address to send the message to.
	 * @param messageSubject
	 *  the subject of the email.
	 * @param messageContent
	 *  the main body of text in the email.
	 * @param emailSession
	 *  the session object to facilitate sending the message.
	 * @param reportFile
	 *  optional file to send, such as a report(leave null if not sending any
	 *  reports).
	 *
	 * @throws MessagingException
	 *  if the messaging service encounters an error.
	 */
	public void sendMail(String recipientAddress, String messageSubject,
						 String messageContent,
						 Session emailSession, File reportFile)
	
	throws MessagingException {
		
		Message message =
		 reportFile == null ?
		  prepareMessage(messageSubject, messageContent, emailSession,
		   credentials.getEmail(), recipientAddress)
		  : prepareAttachedMessage(emailSession, 
		credentials.getEmail(), recipientAddress, reportFile, messageSubject);
		
		assert message != null;
		Transport.send(message);
		System.out.println();
	} // End processEmails
} // End Emailer
