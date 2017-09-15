/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var passport = require('passport');
var OAUTH2Strategy = require('passport-cloudfoundry').Strategy;
var config = require('../config/config');

var sso = config.getSso();

var strategy = new OAUTH2Strategy({
  authorizationURL: sso.authorizationUri,
  tokenURL: sso.tokenUri,
  clientID: sso.clientId,
  clientSecret: sso.clientSecret,
  callbackURL: '/oauth/callback',
  passReqToCallback: false
}, function (accessToken, refreshToken, profile, done) {
  profile.accessToken = accessToken;
  done(null, profile);
});

strategy.setUserProfileURI(sso.userInfoUri);
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
