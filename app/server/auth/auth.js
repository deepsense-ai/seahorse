/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var session = require('express-session');
var crypto = require('crypto');
var oauth2 = require('./oauth2');
var config = require('../config/config');
var passport = oauth2.passport;
var strategy = oauth2.strategy;

module.exports = {
  init: init,
  login: login,
  checkLoggedIn: checkLoggedIn
};

function init(app) {
  app.use(session({
    name: 'JSESSIONID',
    secret: crypto.randomBytes(16).toString('hex'),
    resave: false,
    saveUninitialized: false
  }));
  app.use(passport.initialize());
  app.use(passport.session());

  app.get('/oauth',
    passport.authenticate('cloudfoundry'));

  app.get('/oauth/callback',
    passport.authenticate('cloudfoundry'),
    function (req, res) {
      console.info('Authenticated, redirecting');
      res.redirect('/');
    });

  app.get('/logout', function (req, res) {
    req.session.destroy();
    req.logout();
    strategy.reset();
    res.redirect(sso.logoutUri);
  });
}

function login(req, res, next) {
  if(!req.user) {
    if(req.session) {
      req.session.destroy();
    }
    req.logout();
    strategy.reset();
    res.redirect('/oauth');
  } else {
    next();
  }
}

function checkLoggedIn(req, res, next) {
  if (req.user) {
    next();
  } else {
    res.status(401).send('session_expired');
  }
}
