/**
 * Copyright (c) 2016, CodiLime Inc.
 */
const session = require('express-session');
const config = require('../config/config');
const request = require('request');

/*
 * When user quota is changed also change numbers in Seahorse documentation
 * We need one account for admin, so user_quota = number_of_user + 1 (for admin)
 */
const user_quota = 2;

module.exports = {
    forward: checkUserQuota,
};

function getToken(success, failure, tokenUri, client_id, client_secret) {

  function handleToken(error, response, body) {
            if (!error && response.statusCode == 200) {
              var authResponse = JSON.parse(body)
              var key = authResponse['access_token'];
              success(key);
            } else {
              failure(error);
            }
  }

  request.post(tokenUri, {
          "form": {
              "grant_type": "client_credentials",
              "client_id": client_id,
              "client_secret": client_secret,
              "response_type": "token"
          }
  }, handleToken);
}

function checkUserQuota(req, res, next) {
    const handleScimUserResponse = function(error, response, body) {
        if (!error && response.statusCode == 200) {
          console.error(body);
          users = JSON.parse(body)
          if (users.totalResults < user_quota) {
              next()
          } else {
              res.redirect('/quota.html');
          }
        } else {
          res.redirect('/quota.html?limit_code=404');
        }
    };

    const callUsersUri = function(key) {
        const usersUri = config.oauth.tokenUri + "/../../Users"
        request.get(usersUri, handleScimUserResponse).auth(null, null, true, key);
    };

    const failure = function(error) {
        console.error('Cannot connect to auth server');
        res.redirect('/quota.html?limit_code=401');
    };

    getToken(callUsersUri, failure, config.oauth.tokenUri, "admin", "adminsecret");
}
