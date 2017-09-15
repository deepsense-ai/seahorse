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

var passport = require('passport');
var OAUTH2Strategy = require('passport-cloudfoundry').Strategy;
var config = require('../config/config');

var strategy = new OAUTH2Strategy({
  authorizationURL: config.oauth.authorizationUri,
  tokenURL: config.oauth.tokenUri,
  clientID: config.oauth.clientId,
  clientSecret: config.oauth.clientSecret,
  callbackURL: '/oauth/callback',
  passReqToCallback: false
}, function (accessToken, refreshToken, profile, done) {
  profile.accessToken = accessToken;
  done(null, profile);
});

strategy.setUserProfileURI(config.oauth.userInfoUri);
passport.use(strategy);

passport.serializeUser(function (user, done) {
  done(null, user);
});
passport.deserializeUser(function (user, done) {
  done(null, user);
});

module.exports = {
  passport: passport,
  strategy: strategy
};
