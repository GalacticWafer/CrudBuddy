import React, { Component} from 'react';
import '../App.css';
import Axios from 'axios';




class CRUD extends Component {

constructor(props) {
  super(props)

  this.state = {
     products : []
  }
}


    componentDidMount = () => {
      this.getInverntorydata();
   
     
      
    }
    
  
  getInverntorydata = ()=> {
    Axios.get('http://localhost:3306/').
    then((response) =>{
      const data = response.data;
      this.setState({ posts: data});
      console.log('Data has been received');
      console.log(data)
      this.setState({
        products : data
      })
    }).catch(() => {
      alert("error")
      
    });
  }

  // quantity: 3628
  // sale_price: 596.54
  // supplier_id: "ZJPGZTLM"
  // wholesale_cost: 420.99

  render() {
    const products = this.state.products.map((product) =>

    <div id="productcardwrapper">
  <div id= "productcard"class="col-lg-md-2" >
  <div class="card-header" key={product.idx} ></div>
  <div class="card-body">
    <p class="card-title"><span id ="bold">Product ID:</span>  {product.product_id}</p>
    <p class="card-title"><span id ="bold">Quantity:</span> {product.quantity}</p>
    <p class="card-title"><span id ="bold">Cost:</span>${product.wholesale_cost}</p>
    <button id="buy"type="button">Buy</button>
    <p class="card-text"></p>
  </div>
</div>
</div>
  
    
);

    return (
     
  

<div>
<div class="container-fluid" id="contactdiv">
<div id ="shoppingcontainer" class="container">
<div  id="shophere" class="row text-center" >
SHOP HERE
</div>
<div class="row justify-content-md-left">
{products}
</div>
</div>
</div>
</div>  

    );
  }
}

export default CRUD;
