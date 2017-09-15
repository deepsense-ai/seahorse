/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var express = require('express'),
    path = require('path'),
    logger = require('morgan'),
    cookieParser = require('cookie-parser'),
    compression = require('compression'),
    timeout = require('connect-timeout'),

    auth = require('./auth/auth'),
    reverseProxy = require('./reverse-proxy'),
    config = require('./config/config');

var app = express();

app.use(logger('dev'));
app.use(timeout(config.get('timeout')));
app.use(cookieParser());
app.use(timeoutHandler);
app.use(compression());
app.disable('x-powered-by');

app.use(function (req, res, next) {
  if (req.headers['x-forwarded-proto'] !== 'https' && !req.headers.host.startsWith("localhost:")) {
    return res.redirect(301, 'https://' + req.headers.host + '/');
  }
  next();
});

auth.init(app);

app.get('/',
  auth.login,
  reverseProxy.forward
);

app.all('/**',
  auth.login,
  reverseProxy.forward
);

app.use(timeoutHandler);

function timeoutHandler(err, req, res, next){
  if (req.timedout) {
    console.error("Server timeout", err);
    res.status(503);
    res.send("Server timeout");
    return;
  }
  next();
}

module.exports = app;
