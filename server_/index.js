const express = require('express');
const mysql = require('mysql');
var cors = require('cors')


const db = mysql.createConnection({

    host: "45.79.55.190",
    user: "GalacticWafer",
    password: "7!qaJ|B[t$",
    database: "cs3250_project",
  
  });

  //Connect
  

  db.connect((err)=> {
if(err)
{
    throw err;
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

const app =  express();

app.use(cors())

app.get('/',(req,res) =>
{
  //const sqlInsert = "INSERT INTO inventory (product_id, wholesale_cost, sale_price, supplier_id, quantity) VALUES ('DOTA123123', '1.99', '2.54', 'Shilpa12', '123');"
 let sql = "SELECT * FROM inventory";
  let query = db.query(sql, (err, results) =>{
    if(err) throw err;
    res.json(results)

  });
});

app.listen('3306', () =>{
    console.log("Server started on port 3306")
});


