/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var express = require('express'),
    path = require('path'),
    logger = require('morgan'),
    cookieParser = require('cookie-parser'),
    compression = require('compression'),
    timeout = require('connect-timeout'),
    reverseProxy = require('./reverse-proxy'),
    config = require('./config/config');

var app = express();

var auth = undefined;
if (config.get('DISABLE_OAUTH') === "true") {
  auth = require('./auth/stub');
} else {
  auth = require('./auth/auth');
}

app.use(logger('dev'));
app.use(timeout(config.get('timeout')));
app.use(cookieParser());
app.use(timeoutHandler);
app.use(compression());
app.disable('x-powered-by');

if (config.get('FORCE_HTTPS') === "true") {
  app.use(httpsRedirectHandler);
}

auth.init(app);

app.use(userCookieHandler);

app.get('/',
  auth.login,
  reverseProxy.forward
);

app.all('/**',
  auth.login,
  reverseProxy.forward
);

app.use(timeoutHandler);
app.use(internalErrorHandler);

function httpsRedirectHandler(req, res, next) {
  if (req.headers['x-forwarded-proto'] !== 'https' && !req.headers.host.startsWith("localhost:")) {
    return res.redirect(301, 'https://' + req.headers.host + req.url);
  }
  next();
}

function userCookieHandler(req, res, next) {
  if (req.user) {
    res.cookie('seahorse_user', JSON.stringify({
      'id': req.user.user_id,
      'name': req.user.user_name
    }));
  } else {
    res.clearCookie('seahorse_user');
  }
  next();
}

function timeoutHandler(req, res, next){
  if (req.timedout) {
    res.status(503);
    res.send("Server timeout");
    return;
  }
  next();
}

function internalErrorHandler(err, req, res, next) {
  console.error(err.stack);
  res.status(500);
  res.send("Internal server error");
}

module.exports = app;
