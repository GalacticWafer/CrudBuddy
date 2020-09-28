import org.junit.jupiter.api.Assertions;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Presentation1
{
    private static final String FILE_NAME = "inventory_team4.csv";
    public static void main(String[] args)throws FileNotFoundException
    {
        Database db = new Database();


        Table inventory = new Table(FILE_NAME, "table1");

        Table table1 = new Table("test1.csv", "table1");
        db.addTable(table1);

        Table table2 = new Table("test1.csv", "table2");
        db.addTable(table2);
        Assertions.assertNotNull(db.createEmptyTable("test2.csv", "table2"));

        Table table3 = new Table("test3.csv", "table3");
        db.addTable(table3);

        Table table4 = new Table("test4.csv", "table4");

        db.addTable(table4);

        Table table5 = new Table("test4.csv", "table5");


        // first test row
        String[] row1 = "HR30BB21WKZW,4933,402.08,703.64,SSLDGFIO".split(",");
        DataRow dataRow_1 = new DataRow(row1);

        // second test row
        String[] row2 = "53TUYLTAPU4U,627,36.21,53.81,MHPSLALJ".split(",");
        DataRow dataRow_2 = new DataRow(row2);

        System.out
                .println(
                        dataRow_1.toReadableString() + "\n" + dataRow_2.toReadableString() + "\n");

        // setQuantity() valid
        System.out.println("row_2.setQuantity(500):");
        dataRow_2.setquantity(500);
        System.out
                .println("row_2.getQuantity() --> " + dataRow_2.getQuantity() + "\n ");


        // setQuantity() invalid
        System.out.println("setting row_1 quantity to a negative value:" +
                "\nrow_1.setQuantity(-1)   --> " +
                dataRow_1.setquantity(-1) + "\n");

        // print updated readable string of row_1
        System.out.println(dataRow_1.toReadableString());



        // Test update "test4.csv"
        table5.update("table5.csv", table2);
    } // End main


} // End Presentation1 class






