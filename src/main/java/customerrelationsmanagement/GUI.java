package customerrelationsmanagement;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class GUI {
	private static GridBagLayout BAG_LAYOUT = new GridBagLayout();
	private static JPanel CENTER_PANEL = new JPanel();
	private static final Color CLOSED_STATUS_FOREGROUND =
	 new Color(217, 85, 80);
	private static final Color DARK_GREY = new Color(20, 20, 20);
	private static JPanel EAST_PANEL = new JPanel();
	private static final Font FONT = new Font("Aharoni", Font.BOLD, 15);
	private static final Color GREY_110x3 = new Color(110, 110, 110);
	private static final Color GREY_50x3 = new Color(50, 50, 50);
	private static GridBagConstraints MIDDLE_CONSTRAINTS =
	 new GridBagConstraints();
	private static JPanel NORTH_PANEL = new JPanel();
	private static final Color OPEN_STATUS_FOREGROUND =
	 new Color(131, 224, 158);
	private static final Color PURE_WHITE = new Color(255, 255, 255);
	private static JPanel SOUTH_PANEL = new JPanel();
	private static final Color TABLE_FOREGROUND = new Color(125, 211, 224);
	private static JPanel WEST_PANEL = new JPanel();
	private static final Color centerBackground = GREY_50x3;
	private final Crud crud;
	private Object[][] data;
	private static JFrame frame;
	private DefaultTableModel model;
	private static JScrollPane scrollPane;
	private TableRowSorter sorter;
	private JTable table;
	private String tableName;
	private final JComboBox tableSelections;
	Object[][] tempData;
	private DefaultTableModel tempDataModel;
	String tempTable = "";
	
	public GUI(Crud crud) throws SQLException {
		this.crud = crud;
		setUIManager();
		scrollPane = new JScrollPane();
		table = new JTable();
		frame = new JFrame("CRUD Buddy");
		JLabel status = new JLabel("Status: " + checkConnection());
		status.setForeground(crud
		 .isClosed() ? CLOSED_STATUS_FOREGROUND : OPEN_STATUS_FOREGROUND);
		setFrameStyle(status);
		
		EAST_PANEL.setLayout(BAG_LAYOUT);
		CENTER_PANEL.setLayout(BAG_LAYOUT);
		tableSelections = new JComboBox(crud.getTableNames());
		
		tableSelections.addActionListener(e -> {
			tableName = tableSelections.getSelectedItem() + "";
			crud.setWorkingTable(tableName);
			try {
				refresh();
			}
			catch(SQLException throwables) {
				throwables.printStackTrace();
			}
		});
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		
		listRenderer
		 .setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center
		// -aligned 
		// items
		tableSelections.setBackground(GREY_110x3);
		tableSelections.setBorder(new LineBorder(GREY_110x3, 2));
		tableSelections.setForeground(TABLE_FOREGROUND);
		tableSelections.setFont(FONT);
		MIDDLE_CONSTRAINTS.gridx = 1;
		MIDDLE_CONSTRAINTS.gridy = 1;
		MIDDLE_CONSTRAINTS.insets = new Insets(0, 0, 10, 0);
		CENTER_PANEL.add(tableSelections, MIDDLE_CONSTRAINTS);
		crud.setWorkingTable("inventory");
		System.out.println(crud.getCurrentTable());
		createTable();
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		makeComponents(EAST_PANEL, CENTER_PANEL, MIDDLE_CONSTRAINTS);
		createFrame(NORTH_PANEL, EAST_PANEL, WEST_PANEL, SOUTH_PANEL,
		 CENTER_PANEL, status);
	}
	
	public void addTable(String tableName) {
		tableSelections.addItem(tableName);
		tableSelections.setSelectedItem(tableName);
	}
	
	private String checkConnection() throws SQLException {
		return crud.isClosed() ? "No Connection" : "Connected";
	}
	
	private void createFrame(JPanel north, JPanel east, JPanel west,
							 JPanel south, JPanel center,
							 JLabel status) {
		frame.setBounds(200, 400, 1300, 1007);
		north.add(status);
		frame.add(north, BorderLayout.NORTH);
		frame.add(east, BorderLayout.EAST);
		frame.add(west, BorderLayout.WEST);
		frame.add(south, BorderLayout.SOUTH);
		frame.add(center, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void createTable() throws SQLException {
		String[] columnNames = crud.getColumnNames();
		setFromDatabase(columnNames);
		setNewModel(columnNames);
	}
	
	private void makeComponents(JPanel east, JPanel center,
								GridBagConstraints middle) {
		
		JLabel user = new JLabel("Username:");
		user.setForeground(GREY_110x3);
		user.setFont(FONT);
		middle.anchor = GridBagConstraints.WEST;
		middle.gridx = 0;
		middle.gridy = 0;
		center.add(user, middle);
		JTextField username =
		 new JTextField(20); //creates textfield with 10 columns
		username.setBackground(GREY_110x3);
		username.setForeground(PURE_WHITE);
		username.setBorder(new LineBorder(DARK_GREY, 2));
		middle.insets =
		 new Insets(10, 4, 20, 0); //padding between textfields labels
		middle.anchor = GridBagConstraints.WEST;
		middle.gridx = 1;
		center.add(username, middle);
		
		JLabel pass = new JLabel("Password:");
		pass.setForeground(GREY_110x3);
		pass.setFont(FONT);
		middle.anchor = GridBagConstraints.WEST;
		middle.gridx = 2;
		center.add(pass, middle);
		JTextField password =
		 new JTextField(20); //creates textfield with 10 columns
		password.setBackground(GREY_110x3);
		password.setForeground(PURE_WHITE);
		password.setBorder(new LineBorder(DARK_GREY, 2));
		middle.anchor = GridBagConstraints.WEST;
		middle.weightx = 3;
		middle.gridx = 3;
		middle.gridy = 0;
		center.add(password, middle);
		
		middle.fill = GridBagConstraints.BOTH;
		middle.weightx = 0.0;
		middle.weighty = 0.9;
		middle.gridwidth = 4;
		middle.gridx = 0;
		middle.gridy = 2;
		table.setBackground(GREY_50x3);
		table.setForeground(TABLE_FOREGROUND);
		table.setGridColor(GREY_110x3);
		table.setFont(FONT);
		table.setSelectionBackground(GREY_110x3);
		table.setSelectionForeground(OPEN_STATUS_FOREGROUND);
		table.setDefaultEditor(Object.class, null);
		JTableHeader header = table.getTableHeader();
		header.setBackground(GREY_110x3);
		header.setBorder(new LineBorder(GREY_110x3));
		center.add(table, middle);
		
		scrollPane = new JScrollPane(table);
		scrollPane.setVisible(true);
		scrollPane.setBorder(new LineBorder(GREY_110x3, 2));
		scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI());
		scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI());
		center.add(scrollPane, middle);
		
		GridBagConstraints c = new GridBagConstraints();
		JLabel srch = new JLabel("Search:");
		srch.setForeground(GREY_110x3);
		srch.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 15, 0, 10);
		east.add(srch, c);
		JTextField search =
		 new JTextField(10); //creates textfield with 10 columns
		search.setBackground(GREY_110x3);
		search.setForeground(PURE_WHITE);
		search.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		east.add(search, c);
		
		JButton exportButton = new JButton("Export All");
		exportButton.setBackground(GREY_110x3);
		exportButton.setFont(FONT);
		exportButton.setForeground(TABLE_FOREGROUND);
		exportButton.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		east.add(exportButton, c);
		
		exportButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					sendEmail("gui.csv", data);
				}
				catch(FileNotFoundException | SQLException fileNotFoundException) {
					fileNotFoundException.printStackTrace();
				}
			}
		});
		
		JButton delete = new JButton("Delete Current");
		delete.setBackground(GREY_110x3);
		delete.setFont(FONT);
		delete.setForeground(TABLE_FOREGROUND);
		delete.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		east.add(delete, c);
		
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				// check for selected row first
				int selectedRow = table.getSelectedRow();
				if(table.getSelectedRow() != -1) {
					int rowIndex = table.convertRowIndexToModel(selectedRow);
					try {
						Object columnValue = Crud
						 .quoteWrap(table.getModel().getValueAt(rowIndex, 0));
						String columnName = table.getColumnName(0);
						crud.deleteRecord(columnName, columnValue);
					}
					catch(SQLException throwables) {
						throwables.printStackTrace();
					}
					model.removeRow(rowIndex);
					JOptionPane
					 .showMessageDialog(null, "Selected row deleted " +
											  "successfully");
				}
			}
		});
		
		JButton exportCurrent = new JButton("Export Current");
		exportCurrent.setBackground(GREY_110x3);
		exportCurrent.setFont(FONT);
		exportCurrent.setForeground(TABLE_FOREGROUND);
		exportCurrent.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		east.add(exportCurrent, c);
		
		exportCurrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[][] update =
				 new Object[table.getRowCount()][table.getColumnCount()];
				for(int row = 0; row < table.getRowCount(); row++) {
					for(int column = 0;
						column < table.getColumnCount(); column++) {
						update[row][column] = table.getModel().getValueAt(
						 table.convertRowIndexToModel(row), column);
					}
				}
				try {
					sendEmail("test.csv", update);
				}
				catch(FileNotFoundException fileNotFoundException) {
					fileNotFoundException.printStackTrace();
				}
				catch(SQLException throwables) {
					throwables.printStackTrace();
				}
				//System.out.println(Arrays.deepToString(update));
			}
		});
		
		JButton upload = new JButton("Upload Table");
		upload.setBackground(GREY_110x3);
		upload.setFont(FONT);
		upload.setForeground(TABLE_FOREGROUND);
		upload.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 4;
		east.add(upload, c);
		
		upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					crud.insertTableFromGui();
				}
				catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		
		JButton send = new JButton("Send Email");
		send.setBackground(GREY_110x3);
		send.setFont(FONT);
		send.setForeground(TABLE_FOREGROUND);
		send.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 5;
		east.add(send, c);
		
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				
			}
		});
		
		search.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				search(search.getText());
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				search(search.getText());
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				search(search.getText());
			}
			
			public void search(String str) {
				RowFilter<DefaultTableModel, Object> rf = null;
				ArrayList<RowFilter<DefaultTableModel, Object>> rfs =
				 new ArrayList<RowFilter<DefaultTableModel, Object>>();
				
				try {
					String text = search.getText();
					String[] textArray = text.split(" ");
					
					for(int i = 0; i < textArray.length; i++) {
						rfs.add(RowFilter
						 .regexFilter("(?i)" + textArray[i], 0, 1, 2, 4));
					}
					
					rf = RowFilter.andFilter(rfs);
				}
				catch(java.util.regex.PatternSyntaxException e) {
					return;
				}
				if(str.length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(rf);
				}
			}
		}); //end of search filter
		
		JButton assetsOT = new JButton("Assets OT");
		assetsOT.setBackground(GREY_110x3);
		assetsOT.setFont(FONT);
		assetsOT.setForeground(TABLE_FOREGROUND);
		assetsOT.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 6;
		east.add(assetsOT, c);
		assetsOT.addActionListener(e -> {
			
			Charts lp = new Charts(crud);
			try {
				//ChartPanel cp = new ChartPanel(lp.getTimeSeriesChart(JOptionPane.showInputDialog(null,
				//	 "Enter a date"), Chart.ASSET));
				ChartPanel cp = new ChartPanel(lp.getChart(JOptionPane.showInputDialog(null,
					 "Enter a date"), ChartType.BAR_TOP_CUSTOMERS));
				JFrame chartFrame = new JFrame();
				chartFrame.getContentPane().add(cp);
				chartFrame.pack();
				chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				chartFrame.setLocationRelativeTo(null);
				chartFrame.setVisible(true);
				
				//TODO: Store the JFreeChart (assetChart) DO WHATEVER DAVID WITH CP
			}
			catch(SQLException throwables) {
				throwables.printStackTrace();
			}
			//try {
			//	analyze.generateTimePlot();
			//} catch (SQLException | IOException throwables) {
			//	throwables.printStackTrace();
			//}
		});

			/*Object[][] data = new Object[0][];
			try {
				data = crud.getAssetTotal("");
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
			System.out.println(data[0][0]);
			java.util.List<Object[]> yearToDateData = new ArrayList<>();
			List<Date> dates = new ArrayList<>();
			Date Jan = StringToDate("2020-02-31");
			for (int i = 0; i < data.length; i++) {
				yearToDateData.add(data[i]);
				dates.add((Date)data[i][0]);
				if (dates.get(i).compareTo(Jan) > 0){
					try {
						makePlot("MYCROWSAWFT", "Assets", yearToDateData, dates.get(i));
					} catch (SQLException | IOException throwables) {
						throwables.printStackTrace();
					}
					//save();
				}
			}
			LinePlot ytd = new LinePlot("MYCROWSAWFT", "Assets", yearToDateData);
			ytd.pack();
			RefineryUtilities.positionFrameRandomly(ytd);
			ytd.setVisible(true);
		}); */
		/*analyzer.addActionListener(e -> {

			String newTableName = null;
			try {
				int count = Integer.parseInt(JOptionPane.showInputDialog(
				 "Please enter the amount records you would like to see"));

				String d = JOptionPane.showInputDialog(
				 null,
				 "what date would you like to see results for? (leave blank " +
				 "for all-time");
				LocalDate date = null;
				if(d != "") {
					date = LocalDate.parse(d);
				}

				boolean isDescending =
				 JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog
				  (null, "Should the results be ascending?\"",
				   "Is Descending", JOptionPane.YES_NO_OPTION);

				newTableName =
				 crud.mostOrderedProducts((date == null ? null :date.toString
				 ()), count, isDescending, this);
				crud.setWorkingTable(newTableName);
				Object[][] description =
				 crud.getRecords(crud.query("describe " + newTableName));
				String[] columnNames = new String[description.length];
				for(int i = 0; i < columnNames.length; i++) {
					columnNames[i] = String.valueOf(description[i][0]);
				}
				setTempData(columnNames, crud
				 .getRecords(crud.query("select * from " + newTableName)));
			}
			catch(SQLException throwables) {
				throwables.printStackTrace();
			}

			*//*JOptionPane
			 .showMessageDialog
			  (null,
			   "Uriel,\n\tPlease make it so that when this button is pressed,
			   \n " +
			   "it does the stuff in Main.main() instead of this message." +
			   "Make sure to take in an 'int' first, to pass into the
			   topNCustomers() function.\n" +
			   "That code in main()  is responsible for displaying
			   topNCustomers() results.\n" +
			   " seen at the beginning of this program, which is our analytics
			   .\n" +
			   "After that, when any other table is selected from the
			   drop-down menu of\n" +
				"tables, remove the one associated with this table, and\n" +
			   "call crud.setWorkingTable(<some_other_table_string_name)");*//*
		});*/
	}




	private void refresh() throws SQLException {
		setFromDatabase(crud.getColumnNames());
		DefaultTableModel dm = (DefaultTableModel)table.getModel();
		dm.setDataVector(data, crud.getColumnNames());
		dm.fireTableDataChanged();
	}
	
	private void sendEmail(String fileName, Object[][] data)
	throws FileNotFoundException, SQLException {
		File report = new File(fileName);
		
		PrintWriter dataWriter = new PrintWriter(report);
		dataWriter.println(String.join(",",crud.getColumnNames()));
		for(int i = 0; i < data.length; i++) {
			Object[] row = data[i];
			for(int j = 0; j < row.length; j++) {
				dataWriter.print(row[j]);
				if(j < row.length - 1) {
					dataWriter.print(",");
				}
			}
			dataWriter.println();
		}
		dataWriter.close();
	}  // End sendFile
	
	private void setFrameStyle(JLabel status) {
		status.setFont(FONT);
		CENTER_PANEL.setBackground(centerBackground);
		CENTER_PANEL.setOpaque(true);
		NORTH_PANEL.setBackground(DARK_GREY);
		NORTH_PANEL.setOpaque(true);
		EAST_PANEL.setBackground(GREY_50x3);
		EAST_PANEL.setOpaque(true);
		WEST_PANEL.setBackground(GREY_50x3);
		WEST_PANEL.setOpaque(true);
		SOUTH_PANEL.setBackground(GREY_50x3);
		SOUTH_PANEL.setOpaque(true);
		frame.getContentPane().setBackground(GREY_50x3);
	}
	
	public void setFromArray(Object[][] newData, String[] columnNames)
	throws SQLException {
		this.data = newData;
		setNewModel(columnNames);
	}
	
	private void setFromDatabase(String[] columnNames) throws SQLException {
		ResultSet rs = crud.query("SELECT * FROM " + crud.getCurrentTable());
		data = new Object[crud.size()][columnNames.length];
		for(int i = 0; rs.next() && i < data.length; i++) {
			data[i] = new Object[columnNames.length];
			for(int j = 0; j < columnNames.length; j++) {
				data[i][j] = rs.getObject(columnNames[j]);
			}
		}
	}
	
	private void setNewModel(String[] columnNames) {
		model = new DefaultTableModel(data, columnNames);
		model.setDataVector(data, columnNames);
		table.setModel(model);
		scrollPane.add(table);
		model.fireTableDataChanged();
		table.repaint();
	}
	
	public void setTempData(String[] columnNames, Object[][] newData)
	throws SQLException {
		model = new DefaultTableModel(newData, columnNames);
		table.setModel(model);
		model.fireTableDataChanged();
		table.repaint();
		//setNewModel(columnNames);
	}
	
	private void setUIManager() {
		UIManager.put("ScrollBar.thumb", new ColorUIResource(GREY_110x3));
		UIManager
		 .put("ScrollBar.thumbDarkShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(GREY_50x3));
		UIManager
		 .put("ScrollBar.thumbHighlight", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.track", new ColorUIResource(GREY_50x3));
	}
}
