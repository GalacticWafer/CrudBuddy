package customerrelationsmanagement;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
		}
		catch(Exception ex) {
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
		}
		catch(Exception ex) {
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
		
		crud.setWorkingTable("statused_sales");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		OrderProcessor orderProcessor = new OrderProcessor(crud);
		Message[] messages = credentials.getMessages(session);
		String reccommend1 = null;
		
		for(Message currentMessage: messages) {
			Order order = null;
			
			String[] messageText =
			 getTextFromMessage(currentMessage).trim().split("\n");
			
			Timestamp timestamp =
			 new Timestamp(currentMessage.getSentDate().getTime());
			
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
							 "SELECT * FROM statused_sales where order_id = '"
							 + orderId + "'" + " and order_status = " +
							 Order.PROCESSED);
							System.out.println(
							 " The following product purchases should be " +
							 "cancelled:\n\n" +
							 Arrays.deepToString(records));
							String[] recordsString =
							 new String[records.length];
							for(int i = 0; i < recordsString.length; i++) {
								recordsString[i] = records[i][4].toString();
							}
							String cancelString =
							 String.join("\n", recordsString);
							if(records.length != 0) {
								crud.update(
								 "update statused_sales set order_status = -1 " +
								 "where order_id = '" +
								 orderId + "'");
								sendMail(
								 email, "Cancellation",
								 " The following product purchases should be" +
								 " " +
								 "cancelled:\n\n" +
								 cancelString, session, null);
								
								continue;
							}
							
							break;
						} // End if
						currentMessage.setFlag(Flags.Flag.DELETED, true);
						break;
					} // End if
					
					String productId = s[0].trim();
					reccommend1 = productId;
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
				if(order != null) {
					order.setEmail(email);
					orderProcessor.processOrder();
					
					Iterator<Product> products = order.productIterator();
					
					TreeSet<Product> productSet = new TreeSet<>();
					
					while(products.hasNext()) {
						Product nextProduct = products.next();
						productSet.add(nextProduct);
						// get the recommended
					}
					
					HashMap<String, Integer> productSumMap = new HashMap<>();
					
					for(Product product: productSet) {
						Object[][] result =
						 recommendations(crud, product.getId(), order.orderId);
						for(Object[] o: result) {
							String recommendedProduct = (String)o[0];
							int associatedQuantity = (Integer)o[1];
							if(productSumMap.containsKey(recommendedProduct)) {
								Integer currentSum =
								 productSumMap.get(recommendedProduct);
								productSumMap.put(recommendedProduct,
								 associatedQuantity + currentSum);
							} else {
								productSumMap
								 .put(recommendedProduct, associatedQuantity);
							}
						}
						ArrayList<Map.Entry<String, Integer>> bestRecommendations = new ArrayList<>();
						int topInt = 0;
						Iterator<Map.Entry<String, Integer>> it =
						 productSumMap.entrySet()
									  .iterator();
						for(int i = 0; i < productSumMap.size(); i++) {
							Map.Entry<String, Integer> entry = it.next();
							int testInt = entry.getValue();
							if(bestRecommendations.size() > 2) {
								for(Map.Entry<String, Integer> bestRecommendation:
								 bestRecommendations) {
									if(bestRecommendation.getValue() < testInt) {
										//switch em
									}
								}
							}
						}
					}
				}
				
				sendMail(order.getCustomerEmail(), order
				 .getResponseSubject(), order
										 .getMessageText() + "", session,
				 null);
			}
			
			currentMessage.setFlag(Flags.Flag.DELETED, true);
		} catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
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
		  credentials.getEmail(), recipientAddress, reportFile,
		  messageSubject);
		
		assert message != null;
		Transport.send(message);
		System.out.println();
	} // End processEmails
	
	public Object[][] recommendations(Crud crud, String product,
									  String orderid)
	throws SQLException {
		
		String sql = "select s2.product_id, sum(s2.product_quantity) from " +
					 "statused_sales\n" +
					 "                                                       " +
					 " " +
					 "as s1 inner join statused_sales as s2 on s1.order_id =" +
					 " " +
					 "s2.order_id\n" +
					 "where s1.product_id = '" + product + "'\n" +
					 "  and s2.product_id not like '" + product + "'\n" +
					 "  and s1.order_id not like '" + orderid + "'\n" +
					 "group by s2.product_id;";
		
		ResultSet rs = crud.query(sql);
		Object[][] objects = new Object[crud.rowCountResults(rs)][2];
		for(int i = 0; rs.next(); i++) {
			objects[i] = new Object[] {
			 rs.getString(1),
			 rs.getInt(2)
			};
		}
		return objects;
	}
} // End Emailer
