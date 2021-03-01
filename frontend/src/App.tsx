import React, { Fragment, useState } from 'react';
import { BrowserRouter, Route, RouteComponentProps, withRouter } from "react-router-dom";
import logo from './logo.svg';
import './App.css';
import { createStyles, Theme, WithStyles, withStyles } from '@material-ui/core';

import Home from './pages/Home'
import Login from './pages/Login'
import { AuthProvider } from './auth-context';

let styles = (theme:Theme) => createStyles({
    main: {
      padding: theme.spacing(3),
      [theme.breakpoints.down('xs')]: {
        padding: theme.spacing(2),
      },
    },
  });

interface AppProps extends WithStyles<typeof styles> {
}
  
function App(props:AppProps) {

  let { classes } = props

  return (
      <Fragment>
          <BrowserRouter>
              <main className={classes.main}>
                  <Route path={`${process.env.PUBLIC_URL}/`} exact component={Home} />
                  <Route path={`${process.env.PUBLIC_URL}/login`} component={Login}></Route>
              </main>
          </BrowserRouter>
      </Fragment>

  );

}

export default withStyles(styles)(App)


