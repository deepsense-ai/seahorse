/**
 * Copyright (c) 2016, CodiLime Inc.
 */
module.exports = {
  init: init,
  login: login,
  checkLoggedIn: checkLoggedIn
};

function init(app) {
  app.use(function(req, res, next) {
    req.user = {
      user_id: "00000000-0000-0000-0000-000000000001",
      user_name: "user",
      accessToken: "sometoken"
    };
    next();
  });

  app.get('/logout', function (req, res) {
    res.redirect('/');
  });
}

function login(req, res, next) {
  next();
}

function checkLoggedIn(req, res, next) {
  next();
}
