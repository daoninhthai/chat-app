import React from 'react';
import { Switch, Route, Redirect } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import ChatRoom from './components/Chat/ChatRoom';

// PrivateRoute - chuyen huong ve /login neu chua dang nhap
const PrivateRoute = ({ component: Component, ...rest }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="loading">Dang tai...</div>;
  }

  return (
    <Route
      {...rest}
      render={(props) =>
        isAuthenticated() ? (
          <Component {...props} />
        ) : (
          <Redirect
            to={{
              pathname: '/login',
              state: { from: props.location },
            }}
          />
        )
      }
    />
  );
};

function AppRoutes() {
  return (
    <Switch>
      <Route exact path="/login" component={Login} />
      <Route exact path="/register" component={Register} />
      <PrivateRoute exact path="/chat" component={ChatRoom} />
      <PrivateRoute path="/chat/:roomId" component={ChatRoom} />
      <Redirect from="/" to="/chat" />
    </Switch>
  );
}

function App() {
  return (
    <AuthProvider>
      <div className="App">
        <AppRoutes />
      </div>
    </AuthProvider>
  );
}

export default App;
