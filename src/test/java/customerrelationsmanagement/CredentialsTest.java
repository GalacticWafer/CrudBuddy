package customerrelationsmanagement;

import org.junit.runner.JUnitCore;

import javax.mail.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

public class CredentialsTest {
	private final int EMAIL_ADDRESS = 5;
	public static final int EMAIL_PASSWORD = 6;
	private String databaseIPAddress;
	private String databasePassword;
	private String databasePort;
	private String databaseSchema;
	private String databaseUserName;
	private String emailAddress;
	private String emailPassword;
	private Crud queryMaker;
	
	public CredentialsTest(Testers test) { new Login(this, test); }
	
	public Crud getCrud() {
		
		return queryMaker;
	}
	
	/**
	 * Return our burner email address
	 */
	public String getEmail() {
		
		return emailAddress;
	}
	
	private String getField(JComponent field) {
		
		if(field instanceof JTextField) {
			return ((JTextField)field).getText();
		} else if(field instanceof JPasswordField) {
			JPasswordField passwordField = (JPasswordField)field;
			return String.join("", Arrays
			 .toString(passwordField
			  .getPassword()));
		}
		return null;
	}
	
	/**
	 * Return our burner email's inbox
	 */
	public Message[] getMessages(Session session)
	throws MessagingException {
		
		Properties pro = System.getProperties();
		pro.setProperty("mail.store.protocol", "imaps");
		try {
			Store store = session.getStore("imaps");
			store
			 .connect("imap.gmail.com", emailAddress, emailPassword);
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);
			return inbox.getMessages();
		} catch(MessagingException e) {
			throw new MessagingException();
		}
	}
	
	/**
	 * Return a new email session
	 */
	public Session getSession() {
		
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		return Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication(emailAddress, emailPassword);
			}
		});
	}
	
	public enum Testers {
		CrudTester,
		EmailerTester,
		GUITester,
		OrderProcessorTester;
	}
	
	private class Login {
		String[] fields =
		 new String[] {
		  "Database User Name",
		  "Database Password",
		  "Database IP Address",
		  "Database Port",
		  "Database Schema",
		  "Email Address",
		  "Email Password"
		 };
		JFrame loginFrame = new JFrame("Credentials");
		
		public Login(CredentialsTest credentials, Testers test) {
			
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			JTextField databaseUserNameField = new JTextField(20);
			JPasswordField databasePasswordField = new JPasswordField(20);
			JTextField databaseIPAddressField = new JTextField(20);
			JTextField databasePortField = new JTextField(20);
			JTextField databaseSchemaField = new JTextField(20);
			JTextField emailAddressField = new JTextField(20);
			JPasswordField emailPasswordField = new JPasswordField(20);
			JLabel[] labels = new JLabel[fields.length];
			JButton button = new JButton("Connect");
			
			JComponent[] components = {
			 databaseUserNameField, databasePasswordField,
			 databaseIPAddressField, databasePortField,
			 databaseSchemaField, emailAddressField, emailPasswordField
			};
			int i = 0;
			for(; i < components.length; i++) {
				// add label
				constraints.gridy = i;
				panel.add(new JLabel(fields[i]), constraints);
				panel.add(components[i], constraints);
			}
			constraints.gridx++;
			constraints.gridy++;
			panel.add(button, constraints);
			
			loginFrame.getContentPane().add(panel);
			loginFrame.pack();
			loginFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			loginFrame.setLocationRelativeTo(null);
			loginFrame.setVisible(true);
			button.addActionListener(e -> {
				try {
					queryMaker =
					 new Crud(
					  databaseUserNameField.getText(),
					  String.valueOf(databasePasswordField.getPassword()),
					  databaseIPAddressField.getText(),
					  databasePortField.getText(),
					  databaseSchemaField.getText()
					 );
					emailAddress = emailAddressField.getText();
					emailPassword =
					 String.valueOf(emailPasswordField.getPassword());
					switch(test) {
						case CrudTester ->  CrudTest.invoke(credentials);
						case EmailerTester -> EmailerTest.invoke(credentials);
						case GUITester -> GUITest.invoke(credentials);
						case OrderProcessorTester -> OrderProcessorTest.invoke(credentials);
					}
				} catch(SQLException | ClassNotFoundException throwables) {
					throwables.printStackTrace();
					queryMaker = null;
					JOptionPane.showMessageDialog(
					 null, "You have not signed in yet");
				}
			});
		}
	}
}
