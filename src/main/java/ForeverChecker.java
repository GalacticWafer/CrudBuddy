import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

public class ForeverChecker {
	public static void main(String args[])
	throws SQLException, ClassNotFoundException, IOException,
		   MessagingException {
		
		Crud crud = Credentials.databaseLogin();
		Emailer.processEmailOrders(crud);
	}
}