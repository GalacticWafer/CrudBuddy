import React, { Component } from 'react';
import '../App.css';
import logo from '../projectpictures/github.png';


class Home extends Component {
  render() {
    return (
     
      <div className="App">

      
<div class="container-fluid" id="maincontainer">


<div class ="row" id="rowheading">
<div class="col">
<p class="text-center" id="helloheading2">We are <span id="microsoft">MyCrowSawft</span> </p>
<p class="text-cenert" id="firstpara"> <br></br>  </p>
</div>
</div>


<div class ="row" id="rowheading2">
<div class="col">
<h1 id="logorow" class="text-center"> Join us on <a href="https://github.com/MyCrowSawft/SemesterProject"><span id="github" class="img-fluid" > <img src={logo} class="img-fluid" alt="GITHUB"></img></span></a> </h1>

</div>
</div>




<div class="container" id="footer">  </div>
</div>
      </div>
    );
  }
}

export default Home;
