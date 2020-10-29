/*
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;


public class invgui extends CRUDBuddyTest {

    static JFrame frame;
    static JTable inv;
    static JScrollPane scrollPane;
    static Boolean closed = false;
    static String connection;
    static String tmptest;
    final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;

    public invgui() throws SQLException, ClassNotFoundException {
        //UIManager.put("ScrollBar.thumb", new Color(200, 110, 110));
        // UIManager.put("scrollbar", (new Color(255,57,57)));
        //UIManager.put("ScrollBar.thumb", new ColorUIResource(new Color(57,57,57)));

       */
/* UIManager.put("ScrollBar.thumbShadow", new Color(255, 110, 110));
        UIManager.put("ScrollBar.thumbHighlight", new Color(110, 255, 110));
        UIManager.put("ScrollBar.darkShadow", new Color(110, 110, 255)); *//*

        //UIManager.put("ScrollBar.minimumThumbSize", 40);
        //UIManager.put("ScrollBar.thumbHeight", 100);
        UIManager.put("ScrollBar.thumb", new ColorUIResource(new Color(110,110,110)));
        UIManager.put("ScrollBar.thumbDarkShadow", new ColorUIResource(new Color(50,50,50)));
        UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(new Color(50,50,50)));
        UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(new Color(50,50,50)));
        UIManager.put("ScrollBar.track", new ColorUIResource(new Color(50,50,50)));




        frame = new JFrame("Inventory");
        JPanel north = new JPanel();
        JPanel east  = new JPanel();
        JPanel west  = new JPanel();
        JPanel south = new JPanel();
        JPanel center = new JPanel();
        if (!closed){ connection = "Connected"; }
        else { connection = "No Connection"; }
        JLabel getStatus = new JLabel("Status: " + connection);
        if (closed == true){ getStatus.setForeground(new Color(217, 85, 80));}
        else { getStatus.setForeground(new Color(131, 224, 158)); }
        getStatus.setFont(new Font("Aharoni", Font.BOLD, 22));

        center.setBackground(new Color(50,50,50));
        north.setBackground(new Color(20,20,20));
        south.setBackground(new Color(50,50,50));
        east.setBackground(new Color(50,50,50));
        west.setBackground(new Color(50, 50, 50));
        frame.getContentPane().setBackground(new Color(50,50,50));

        east.setLayout(new GridBagLayout());
        center.setLayout(new GridBagLayout());
        GridBagConstraints middle = new GridBagConstraints();
        JLabel tname = new JLabel(tableName);
        tname.setForeground(new Color(125, 211, 224));
        tname.setFont(new Font("Aharoni", Font.BOLD, 22));
        middle.weightx = 0.5;
        middle.fill = GridBagConstraints.HORIZONTAL;
        middle.ipady = 70;
        middle.gridx = 0;
        middle.gridy = 0;
        middle.insets = new Insets(5,40,0,50);  //top padding
        center.addProduct(tname, middle);

        CRUDBuddy crud = new CRUDBuddy(userName, password, ipAddress, portNumber, databaseName);
        closed = crud.isClosed();
        ArrayList<String> temp = crud.readColumnNames(databaseName, tableName);
        String columnNames = (temp + "").substring(1, (temp + "").length() - 1);
        ResultSet rs = crud.query("Select " + columnNames + " from " + tableName);

        //JTable jt;
        int i = 0;
        ArrayList<Object[]> rows = new ArrayList<>();
        while(rs.next()) {
            i++;
            Object idx = rs.getObject("idx");
            Object product_id = rs.getObject("product_id");
            Object quantity = rs.getInt("quantity");
            Object wholesale_cost = rs.getDouble("wholesale_cost");
            Object sale_price = rs.getDouble("sale_price");
            String supplier_id = rs.getString("supplier_id");

            rows.addProduct(new Object[] {
                    idx, product_id, quantity, wholesale_cost, sale_price, supplier_id
            });
        }
        Iterator<Object[]> row_it = rows.productIterator();
        Object[][] data = new Object[i][6];
        for(int i1 = 0; i1 < data.length; i1++) {
            data[i1] = row_it.next();
        }
        TableFormatter tf = new TableFormatter(data, columnNames.split(","), crud);
        inv = tf.getTable();
        inv.setModel(new DefaultTableModel(data, columnNames.split(",")));
        tf.setData();

        middle.fill = GridBagConstraints.HORIZONTAL;
        middle.ipady = 380;      //make this component tall
        middle.weightx = 0.0;
        middle.gridwidth = 6;
        middle.gridx = 0;
        middle.gridy = 1;
        inv.setBackground(new Color(50,50,50));
        inv.setGridColor(new Color(110,110,110));
        inv.setForeground(new Color(125, 211, 224));
        inv.setFont(new Font("Aharoni", Font.BOLD, 15));
        center.addProduct(inv, middle);

        scrollPane = new JScrollPane(inv);
        scrollPane.setVisible(true);
        //scrollPane.getVerticalScrollBar().setBackground(new Color(50,50,50));
        scrollPane.setBorder(new LineBorder(new Color(110, 110, 110),2));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() );
        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI());
        center.addProduct(scrollPane, middle);

        GridBagConstraints c = new GridBagConstraints();
        JLabel pid = new JLabel("Product ID ");
        pid.setForeground(new Color(110, 110, 110));
        pid.setFont(new Font("Aharoni", Font.BOLD,22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        east.addProduct(pid, c);
        JTextField productid  = new JTextField(10); //creates textfield with 10 columns
        productid.setBackground(new Color(110, 110, 110));
        productid.setForeground(new Color(255, 255, 255));
        productid.setBorder(new LineBorder(new Color(20, 20, 20),2));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,20);  //top padding
        east.addProduct(productid, c);

        productid.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        tmptest = productid.getText();
                        System.out.print(tmptest);
                    }
                }
        );

        JLabel sid = new JLabel("Supplier ID ");
        sid.setForeground(new Color(110, 110, 110));
        sid.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        east.addProduct(sid, c);
        JTextField supplierid  = new JTextField(10); //creates textfield with 10 columns
        supplierid.setBackground(new Color(110, 110, 110));
        supplierid.setForeground(new Color(255, 255, 255));
        supplierid.setBorder(new LineBorder(new Color(20, 20, 20),2));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        east.addProduct(supplierid, c);

        JLabel qty = new JLabel("Quantity ");
        qty.setForeground(new Color(110, 110, 110));
        qty.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        east.addProduct(qty, c);
        JTextField quantity  = new JTextField(10); //creates textfield with 10 columns
        quantity.setBackground(new Color(110, 110, 110));
        quantity.setForeground(new Color(255, 255, 255));
        quantity.setBorder(new LineBorder(new Color(20, 20, 20),2));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        east.addProduct(quantity, c);

        JLabel wholesale = new JLabel("Wholesale ");
        wholesale.setForeground(new Color(110, 110, 110));
        wholesale.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        east.addProduct(wholesale, c);
        JLabel wsamount = new JLabel("temp");
        wsamount.setForeground(new Color(125, 211, 224));
        wsamount.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 4;
        east.addProduct(wsamount, c);

        JLabel sale = new JLabel("Set Sale Price ");
        sale.setForeground(new Color(110, 110, 110));
        sale.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        east.addProduct(sale, c);
        JTextField saleprice = new JTextField(10); //creates textfield with 10 columns
        saleprice.setBackground(new Color(110, 110, 110));
        saleprice.setForeground(new Color(255, 255, 255));
        saleprice.setBorder(new LineBorder(new Color(20, 20, 20),2));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        east.addProduct(saleprice, c);

        JLabel inventory = new JLabel("Order Inventory ");
        inventory.setForeground(new Color(110, 110, 110));
        inventory.setFont(new Font("Aharoni", Font.BOLD, 22));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        east.addProduct(inventory, c);
        JTextField orderinv = new JTextField(10); //creates textfield with 10 columns
        orderinv.setBackground(new Color(110, 110, 110));
        orderinv.setForeground(new Color(255, 255, 255));
        orderinv.setBorder(new LineBorder(new Color(20, 20, 20),2));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 6;
        east.addProduct(orderinv, c);

        frame.setBounds(200, 400, 1300, 620);
        north.addProduct(getStatus);
        frame.addProduct(north, BorderLayout.NORTH);
        frame.addProduct(east, BorderLayout.EAST);
        frame.addProduct(west, BorderLayout.WEST);
        frame.addProduct(south, BorderLayout.SOUTH);
        frame.addProduct(center, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }



    public void paintComponent(Graphics g) {


    }


}*/
