import logo from './logo.svg';
import Main from './Main.js';
import './App.css';
import {BrowserRouter} from 'react-router-dom';

function App() {
  return (

    <div className="App">
      <h1>"MyCrowMart</h1>
          <ul>
            <li><a href="default.asp">Home</a></li>
            <li><a href="news.asp">News</a></li>
            <li><a href="contact.asp">Contact</a></li>
            <li><a href="about.asp">About</a></li>
          </ul>
          <Main />
    </div>
  );
}

export default App;
