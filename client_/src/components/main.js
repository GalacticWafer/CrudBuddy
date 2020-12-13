import React, { Component } from 'react';
import { HashRouter as Router, Route, Switch } from 'react-router-dom';
import About from './About';
import Home from './home';
import Shop from './Shop';

const Main = () =>
(
<main>
    <Switch>
        <Route path ='/' component={Home}/>
        <Route path ='/home' component={Home}/>
        <Route path ='/about' component={About}/>
        <Route path ='/shop' component={Shop}/>
    </Switch>
</main>


)
export default Main;