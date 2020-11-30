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
import java.text.ParseException;
import java.util.ArrayList;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.text.*;

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
	private JFrame analyticsFrame;
	private static final Color centerBackground = GREY_50x3;
	private ChartMaker chartMaker;
	private ChartPanel chartPanel;
	private final Crud crud;
	private Object[][] data;
	private static JFrame frame;
	private DefaultTableModel model;
	private JTextField queryDate;
	private static JScrollPane scrollPane;
	private TableRowSorter sorter;
	private JTable table;
	private String tableName;
	private final JComboBox tableSelections;
	private JButton[] ytdButtons;
	
	public GUI(Crud crud) throws SQLException, ParseException {
		
		this.crud = crud;
		chartMaker = new ChartMaker(crud);
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
			} catch(SQLException throwables) {
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
	
	private void displayChart(ChartPanel chartPanel, String dateString,
							  ChartType chartType) {
		
		try {
			chartPanel = new ChartPanel(chartMaker
			 .getChart(dateString, chartType));
		} catch(SQLException | ParseException throwables) {
			throwables.printStackTrace();
		}
		analyticsFrame = new JFrame();
		analyticsFrame.getContentPane().add(chartPanel);
		analyticsFrame
		 .setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		analyticsFrame.setLocationRelativeTo(null);
		analyticsFrame.pack();
		analyticsFrame.setVisible(true);
	}
	
	private void makeComponents(JPanel east, JPanel center,
								GridBagConstraints middle)
	throws ParseException {
		
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
		setButtonStyle(exportButton);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		east.add(exportButton, c);
		
		exportButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				
				try {
					sendEmail("gui.csv", data);
				} catch(FileNotFoundException | SQLException fileNotFoundException) {
					fileNotFoundException.printStackTrace();
				}
			}
		});
		
		JButton delete = new JButton("Delete Current");
		setButtonStyle(delete);
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
					} catch(SQLException throwables) {
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
		setButtonStyle(exportCurrent);
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
				} catch(FileNotFoundException fileNotFoundException) {
					fileNotFoundException.printStackTrace();
				} catch(SQLException throwables) {
					throwables.printStackTrace();
				}
				//System.out.println(Arrays.deepToString(update));
			}
		});
		
		JButton upload = new JButton("Upload Table");
		setButtonStyle(upload);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 4;
		east.add(upload, c);
		
		upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				
				try {
					crud.insertTableFromGui();
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		
		JButton send = new JButton("Send Email");
		setButtonStyle(send);
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
				} catch(java.util.regex.PatternSyntaxException e) {
					return;
				}
				if(str.length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(rf);
				}
			}
		}); //end of search filter
		
		ytdButtons = new JButton[ChartType.size()];
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		for(int i = 0; i < ytdButtons.length; i++) {
			int finalI = i;
			ChartType type = ChartType.TOP_CUSTOMERS
			  .get(finalI);
			ytdButtons[i] = new JButton(type.toString());
			setButtonStyle(ytdButtons[i]);
			ytdButtons[i].setVisible(false);
			c.gridy = i + 7;
			east.add(ytdButtons[i], c);
			ytdButtons[i].addActionListener(e -> displayChart(
			 chartPanel, queryDate.getText(), type));
		}
		
		JLabel analyzeDate = new JLabel("Analyze YTD:");
		analyzeDate.setForeground(GREY_110x3);
		analyzeDate.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 6;
		east.add(analyzeDate, c);
		//creates textfield with 10 columns
		queryDate = new JFormattedTextField();
		queryDate.setDocument(new JTextFieldLimit(10));
		queryDate.setBackground(GREY_110x3);
		queryDate.setForeground(PURE_WHITE);
		queryDate.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 6;
		east.add(queryDate, c);
		
		queryDate.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {}
			
			private void format() {
				
				Runnable doFormat = new Runnable() {
					@Override
					public void run() {
						
						if(queryDate.getText().length() == 4 ||
						   queryDate.getText().length() == 7) {
							queryDate.setText(queryDate.getText() + "-");
						}
					}
				};
				SwingUtilities.invokeLater(doFormat);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				format();
				if(queryDate.getText().length() == 10) {
					for(JButton ytdButton: ytdButtons) {
						ytdButton.setVisible(true);
					}
				}
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				for(JButton ytdButton: ytdButtons) {
					ytdButton.setVisible(false);
				}
			}
		});
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
		dataWriter.println(String.join(",", crud.getColumnNames()));
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
	
	private void setButtonStyle(JButton button) {
		
		button.setBackground(GREY_110x3);
		button.setFont(FONT);
		button.setForeground(TABLE_FOREGROUND);
		button.setBorder(new LineBorder(DARK_GREY, 2));
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
	
	private void setUIManager() {
		
		UIManager.put("ScrollBar.thumb", new ColorUIResource(GREY_110x3));
		UIManager
		 .put("ScrollBar.thumbDarkShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(GREY_50x3));
		UIManager
		 .put("ScrollBar.thumbHighlight", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.track", new ColorUIResource(GREY_50x3));
	}
	
	public static class JTextFieldLimit extends PlainDocument {
		private final int limit;
		
		JTextFieldLimit(int limit) {
			
			super();
			this.limit = limit;
		}
		
		public void insertString(int offset, String str, AttributeSet attr)
		throws BadLocationException {
			
			if(str == null) { return; }
			
			if((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}
	}
}
