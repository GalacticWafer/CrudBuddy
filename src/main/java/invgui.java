import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

public class invgui {
	static JFrame frame;
	static JTable inv;
	static JScrollPane scrollPane;
	static Boolean closed = false;
	static String connection;
	static String tmptest;
	private DefaultListCellRenderer listRenderer;
	final static boolean shouldFill = true;
	final static boolean shouldWeightX = true;
	private Crud crud;
	
	private final Color openStatusForeground = new Color(131, 224, 158);
	private final Color closedStatusForeground = new Color(217, 85, 80);
	
	public invgui(Crud crud) throws SQLException, ClassNotFoundException {
		
		
		UIManager.put("ScrollBar.thumb", new ColorUIResource(GREY_110x3));
		UIManager.put("ScrollBar.thumbDarkShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(GREY_50x3));
		UIManager.put("ScrollBar.track", new ColorUIResource(GREY_50x3));
		this.crud = crud;
		
		String[] array = crud.getTableNames();
		System.out.println(Arrays.toString(array));
		
		frame = new JFrame("Inventory");
		JPanel north = new JPanel();
		JPanel east = new JPanel();
		JPanel west = new JPanel();
		JPanel south = new JPanel();
		JPanel center = new JPanel();
		if(! closed) { connection = "Connected"; } else { connection = "No Connection"; }
		JLabel status = new JLabel("Status: " + connection);
		if(closed == true) {
			status.setForeground(closedStatusForeground);
		} else { status.setForeground(openStatusForeground); }
		status.setFont(FONT);
		
		center.setBackground(centerBackground);
		north.setBackground(GREY_50x3);
		south.setBackground(GREY_50x3);
		east.setBackground(GREY_50x3);
		west.setBackground(GREY_50x3);
		frame.getContentPane().setBackground(GREY_50x3);
		
		east.setLayout(new GridBagLayout());
		center.setLayout(new GridBagLayout());
		GridBagConstraints middle = new GridBagConstraints();
		JComboBox tables = new JComboBox(crud.getTableNames());
		listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned 
		// items
		tables.setBackground(GREY_110x3);
		tables.setForeground(TABLE_FOREGROUND);
		tables.setFont(FONT);
		middle.weightx = 0.5;
		middle.ipady = 70;
		middle.gridx = 0;
		middle.gridy = 0;
		middle.insets = new Insets(13, 0, - 1, 0);  //top padding
		center.add(tables, middle);
       /* JLabel tname = new JLabel(crud.getWorkingTable());
        tname.setForeground(TABLE_FOREGROUND);
        tname.setFont(FONT);
        middle.weightx = 0.5;
        middle.fill = GridBagConstraints.HORIZONTAL;
        middle.ipady = 70;
        middle.gridx = 0;
        middle.gridy = 0;
        middle.insets = new Insets(5,40,0,50);  //top padding
        center.add(tname, middle); */
		
		crud.setWorkingTable("inventory");
		System.out.println(crud.getWorkingTable());
		closed = crud.isClosed();
		String[] columnNames = crud.getColumnNames();
		ResultSet rs = crud.getAllRecords();
		
		//JTable jt;
		int i = 0;
		int count = 0;
		Object[][] data = new Object[crud.size()][columnNames.length + 1];
		while(rs.next()) {
			i++;
			Object idx = rs.getObject("idx");
			Object product_id = rs.getObject("product_id");
			Object quantity = rs.getInt("quantity");
			Object wholesale_cost = rs.getDouble("wholesale_cost");
			Object sale_price = rs.getDouble("sale_price");
			String supplier_id = rs.getString("supplier_id");
			
			data[count++] = new Object[] {
			 idx, product_id, quantity, wholesale_cost, sale_price, supplier_id
			};
			count++;
		}

		TableFormatter tf = new TableFormatter(data, columnNames, crud);
		inv = tf.getTable();
		inv.setModel(new DefaultTableModel(data, columnNames));
		tf.setData();
		
		middle.fill = GridBagConstraints.HORIZONTAL;
		middle.ipady = 380;      //make this component tall
		middle.weightx = 0.0;
		middle.gridwidth = 6;
		middle.gridx = 0;
		middle.gridy = 1;
		inv.setBackground(GREY_50x3);
		inv.setGridColor(GREY_110x3);
		inv.setForeground(TABLE_FOREGROUND);
		inv.setFont(FONT);
		center.add(inv, middle);
		
		scrollPane = new JScrollPane(inv);
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
		east.add(pid, c);
		JTextField productid = new JTextField(10); //creates textfield with 10 columns
		productid.setBackground(GREY_110x3);
		productid.setForeground(PURE_WHITE);
		productid.setBorder(new LineBorder(DARK_GREY, 2));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 0, 20);  //top padding
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
	
	public void paintComponent(Graphics g) {
		
	}
	
	private final Color centerBackground = GREY_50x3;
	private final Color  DARK_GREY = new Color(20, 20, 20);
	private final Color TABLE_FOREGROUND = new Color(125, 211, 224);
	public static final Font FONT = new Font("Aharoni", Font.BOLD, 15);
	public static final Color GREY_110x3 = new Color(110, 110, 110);
	public static final Color GREY_50x3 = new Color(50, 50, 50);
	public static final Color PURE_WHITE = new Color(255, 255, 255);
}
