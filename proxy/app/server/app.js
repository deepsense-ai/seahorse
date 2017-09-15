/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const express = require('express');
const path = require('path');
const logger = require('morgan');
const cookieParser = require('cookie-parser');
const compression = require('compression');
const timeout = require('connect-timeout');
const reverseProxy = require('./reverse-proxy');
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

app.all("/authorization/**",
  reverseProxy.forward
);

let auth;
if (config.get('ENABLE_AUTHORIZATION') === "true") {
  auth = require('./auth/auth');
} else {
  auth = require('./auth/stub');
}
auth.init(app);
app.use(auth.login);
app.use(userCookieMiddleware);

app.get('/', reverseProxy.forward);
app.all('/**', reverseProxy.forward);

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
