/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var _ = require('underscore');
var defaults = require('./default-config.json');

var thr = require('throw');

var vcapServices = JSON.parse(process.env.VCAP_SERVICES || thr('VCAP_SERVICES env is required'));
var domain = process.env.DOMAIN || thr('DOMAIN env is required');
var organizationId = process.env.ORGANIZATION_ID || thr('ORGANIZATION_ID env is required');
var checkOrganization = process.env.CHECK_ORGANIZATION || thr('CHECK_ORGANIZATION env is required');

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
  domain,
  organizationId,
  checkOrganization,
  getSso: _.partial(getUserProvidedSerice, 'sso'),
  get: getVariable
};
