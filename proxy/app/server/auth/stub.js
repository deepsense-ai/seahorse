/**
 * Copyright (c) 2016, CodiLime Inc.
 */
module.exports = {
  init: init,
  login: login,
};

function init(app) {
  app.use(function(req, res, next) {
    // user_id is also used in fronted for detecting if oauth is on.
    // If user_id is 00000000-0000-0000-0000-000000000001 then auth is off.
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
