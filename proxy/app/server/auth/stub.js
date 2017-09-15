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
