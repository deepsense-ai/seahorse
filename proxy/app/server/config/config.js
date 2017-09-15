/**
 * Copyright (c) 2016, CodiLime Inc.
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
