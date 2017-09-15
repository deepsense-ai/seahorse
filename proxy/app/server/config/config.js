/**
 * Copyright (c) 2016, CodiLime Inc.
 */
const _ = require('underscore');
const defaults = require('./default-config.json');
const serviceMapping = require('./service-mapping');

const oauth = {
  "clientSecret": "seahorse01",
  "tokenUri": `${serviceMapping.authorization.host}/authorization/oauth/token`,
  "clientId": "Seahorse",
  "logoutUri": "/authorization/logout.do",
  "authorizationUri": "/authorization/oauth/authorize",
  "userInfoUri": `${serviceMapping.authorization.host}/authorization/userinfo`
};

function getVariable(name) {
  if(!name || !_.isString(name)) {
    return null;
  }
  var value = process.env[name.toUpperCase()];
  if(!value) {
    value = defaults[name.toLowerCase()];
  }
  return value;
}

module.exports = {
  oauth,
  get: getVariable
};
