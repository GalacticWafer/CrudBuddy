package customerrelationsmanagement;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

	/**
	 * Can be used to access the inbox folder to process emails/orders
	 *
	 * @param credentials
	 *  hold relative information for connecting to database and email
	 */
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
		
		/*
		String[][] test = { new String[]{"RHPXPHGBJS1P,900,true,29384","17TXG621YHM0,1,true,29384"},
							new String[]{"MC8QH9N14OC6,1,true,29384", "3DG9X9C79GR5,1,true,29384" },
							new String[]{"7GBS3ZBYMVEL,1,true,29384", "9F1BBF77YJ7Y,1,true,29384" },
							new String[]{"8FNQ9Q99UAU8,1,true,29384", "S7PTXR74PM50,1,true,29384" },
							new String[]{"T7LV3UP0N991,1,true,29384", "G0L5248Q2Z3F,1,true,29384" }};
		
		for(int i = 0; i < test.length; i++) {
			sendMail(credentials.getEmail(), "", String.join("\n", test[i]),credentials.getSession(),null);
		}
		
		 */

		crud.setWorkingTable("statused_sales");
		OrderProcessor orderProcessor = new OrderProcessor(crud);
		Message[] messages = credentials.getMessages(session);


		HashMap<String, String> emailsNOrder = new HashMap<>();

		for(Message currentMessage: messages) {
			Order order = null;

			String[] messageText =
					getTextFromMessage(currentMessage).trim().split("\n");

			DateTime time =
					new DateTime(currentMessage.getSentDate().getTime());

			Matcher m = Order.EMAIL_PATTERN.matcher(
					currentMessage.getFrom()[0].toString());

			String email = m.find() ? m.group("email") : credentials.getEmail();

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
											"'" + Status.PROCESSED + "'");
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
										"update statused_sales set order_status = " +
												"-1" +
												" " +
												"where order_id = '" +
												orderId + "'");
								sendMail(
										email, "Cancellation",
										"       The following product purchases have been cancelled:" +
												"\n\n" +
												cancelString, credentials.getSession(), null);

								continue;
							}
							break;
						} // End if
						currentMessage.setFlag(Flags.Flag.DELETED, true);
						break;
					} // End if

					String productId = s[0].trim();
					int requestedQuantity = Integer.parseInt(s[1].trim());
					EventType isSale = EventType.parse(s[2].trim());
					String location = s[3].trim();

					if(order == null) {
						order = new Order(time, isSale, location);
						orderProcessor.setCurrentOrder(order);
					} // End if

					order.addProduct(new Product(
							productId,
							requestedQuantity));
				} // End for
				if(order != null) {

					order.setEmail(email);
					orderProcessor.processOrder();

					sendMail(order.getCustomerEmail(), order
									.getResponseSubject(), order.getMessageText() + "",
							credentials.getSession(), null);


					if(order.getResponseSubject().toUpperCase().contains("PROCESSED")){
						emailsNOrder.put(order.orderId,order.getCustomerEmail());
					}
				}

				currentMessage.setFlag(Flags.Flag.DELETED, true);
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
				System.out.println(e.getStackTrace());
			} // End try-catch
		} // End for

		orderProcessor.updateAndClose();
		for(Map.Entry<String, String> entry : emailsNOrder.entrySet()){

			String orderIdR = entry.getKey();
			String orderEmail = entry.getValue();

			String recommendPr = recommendProducts(3, orderIdR);
			if(recommendPr.length() != 0) {
				sendMail(orderEmail,
						"We thought you might like these!",
						recommendPr + "", credentials.getSession(),
						null);
			}

		}

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
	/**
	 * Returning a statement for recommendations that will be sent
	 * in a email after an order has been confirmed
	 *
	 * @param limit
	 *  max of suggestive products
	 * @param orderId
	 *  to find matching recommendations against the current order
	 *
	 * @return a string for the message body
	 *
	 * @throws SQLException if a parameter of the query is wrong or
	 * the connection is interrupted
	 */
	@NotNull private String recommendProducts(int limit, String orderId)
			throws SQLException {

		String orderItemIds =
				"    SELECT `product_id`\n" +
				"    FROM `statused_sales`\n" +
				"    WHERE `order_id` = '" + orderId
		
		String otherOrdersWithSameItems = 
				"    SELECT `order_id` FROM `statused_sales`\n" +
				"    WHERE  `product_id` IN(\n" +
				         orderItemIds "'\n" +
				"    )\n" +
				"    AND `order_id` NOT LIKE '" + orderId +
		String query =
				"SELECT product_id as `We thought you might also like:`\n " +
				"FROM `statused_sales`\n" +
				"WHERE `order_id` IN (\n" +
				    otherOrdersWithSameItems +
				")\n" +
				"AND `product_id` NOT IN (\n" +
				"    " + orderItemIds "'\n" +
				")\n" +
				"GROUP BY `product_id` " +
				"ORDER BY sum(`product_quantity`)" +
				"DESC LIMIT " + limit;

		Crud crud = credentials.getCrud();
		ResultSet rs = crud.query(query);

		if(crud.rowCountResults(rs) == 0){
			rs = crud.query("select product_id from statused_sales where product_id not in " +
					"(select product_id from statused_sales where order_id = '" + orderId +  "') " +
					"order by product_quantity DESC limit " + limit);
		}

		StringBuilder out =
				new StringBuilder("Check out our hottest products!\n\n");
		while(rs.next()) {
			out.append(rs.getString(1)).append("\n");
		}
		return out.toString();
	}
} // End Emailer
