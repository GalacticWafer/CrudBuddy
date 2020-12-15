import React, { Component } from 'react';
import { HashRouter as Router, Route, Switch } from 'react-router-dom';
import Home from './Home';

const Main = () =>
(
<main>
    <Switch>
        <Route exact path ='/' component={Home}/>
        <Route exact path ='/home' component={Home}/>
    </Switch>
</main>
)
export default Main;
{/* <Route exact path ='/about' component={About}/>
<Route exact path ='/shop' component={Shop}/> */}