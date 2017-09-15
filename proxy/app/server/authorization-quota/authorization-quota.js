/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var session = require('express-session');
var config = require('../config/config');
var request = require('request');

var sso = config.getSso();

/*
 * When user quota is changed also change numbers in Seahorse documentation
 * We need one account for admin, so user_quota = number_of_user + 1 (for admin)
 */
var user_quota = 2

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
    var handleScimUserResponse = function(error, response, body) {
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
    }

    var callUsersUri = function(key) {
        var usersUri = sso.tokenUri + "/../../Users"
        request.get(usersUri, handleScimUserResponse).auth(null, null, true, key);
    }

    var failure = function(error) {
        console.error('Cannot connect to auth server');
        res.redirect('/quota.html?limit_code=401');
    }

    getToken(callUsersUri, failure, sso.tokenUri, "admin", "adminsecret");
}
