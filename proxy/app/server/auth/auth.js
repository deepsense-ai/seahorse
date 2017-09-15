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

var session = require('express-session');
var crypto = require('crypto');
var url = require('url');
var oauth2 = require('./oauth2');
var config = require('../config/config');
var passport = oauth2.passport;
var strategy = oauth2.strategy;

module.exports = {
  init: init,
  login: login,
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
    var oauthPage = url.format({protocol: req.protocol, host: req.get("host"), pathname: "oauth"})
    res.redirect(config.oauth.logoutUri+"?redirect=" + oauthPage);
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
