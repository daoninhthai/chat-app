import React from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import ChatRoom from './components/Chat/ChatRoom';

function App() {
  return (
    <div className="App">
      <Switch>
        <Route exact path="/login" component={Login} />
        <Route exact path="/register" component={Register} />
        <Route exact path="/chat" component={ChatRoom} />
        <Route path="/chat/:roomId" component={ChatRoom} />
        <Redirect from="/" to="/login" />
      </Switch>
    </div>
  );
}

export default App;
