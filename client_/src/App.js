import React, {Component, useEffect, useState} from 'react';
import './App.css';
import Axios from 'axios';
import Main from './components/main';
import {Link, BrowserRouter, Route} from 'react-router-dom';
import About from './components/About';
import Home from './components/home';
import Shop from './components/Shop';
import
{
    Collapse,
    Navbar,
    NavbarToggler,
    Nav,
    NavItem,
    Container
} from 'reactstrap';
import {userInfo} from 'os';


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
//     then((response) =>{m
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

    toggle = () => {
        this.setState(
            {
                isOpen: !this.state.isOpen
            });
    }


    render() {
        return (
            <div className="App">
              <BrowserRouter>
                <Route path ='/' component={Home}/>
                <Route path ='/home' component={Home}/>
                <Route path ='/about' component={About}/>
                <Route path ='/shop' component={Shop}/>
                <Navbar id="navbar" font="" height="px;" expand="sm" className="mb-0">
                    <Container>
                        <Link to="/">
                            <span id="logoimage"></span>
                        </Link>
                        <NavbarToggler onClick={this.toggle}></NavbarToggler>
                        <Collapse isOpen={this.state.isOpen} navbar>
                            <Nav className="ml-auto" height="0px">
                                <NavItem>
                                    <Link to="home">
                                        Home
                                    </Link>
                                </NavItem>
                                <NavItem>
                                    <Link height="50px;" to="shop">
                                        Shop
                                    </Link>
                                </NavItem>
                                <NavItem>
                                    <Link to="about">
                                        About
                                    </Link>
                                </NavItem>
                            </Nav>
                        </Collapse>
                    </Container>
                </Navbar>
              </BrowserRouter>
            </div>
        );
    }
}

export default App;


