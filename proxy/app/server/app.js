/**
 * Copyright (c) 2016, CodiLime Inc.
 */
const express = require('express');
const path = require('path');
const logger = require('morgan');
const cookieParser = require('cookie-parser');
const compression = require('compression');
const timeout = require('connect-timeout');
const reverseProxy = require('./reverse-proxy');
const authorizationQuota = require('./authorization-quota/authorization-quota');
const config = require('./config/config');

const app = express();

app.use(logger('dev'));
app.use(timeout(config.get('timeout')));
app.use(cookieParser());
app.use(timeoutMiddleware);
app.use(internalErrorMiddleware);
app.use(compression());
app.disable('x-powered-by');

if (config.get('FORCE_HTTPS') === "true") {
  app.use(httpsRedirectHandler);
}

app.use(express.static('app/server/html'));
app.all("/wait.html");
app.all("/quota.html");
app.all("/trial-expired.html");

let auth = undefined;
if (config.get('ENABLE_AUTHORIZATION') === "true") {
  auth = require('./auth/auth');
} else {
  auth = require('./auth/stub');
}
auth.init(app);

app.use(userCookieMiddleware);

app.all("/authorization/create_account*",
  authorizationQuota.forward,
  reverseProxy.forward
);

app.all("/authorization/**",
  reverseProxy.forward
);

app.get('/',
  auth.login,
  reverseProxy.forward
);

app.all('/**',
  auth.login,
  reverseProxy.forward
);

if (config.get('ENABLE_AUTHORIZATION') === "true") { // Depends on userCookieMiddleware
  const trialTimeLimitMiddleware = require('./trial/time-limit').middleware;
  app.use(trialTimeLimitMiddleware)
}

function httpsRedirectHandler(req, res, next) {
  if (req.headers['x-forwarded-proto'] !== 'https' && !req.headers.host.startsWith("localhost:")) {
    return res.redirect(301, 'https://' + req.headers.host + req.url);
  }
  next();
}

function userCookieMiddleware(req, res, next) {
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

function timeoutMiddleware(req, res, next) {
  if (req.timedout) {
    res.status(503);
    res.send("Server timeout");
    return;
  }
  next();
}

function internalErrorMiddleware(err, req, res, next) {
  console.error(err.stack);
  res.status(500);
  res.send("Internal server error");
}

module.exports = app;
