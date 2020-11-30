package customerrelationsmanagement;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javax.mail.*;
import java.text.ParseException;
import java.util.Properties;

public class Credentials {
	private String emailAddress;
	private String emailPassword;
	private Crud crud;
	
	public Credentials() {
		new Login(this);
	}
	
	public Crud getCrud() {
		
		return crud;
	}
	
	/**
	 * Return our burner email address
	 */
	public String getEmail() {
		
		return emailAddress;
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
		
		public Login(Credentials credentials) {
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			JTextField databaseUserNameField = new JTextField(20);
			JPasswordField databasePasswordField = new JPasswordField(20);
			JTextField databaseIPAddressField = new JTextField(20);
			JTextField databasePortField = new JTextField(20);
			JTextField databaseSchemaField = new JTextField(20);
			JTextField emailAddressField = new JTextField(20);
			JPasswordField emailPasswordField = new JPasswordField(20);
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
			loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			loginFrame.setLocationRelativeTo(null);
			loginFrame.setVisible(true);
			button.addActionListener(e -> {
				try {
					crud =
					 new Crud(
					  databaseUserNameField.getText(),
					  String.valueOf(databasePasswordField.getPassword()),
					  databaseIPAddressField.getText(),
					  databasePortField.getText(),
					  databaseSchemaField.getText()
					 );
					//loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					loginFrame.dispose();
					emailAddress = emailAddressField.getText();
					emailPassword =
					 String.valueOf(emailPasswordField.getPassword());
					new Main(credentials,crud);
				} catch(SQLException | ClassNotFoundException | FileNotFoundException throwables) {
					throwables.printStackTrace();
					crud = null;
					JOptionPane.showMessageDialog(
					 null, "You have not signed in yet");
				} catch(IOException | ParseException ioException) {
					ioException.printStackTrace();
				}
			});
		}
	}
}
