/**
 * Copyright (c) 2016, CodiLime Inc.
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
