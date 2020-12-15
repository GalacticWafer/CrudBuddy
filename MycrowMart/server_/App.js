const express = require('express');
const mysql = require('mysql');
const bodyParser = require('body-parser')

/*var cors = require('cors')*/
const db = mysql.createConnection({
    host: "45.79.55.190",
    user: "GalacticWafer",
    password: "CYN2!5#vVW)*f&>PSvVW)*fK=K=X#x,NX}$H",
    database: "GalacticWafer"
});

db.connect((err) => {
    if (err) {
        throw err;
        console.log(err)
    }
    console.log("SQL Connected...");
})

//   db.query('SELECT * FROM inventory', (err,rows) => {
//     if(err) throw err;
//     console.log('Data received from Db:');
//     console.log(rows);
// });

// product_id : DOTA123123
// wholesale_cost : 1.99
// sale_price : 2.54
// supplier_id  : Shilpa123
// quantity : 123

const app = express();
app.use(express.json())
// app.use(bodyParser.urlencoded({extended: true}))
// app.use(cors())

app.get('/', (req, res) => {
    //const sqlInsert = "INSERT INTO inventory (product_id, wholesale_cost, sale_price, supplier_id, quantity) VALUES ('DOTA123123', '1.99', '2.54', 'Shilpa12', '123');"
    // const page = req.query.page
    // const limit = req.query.limit

    // const startIndex  =(page -1) * limit
    // const endIndex  = page * limit

    let sql = "SELECT * FROM inventory";

    let query = db.query(sql, (err, results) => {
        if (err) throw err;
        // const resultsliced = results.slice(startIndex,endIndex)
        res.json(results)
        console.log(results)
    });
});


app.post('/insertorder', (req, res) => {

    var today = new Date();


    const dateformat = today.toISOString().slice(0, 10) + " " + today.toTimeString().slice(0, 0);
    const appendzero = "00:00:00"

    var sqlformat = (dateformat.concat("", appendzero)).toString();

    const date_ordered = sqlformat
    const cust_email = req.body.cust_email
    const cust_location = req.body.cust_location
    const product_id = req.body.product_id
    const product_quantity = req.body.product_quantity

    const sqlInsert =
        "INSERT INTO unstatused_sales(" +
        "date_ordered," +
        "cust_email," +
        "cust_location," +
        "product_id," +
        "product_quantity" +
        ")VALUES(?,?,?,?,?)";

    db.query(sqlInsert, [
        date_ordered,
        cust_email,
        cust_location,
        product_id,
        product_quantity], (err, results) => {
        console.log(sqlInsert)
        console.log(err)
        console.log(results)
    });
});

app.listen('3306', () => {
    console.log("Server started on port 3306")
});
  
