import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

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
	private static JTable table;
	private static String tmptest;
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
		tableSelections.setForeground(TABLE_FOREGROUND);
		tableSelections.setFont(FONT);
		MIDDLE_CONSTRAINTS.weightx = 0.5;
		MIDDLE_CONSTRAINTS.ipady = 70;
		MIDDLE_CONSTRAINTS.gridx = 0;
		MIDDLE_CONSTRAINTS.gridy = 0;
		MIDDLE_CONSTRAINTS.insets = new Insets(13, 0, - 1, 0);  //top padding
		CENTER_PANEL.add(tableSelections, MIDDLE_CONSTRAINTS);
		crud.setWorkingTable("inventory");
		System.out.println(crud.getWorkingTable());
		createTable();
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
		NORTH_PANEL.setBackground(DARK_GREY);
		SOUTH_PANEL.setBackground(GREY_50x3);
		EAST_PANEL.setBackground(GREY_50x3);
		WEST_PANEL.setBackground(GREY_50x3);
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
		table.setGridColor(GREY_110x3);
		table.setForeground(TABLE_FOREGROUND);
		table.setFont(FONT);
		center.add(table, middle);
		
		scrollPane = new JScrollPane(table);
		scrollPane.setVisible(true);
		scrollPane.setBorder(new LineBorder(GREY_110x3, 2));
		scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI());
		scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI());
		center.add(scrollPane, middle);
		
		GridBagConstraints c = new GridBagConstraints();
		JLabel pid = new JLabel("Product ID ");
		pid.setForeground(GREY_110x3);
		pid.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 15, 0, 10);
		east.add(pid, c);
		JTextField productid = new JTextField(10); //creates textfield with 10 columns
		productid.setBackground(GREY_110x3);
		productid.setForeground(PURE_WHITE);
		productid.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		east.add(productid, c);
		
		productid.addActionListener(
		 new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 tmptest = productid.getText();
				 System.out.print(tmptest);
			 }
		 }
		);
		
		JLabel sid = new JLabel("Supplier ID ");
		sid.setForeground(GREY_110x3);
		sid.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		east.add(sid, c);
		JTextField supplierid = new JTextField(10); //creates textfield with 10 columns
		supplierid.setBackground(GREY_110x3);
		supplierid.setForeground(PURE_WHITE);
		supplierid.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		east.add(supplierid, c);
		
		JLabel qty = new JLabel("Quantity ");
		qty.setForeground(GREY_110x3);
		qty.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		east.add(qty, c);
		JTextField quantity = new JTextField(10); //creates textfield with 10 columns
		quantity.setBackground(GREY_110x3);
		quantity.setForeground(PURE_WHITE);
		quantity.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		east.add(quantity, c);
		
		JLabel wholesale = new JLabel("Wholesale ");
		wholesale.setForeground(GREY_110x3);
		wholesale.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 4;
		east.add(wholesale, c);
		JLabel wsamount = new JLabel("temp");
		wsamount.setForeground(TABLE_FOREGROUND);
		wsamount.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 4;
		east.add(wsamount, c);
		
		JLabel sale = new JLabel("Set Sale Price ");
		sale.setForeground(GREY_110x3);
		sale.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 5;
		east.add(sale, c);
		JTextField saleprice = new JTextField(10); //creates textfield with 10 columns
		saleprice.setBackground(GREY_110x3);
		saleprice.setForeground(PURE_WHITE);
		saleprice.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 5;
		east.add(saleprice, c);
		
		JLabel inventory = new JLabel("Order Inventory ");
		inventory.setForeground(GREY_110x3);
		inventory.setFont(FONT);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 6;
		east.add(inventory, c);
		JTextField orderinv = new JTextField(10); //creates textfield with 10 columns
		orderinv.setBackground(GREY_110x3);
		orderinv.setForeground(PURE_WHITE);
		orderinv.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 6;
		east.add(orderinv, c);
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
}
