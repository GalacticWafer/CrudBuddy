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
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class GUI {
	private static final Color CLOSED_STATUS_FOREGROUND = new Color(217, 85, 80);
	private static final Color DARK_GREY = new Color(20, 20, 20);
	private static final Font FONT = new Font("Aharoni", Font.BOLD, 15);
	private static final Color GREY_110x3 = new Color(110, 110, 110);
	private static final Color GREY_50x3 = new Color(50, 50, 50);
	private static final Color OPEN_STATUS_FOREGROUND = new Color(131, 224, 158);
	private static final Color PURE_WHITE = new Color(255, 255, 255);
	private static final Color TABLE_FOREGROUND = new Color(125, 211, 224);
	private static final Color centerBackground = GREY_50x3;
	private static GridBagLayout BAG_LAYOUT = new GridBagLayout();
	private static JPanel CENTER_PANEL = new JPanel();
	private static JPanel EAST_PANEL = new JPanel();
	private static GridBagConstraints MIDDLE_CONSTRAINTS = new GridBagConstraints();
	private static JPanel NORTH_PANEL = new JPanel();
	private static JPanel SOUTH_PANEL = new JPanel();
	private static JPanel WEST_PANEL = new JPanel();
	private static JFrame frame;
	private static JScrollPane scrollPane;
	private JTable table;
	private TableRowSorter sorter;
	private final Crud crud;
	private DefaultTableModel model;
	private Object[][] data;
	private String tableName;
	
	public GUI(Crud crud) throws SQLException {
		this.crud = crud;
		setUIManager();
		scrollPane = new JScrollPane();
		table = new JTable();
		frame = new JFrame("CRUD Buddy");
		JLabel status = new JLabel("Status: " + checkConnection());
		status.setForeground(crud.isClosed() ? CLOSED_STATUS_FOREGROUND : OPEN_STATUS_FOREGROUND);
		setFrameStyle(status);
		
		EAST_PANEL.setLayout(BAG_LAYOUT);
		CENTER_PANEL.setLayout(BAG_LAYOUT);
		JComboBox tableSelections = new JComboBox(crud.getTableNames());
		
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
		listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned 
		// items
		tableSelections.setBackground(GREY_110x3);
		tableSelections.setBorder(new LineBorder(GREY_110x3, 2));
		tableSelections.setForeground(TABLE_FOREGROUND);
		tableSelections.setFont(FONT);
		MIDDLE_CONSTRAINTS.weightx = 0.5;
		MIDDLE_CONSTRAINTS.gridx = 0;
		MIDDLE_CONSTRAINTS.gridy = 0;
		MIDDLE_CONSTRAINTS.insets = new Insets(0, 0, 25, 0);  //top padding
		CENTER_PANEL.add(tableSelections, MIDDLE_CONSTRAINTS);
		crud.setWorkingTable("inventory");
		System.out.println(crud.getWorkingTable());
		createTable();
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		makeComponents(EAST_PANEL, CENTER_PANEL, MIDDLE_CONSTRAINTS);
		createFrame(NORTH_PANEL, EAST_PANEL, WEST_PANEL, SOUTH_PANEL, CENTER_PANEL, status);
	}
	
	private void setUIManager() {
		UIManager.put("ScrollBar.thumb", new ColorUIResource(GREY_110x3));
		UIManager.put("ScrollBar.thumbDarkShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.track", new ColorUIResource(GREY_50x3));
	}
	
	private String checkConnection() throws SQLException {
		return crud.isClosed() ? "No Connection" : "Connected";
	}
	
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
	
	private void refresh() throws SQLException {
		setFromDatabase(crud.getColumnNames());
		DefaultTableModel dm = (DefaultTableModel)table.getModel();
		dm.setDataVector(data, crud.getColumnNames());
		dm.fireTableDataChanged();
	}
	
	private void createTable() throws SQLException {
		String[] columnNames = crud.getColumnNames();
		setFromDatabase(columnNames);
		setNewModel(columnNames);
	}
	
	private void makeComponents(JPanel east, JPanel center, GridBagConstraints middle) {
		middle.fill = GridBagConstraints.HORIZONTAL;
		middle.ipady = 380;      //make this component tall
		middle.weightx = 0.0;
		middle.gridwidth = 6;
		middle.gridx = 0;
		middle.gridy = 1;
		table.setBackground(GREY_50x3);
		table.setForeground(TABLE_FOREGROUND);
		table.setGridColor(GREY_110x3);
		table.setFont(FONT);
		table.setSelectionBackground(GREY_110x3);
		table.setSelectionForeground(OPEN_STATUS_FOREGROUND);
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
		JTextField  search = new JTextField(10); //creates textfield with 10 columns
		search.setBackground(GREY_110x3);
		search.setForeground(PURE_WHITE);
		search.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		east.add(search, c);

		JButton exportButton = new JButton("Export"); //creates textfield with 10 columns
		exportButton.setBackground(GREY_110x3);
		exportButton.setFont(FONT);
		exportButton.setForeground(TABLE_FOREGROUND);
		exportButton.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		east.add(exportButton, c);

		exportButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e)
			{
				try
				{
					sendEmail("gui.csv");
				}
				catch(FileNotFoundException | SQLException fileNotFoundException)
				{
					fileNotFoundException.printStackTrace();
				}
			}
		});

		JButton testButton = new JButton("TEST IGNORE"); //creates textfield with 10 columns
		testButton.setBackground(GREY_110x3);
		testButton.setFont(FONT);
		testButton.setForeground(TABLE_FOREGROUND);
		testButton.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		east.add(testButton, c);

		testButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for(int row = 0;row < table.getRowCount();row++) {
							System.out.println(table.getModel().getValueAt(table.convertRowIndexToModel(row), 0));
						}
					}
				}
		);

		search.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				search(search.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				search(search.getText());
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				search(search.getText());
			}
			public void search(String str) {
				if (str.length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(RowFilter.regexFilter(str));
				}
			}
		}); //end of search filter
	}
	
	private void createFrame(JPanel north, JPanel east, JPanel west, JPanel south, JPanel center,
							 JLabel status) {
		frame.setBounds(200, 400, 1300, 620);
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
	
	private void setFromDatabase(String[] columnNames) throws SQLException {
		ResultSet rs = crud.getAllRecords();
		data = new Object[crud.size()][columnNames.length];
		for(int i = 0; rs.next() && i < data.length; i++) {
			data[i] = new Object[columnNames.length];
			for(int j = 0; j < columnNames.length; j++) {
				data[i][j] = rs.getObject(columnNames[j]);
			}
		}
	}
	
	public void setFromArray(Object[][] newData, String[] columnNames) throws SQLException {
		this.data = newData;
		setNewModel(columnNames);
	}
	
	private void setNewModel(String[] columnNames) {
		model = new DefaultTableModel(data, columnNames);
		model.setDataVector(data, columnNames);
		table.setModel(model);
		scrollPane.add(table);
		model.fireTableDataChanged();
		table.repaint();
	}

	private void sendEmail(String fileName) throws FileNotFoundException, SQLException
	{
		/*
		 * PSUEDO CODE
		 *
		 * HIGHER LEVEL
		 * 1. Take data
		 * 2. write to a csv
		 * 3. export csv
		 *
		 * Next Level
		 * 1.Loop through data
		 * 2. Put data in Object[][] dat
		 * */

		File report = new File(fileName);

		PrintWriter dataWriter = new PrintWriter(report);
		dataWriter.println(crud.arrayToCSV(crud.getColumnNames()));
		for(int i = 0; i < data.length; i++)
		{
			Object[] row = data[i];
			for(int j = 0; j < row.length; j++)
			{
				dataWriter.print(row[j]);
				if(j < row.length - 1) {
					dataWriter.print(",");
				}
			}
			dataWriter.println();
		}
		dataWriter.close();
	}  // End sendFile
}
