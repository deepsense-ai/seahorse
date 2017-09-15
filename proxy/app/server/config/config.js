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

const _ = require('underscore');
const defaults = require('./default-config.json');
const serviceMapping = require('./service-mapping');
const thr = require('throw');

const oauth = {
  "clientSecret": "seahorse01",
  "tokenUri": `${serviceMapping.authorization.host}/authorization/oauth/token`,
  "clientId": "Seahorse",
  "logoutUri": "/authorization/logout.do",
  "authorizationUri": "/authorization/oauth/authorize",
  "userInfoUri": `${serviceMapping.authorization.host}/authorization/userinfo`
};

function getMandatory(name) {
  return getVariable(name) || thr(`${name} must be defined.`);
}

function getVariable(name) {
  if(!name || !_.isString(name)) {
    return null;
  }
  const value = process.env[name.toUpperCase()];
  if(value) {
    return value;
  } else {
    return defaults[name.toLowerCase()];
  }
}



module.exports = {
  oauth,
  getMandatory,
  get: getVariable,
};
