import java.io.FileNotFoundException;

public class RowDriver {
	private static final String FILE_NAME = "inventory_team4.csv";
	public static Table table;
	
	public static void main(String[] args) throws FileNotFoundException {
		table = new Table(FILE_NAME, "sprint_1_table");
		
	}
}

