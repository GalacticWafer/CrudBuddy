import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.util.Map.entry;

class uploadCsvGui {
	private String[] columns;
	private String fileName;
	private final Crud crud;
	private static final Pair<String, String> PRIMARY_KEY = new Pair("idx", "int(16)");
	private static HashMap<Integer, String> typeMap;
	
	public void setColumns(String[] columns) {
		this.columns = columns;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static void setTypeMap(HashMap<Integer, String> typeMap) {
		uploadCsvGui.typeMap = typeMap;
	}
	
	public uploadCsvGui(String[] columns, String fileName, Crud crud) {
		this.columns = columns;
		this.fileName = fileName;
		this.crud = crud;
	}
	
	public void invoke() {
		GridBagConstraints constraints = new GridBagConstraints();
		int gridy = 0;
		GridBagLayout layout = new GridBagLayout();
		JFrame frame = new JFrame();
		JScrollPane scrollPane = new JScrollPane();
		JPanel panel = new JPanel(layout);
		Object[] boxOptions = J_TO_SQL.values().toArray();
		JRadioButton[] radioButtons = new JRadioButton[columns.length + 1];
		JLabel primaryColumnLabel = new JLabel("Primary Column:");
		JComboBox[] boxes = new JComboBox[columns.length];
		JLabel[] labels = new JLabel[columns.length];
		JTextField fileField = new JTextField(fileName);
		JLabel fileLabel = new JLabel("File Name:");
		fileField.setColumns(30);
		constraints.gridx = 0;
		panel.add(fileLabel, constraints);
		constraints.gridx = 1;
		panel.add(fileField, constraints);
		constraints.gridy = ++ gridy;
		constraints.gridx = 0;
		JLabel nameLabel = new JLabel("Table Name:");
		panel.add(nameLabel, constraints);
		JTextField nameField = new JTextField();
		nameField.setColumns(20);
		constraints.gridx = 1;
		panel.add(nameField, constraints);
		constraints.gridy = ++ gridy;
		constraints.gridx = 3;
		panel.add(primaryColumnLabel, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		constraints.gridy = ++ gridy;
		++ gridy;
		constraints.gridwidth = 1;
		ButtonGroup buttonGroup = new ButtonGroup();
		int i = 0;
		for(; i < boxes.length; i++) {
			constraints.gridy = ++ gridy;
			constraints.gridx = 0;
			labels[i] = new JLabel(columns[i]);
			panel.add(labels[i], constraints);
			constraints.gridx = 1;
			boxes[i] = new JComboBox(boxOptions);
			boxes[i].setEditable(true);
			boxes[i].setSelectedItem("VARCHAR(16)");
			panel.add(boxes[i], constraints);
			constraints.gridx = 3;
			radioButtons[i] = new JRadioButton("", false);
			buttonGroup.add(radioButtons[i]);
			panel.add(radioButtons[i], constraints);
		}
		radioButtons[i] = new JRadioButton("Add index column", true);
		buttonGroup.add(radioButtons[i]);
		constraints.gridy = i + ++ gridy;
		panel.add(radioButtons[i], constraints);
		JButton ok = new JButton("Ok");
		ok.addActionListener(e -> {
			
			if(nameField.getText().length() >
			   0 /*&& fileField.getText().length() > 0*/) {
				//Todo: create fileField to get fileName
				
				String tableName = nameField.getText().trim();
				final String finalFileName = fileField.getText().trim();
				Enumeration<AbstractButton> bs = buttonGroup.getElements();
				boolean foundButton = false;
				for(int j = 0; j < radioButtons.length - 1; j++) {
					JRadioButton radioButton = (JRadioButton)bs.nextElement();
					if(radioButton.isSelected() && bs.hasMoreElements()) {
						PRIMARY_KEY.setValue(J_TO_SQL.get(j));
						foundButton = true;
					}
				}
				if(! foundButton) {
					PRIMARY_KEY.setValue("idx".trim());
					PRIMARY_KEY.setValue("int(16)".trim());
				}
				typeMap = new HashMap<>();
				for(int j = 0; j < boxes.length; j++) {
					typeMap.put(j, (boxes[j].getSelectedItem() + "").trim());
				}
				try {
					batchSqlString(tableName, columns,
					 new Scanner(new File(finalFileName)));
				}
				catch(SQLException | FileNotFoundException throwables) {
					throwables.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		constraints.gridx = 0;
		constraints.gridy = ++ gridy;
		panel.add(ok, constraints);
		constraints.gridx = 1;
		JButton cancel = new JButton("Cancel");
		Dimension dim = frame.getContentPane().getPreferredSize();
		scrollPane.setMaximumSize(new Dimension(dim.width - 1, dim.height - 1));
		cancel.addActionListener(e -> {
			frame.remove(panel);
			frame.setVisible(false);
		});
		panel.add(cancel, constraints);
		scrollPane.getViewport().add(panel);
		frame.getContentPane().add(scrollPane);
		int sizeW = panel.getPreferredSize().width + 50;
		int sizeH = panel.getPreferredSize().height + 50;
		frame.setSize(new Dimension(sizeW, sizeH));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	/** Creates one sql string for an entire csv file, to create and populate a table. */
	private void batchSqlString
	(String tableName, String[] columns, Scanner scanner)
	throws SQLException {
		if(typeMap != null) {
			crud.insertTable(tableName, columns, typeMap);
			StringBuilder sf = new StringBuilder(String.format("INSERT INTO %s %s"
			 , tableName, crud.getColumnsTuple(columns)));
			String sqlDeclaration = sf.toString();
			if(scanner.hasNextLine()) {
				scanner.nextLine();
				int MAX_LOOPS = 100000;
				int i = 1;
				for(; i < MAX_LOOPS && scanner.hasNextLine(); i++) {
					String[] line = scanner.nextLine().split(",");
					String nextInsertion = crud.toValueTuple(line, typeMap);
					boolean isLastIteration = i == MAX_LOOPS - 1;
					sf.append(nextInsertion);
					if(i == MAX_LOOPS - 1) {sf.append(";");}
					else {sf.append(",");}
					if(isLastIteration) {
						crud.update(sf.toString());
						i = 0;
						sf.setLength(0);
						sf.append(sqlDeclaration);
					}
				}
				if(i > 1) {
					sf.replace(sf.length() - 1, sf.length(), ";");
					crud.updateF(sf.toString());
				}
			}
			scanner.close();
			JOptionPane.showMessageDialog(null, crud.format(
			 "The csv file has been exported to %s in the %s database.",
			 tableName, crud.getDatabaseName()));
		}
	}
	public static final Map<String, String> J_TO_SQL2 = Map
	 .ofEntries(
	  entry("VARCHAR", "VARCHAR(16)"),
	  entry("CHAR", "CHAR"),
	  entry("LONGVARCHAR", "VARCHAR(32)"),
	  entry("BOOLBIT", "BIT"),
	  entry("INT", "int(8)"),
	  entry("DOUBLE", "decimal(13,2)"),
	  entry("DATE", "DATE"),
	  entry("TIME", "TIME"),
	  entry("TIMESTAMP", "TIMESTAMP"));
	public static final int STRING = 1;
	public static final int CHAR = 2;
	public static final int LONGVARCHAR = 3;
	public static final int BOOLBIT = 4;
	public static final int NUMERIC = 5;
	public static final int TINYINT = 6;
	public static final int SMALLINT = 7;
	public static final int INTEGER = 8;
	public static final int BIGINT = 9;
	public static final int REAL = 10;
	public static final int FLOAT = 11;
	public static final int DOUBLE = 12;
	public static final int VARBINARY = 13;
	public static final int BINARY = 14;
	public static final int DATE = 15;
	public static final int TIME = 16;
	public static final int TIMESTAMP = 17;
	public static final int CLOB = 18;
	public static final int BLOB = 19;
	public static final int ARRAY = 20;
	public static final int REF = 21;
	public static final int STRUCT = 22;
	public static final Map<Integer, String> J_TO_SQL = Map
	 .ofEntries(
	  entry(STRING, "VARCHAR(16)"),
	  entry(CHAR, "CHAR"),
	  entry(LONGVARCHAR, "VARCHAR(32)"),
	  entry(BOOLBIT, "BIT"),
	  entry(NUMERIC, "NUMERIC"),
	  entry(TINYINT, "int(2)"),
	  entry(SMALLINT, "int(4)"),
	  entry(INTEGER, "int(8)"),
	  entry(BIGINT, "int(16)"),
	  entry(REAL, "REAL"),
	  entry(FLOAT, "FLOAT"),
	  entry(DOUBLE, "decimal(13,2)"),
	  entry(VARBINARY, "VARBINARY"),
	  entry(BINARY, "BINARY"),
	  entry(DATE, "DATE"),
	  entry(TIME, "TIME"),
	  entry(TIMESTAMP, "TIMESTAMP"),
	  entry(CLOB, "CLOB"),
	  entry(BLOB, "BLOB"),
	  entry(ARRAY, "ARRAY"),
	  entry(REF, "REF"),
	  entry(STRUCT, "STRUCT"));
}
