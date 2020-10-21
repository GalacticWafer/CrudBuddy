import javax.mail.*;
import java.sql.SQLException;
import java.util.Properties;

public class Credentials {
	static String userName = "GalacticWafer";
	static String password = "7!qaJ|B[t$";
	static String ipAddress = "45.79.55.190";
	static String portNumber = "3306";
	static String databaseName = "cs3250_project";
	
	public static Crud databaseLogin() throws SQLException, ClassNotFoundException {
		return new Crud(userName, password, ipAddress, portNumber, databaseName);
	}
	
	/**Return our burner email address*/
	public static String getEmail() {
		return "mycrowsawftburner@gmail.com";
	}
	
	/**Return our burner email's inbox*/
	public static Folder getInbox() throws MessagingException {
		Properties pro = System.getProperties();
		pro.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getDefaultInstance(pro, null);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", "mycrowsawftburner@gmail.com", "CS3250TEAM4");
			return store.getFolder("Inbox");
		}
		catch(MessagingException e) { throw new MessagingException(); }
	}

	/**Return a new email session*/
	public static Session getSession() {
		Properties properties = new Properties();
		
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		
		String myAccountEmail = "mycrowsawftburner@gmail.com";
		String password = "CS3250TEAM4";
		
		return Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myAccountEmail, password);
			}
		});
	}
}
