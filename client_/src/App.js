import React, { Component,useEffect,useState } from 'react';
import './App.css';
import Axios from 'axios';
import Main from './components/main';



import
{
Collapse,
Navbar,
NavbarToggler,
NavbarBrand,
Nav,
NavItem,
NavLink,
Container
} from 'reactstrap';
import { userInfo } from 'os';




class App extends Component {



// state = {
//    title: '',
//    body: '',
//    posts: []
// };

// componentDidMount = () => {
//   this.getInverntorydata();
// }


//   getInverntorydata = ()=> {
//     Axios.get("http://localhost:3306/").
//     then((response) =>{
//       const data = response.data;
//       this.setState({ posts: data});
//       console.log(data[0]);
//     }).catch(() => {
//       alert("error")
//     });
//   }


  state = {
    isOpen: false 
}

toggle = () =>
{
    this.setState(
        {
            isOpen: !this.state.isOpen
        });
}



  render() {
    return (
     
      <div className="App">
 <Navbar id="navbar" font="" height="px;"  expand="sm" className="mb-0">
                <Container>
                    <NavbarBrand href="/">
                    
       <span id="logoimage"></span>

                    </NavbarBrand>
                    <NavbarToggler onClick = {this.toggle} ></NavbarToggler>
                    <Collapse isOpen={this.state.isOpen} navbar>
                    <Nav className="ml-auto" height="0px">
                    <NavItem>
                        <NavLink href="home">
                           Home
                        </NavLink>
                    </NavItem>
                    <NavItem>
                        <NavLink height="50px;" href="shop">
                        Shop
                        </NavLink>
                        
                    </NavItem>
                    <NavItem>
                        <NavLink href="about">
                       About 
                        </NavLink>
                        
                    </NavItem>
                    </Nav>
                    </Collapse>
                </Container>
            </Navbar>
      

<Main/>
      </div>
    );
  }
}

export default App;


