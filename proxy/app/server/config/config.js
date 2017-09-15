/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var _ = require('underscore');
var defaults = require('./default-config.json');

var vcapServices = JSON.parse(process.env.VCAP_SERVICES || '{}');
var vcapApplication = JSON.parse(process.env.VCAP_APPLICATION || '{}');
var userProvided = vcapServices['user-provided'] || [];

function getUserProvidedSerice(name) {
  var service = _.findWhere(userProvided, { name: name });
  return service && service.credentials;
}

function getDomain() {
  var domain = getVariable('domain');
  if(domain) {
    return domain;
  }

  if (vcapApplication.uris) {
    return vcapApplication.uris[0].split(".").slice(1).join(".");
  }

  throw new Error('Cannot fetch domain configuration');
}

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
  getUserProvidedSerice: getUserProvidedSerice,
  getDomain: getDomain,
  getSso: _.partial(getUserProvidedSerice, 'sso'),
  get: getVariable
};
