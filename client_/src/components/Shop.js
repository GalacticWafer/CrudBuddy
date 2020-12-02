import React, {Component} from 'react';
import '../App.css';
import Axios from 'axios';



class CRUD extends Component {


  constructor(props) {
    super(props)


    this.state = {
      products: [],
      display: false,

      cartitems: [],




      cartwidth: "0px",
      showcart: "Show Cart",
      showCustomerinformationBox: "hidden",
      quantity: 0,
      inputValue: "",

      ////order
      order_id: "",
      cust_email: "",
      cust_location: "",
      product_id: "XROLELA37H",
      product_quantity: 1,
      date_accepted: "",
      order_status: "UNPROCESSED"
      ////order

    }


  }


  toggleHandler = () => {
    if (this.state.cartwidth == "0px") {
      this.setState({
        cartwidth: "500px",
        showcart: "HideCart"
      })
    } else if (this.state.cartwidth == "500px")
      this.setState({
        cartwidth: "0px",
        showcart: "Show Cart"
      })

  }


  SubmitOrder = (event) =>

  {


    event.preventDefault();




    var length = 10 // random id generator
    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    var randomOrderID = '';
    for (var i = length; i > 0; --i) randomOrderID += chars[Math.floor(Math.random() * chars.length)];




    const email = this.state.cust_email;
    const location = this.state.cust_location;
    alert(email + "" + location);


    const location1 = parseInt(location);
    const a = Number.isInteger(location1) // location verification

    const testemail = (/^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/.test(email))


    if(testemail==false)
    {
      alert("wrong email")
    }
    else if(location.length == 5 && a == true && (location + "").length == 5) {



      this.setState({
        order_id: randomOrderID,
        product_id: this.state.cartitems[0].product_id,
        product_quantity: this.state.quantity,
        date_accepted: null,
        order_status: "UNPROCESSED"

      })


      const order = this.state


      Axios.post("http://localhost:3306/insertorder",
          {

            cust_email: order.cust_email,
            cust_location: order.cust_location,
            product_id: order.product_id,
            product_quantity: order.quantity,

          }).then(() => {
        console.log("successfulyy submitted")
        console.log(this.state.cartitems);

      });



    }
    else {
      alert("invalid input")
    }
  }




  openconfirmationbox = () => {
    if (this.state.cartitems.length <= 0) {
      alert("plese add items in cart to checkout")
    } else if (this.state.cartitems.length > 0) {

      this.setState({
        showCustomerinformationBox: "visible"

      })
    }}







//Empty Cart
  Emptycart = () =>
  {
    this.setState({
      cartitems: [],
      showCustomerinformationBox: "hidden"
    })
  }



  //LifeCycleHook
  componentDidMount = () => {
    this.getInverntorydata();
  }


  getInverntorydata = () => {
    Axios.get('http://localhost:3306/?page=1&limit=500').then((response) => {
      const data = response.data;
      console.log('Data has been received');
      this.setState({
        products: data
      })
    }).catch(() => {
      alert("error")

    });
  }




  hideCheckout = () => {
    this.setState({
      showCustomerinformationBox: "hidden"
    })

  }


  handleInputChange = (event) => {


    this.setState({
      quantity: event.target.value
    })

  }


  handleemailinputChange = (event) => {


    this.setState({
      cust_email: event.target.value
    })

  }

  handlelocationInputChange = (event) => {


    this.setState({
      cust_location: event.target.value
    })

  }

  render() {

//adding products to cart


    const addtocart = (product) => {


      // {idx: 1, product_id: "ZRDATK9CSM23", quantity: 3628, wholesale_cost: 420.99, sale_price: 596.54, …}
      // Shop.js:132
      if (this.state.quantity <= 0) {
        alert("quantity can't be null or zero")
      } else if (this.state.quantity > 0) {

        const cartproduct = {
          idx: product.product_idx,
          product_id: product.product_id,
          quantity: this.state.quantity,
          wholesale_cost: product.wholesale_cost
        }
        this.setState({cartitems: ([...this.state.cartitems, cartproduct])})


      }
    };


    const cartproduct = this.state.cartitems.map((product, idx) =>

        <div id="cartproductcardwrapper">
          <div id="cartproductcard" class="col-lg-md-12">
            <div class="card-header" key={product.idx}></div>
            <div class="card-body">
              <p class="card-title">Product ID:
                <span id="bold">         {product.product_id} </span>
              </p>
              <p class="card-title">Quantity Purchased:
                <span id="bold"> {product.quantity}</span>
              </p>
              <p class="card-title">Cost:
                <span id="bold">$ {product.wholesale_cost} </span>
              </p>

              <p class="card-text"></p>
            </div>
          </div>
        </div>
    );


    const products = this.state.products.map((product, idx) =>


        <div id="productcardwrapper" class="col-lg-md-6">
          <div id="productcard" class="col-lg-md-3">
            <div class="card-header" key={product.idx}></div>
            <div class="card-body">
              <p class="card-title">Product ID:
                <span id="bold">{product.product_id} </span>
              </p>
              <p class="card-title">Quantity Remaining:
                <span id="bold"> {product.quantity}</span>
              </p>
              <p class="card-title">Cost:
                <span id="bold">${product.wholesale_cost} </span>
              </p>
              <form>
                <input type="number" min="0" placeholder="Select Quantity"
                       onChange={this.handleInputChange}></input>

                <button id="buy" type="button" onClick={() => addtocart(product)}>Add to Cart
                </button>
              </form>
              <p class="card-text"></p>
            </div>
          </div>
        </div>
    );


    return (


        <div>

          //Customer information box

          <div id="customerinfobox" class="text-center"
               style={{visibility: this.state.showCustomerinformationBox}}>
            <form onSubmit={this.SubmitOrder}>
              <br></br>

              <h1>Check Out</h1>
              <br></br>

              <p>Please provide the below information to complete the checkout</p>
              <br></br>


              <h3>Email</h3>
              <input type="email" ref="email" onChange={this.handleemailinputChange}/>
              <br></br>
              <br></br>
              <h3>Location(5 digits Zip code)</h3>
              <input type="number" maxLength="5" ref="location" onChange={this.handlelocationInputChange}/>
              <br></br>
              <br></br>
              <button type="submit">
                submit
              </button>
            </form>
            <button id="x" onClick={this.hideCheckout}>
              x
            </button>
            <br></br>
            <br></br>

          </div>

          //Customer information box


          <div class="container-fluid" id="contactdiv">
            <div id="shoppingcontainer" class="container">

              <div id="cart" class="row text-left"
                   style={{width: this.state.cartwidth}}>
                <div id="cartheading" class="row text-center">
                  <h1>CART</h1>
                </div>
                {cartproduct}
                <button id="Checkout" onClick={this.openconfirmationbox}>
                  Check out
                </button>
                <button id="Emptycart" onClick={this.Emptycart}>
                  Empty Cart
                </button>
              </div>
              <div id="shophere" class="row text-center">
                SHOP HERE
              </div>
              <button id="minimize" onClick={this.toggleHandler}>
                {this.state.showcart}
                ({this.state.cartitems.length})
              </button>
              <div class="row justify-content-md-left">
                {products}
              </div>
              <div class="row justify-content-md-left">

              </div>
            </div>
          </div>
        </div>

    );
  }
}


export default CRUD;
