/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var _ = require('underscore');
var defaults = require('./default-config.json');

var thr = require('throw');

var vcapServices = JSON.parse(process.env.VCAP_SERVICES || thr('VCAP_SERVICES env is required'));

var userProvided = vcapServices['user-provided'] || [];

function getUserProvidedSerice(name) {
  var service = _.findWhere(userProvided, { name: name });
  return service && service.credentials;
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
  getSso: _.partial(getUserProvidedSerice, 'sso'),
  get: getVariable
};
