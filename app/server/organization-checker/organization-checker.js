/**
 * Copyright (c) 2016, CodiLime Inc.
 */
const request = require('request'),
  Log = require('log'),
  log = new Log('info'),
  timeout = require('connect-timeout'),
  _ = require('lodash'),
  config = require('../config/config');

function organizationChecker(req, res, next) {
  if(req.session && req.session.organizationChecked) {
    log.debug('Organization already was checked in this session');
    next();
    return;
  }

  const token = req.user && req.user.accessToken;
  if (!token) {
    next(); // User must be logged first
  } else {
    const requestOpts = {
      url: 'http://user-management.' + config.domain + '/rest/orgs/permissions',
      headers: {
        'Authorization': 'bearer ' + token
      }
    };
    request(requestOpts, (error, response, rawBody) => {
      if (error) {
        log.error('Error while trying to get list of organizations', error);
        res.status(401).send('Cannot get list of organizations');
      } else {
        log.debug('Body', rawBody);
        const body = JSON.parse(rawBody);

        const hasSeahorseOrganization = _.filter(body, o => o.organization.metadata.guid === config.organizationId).length > 0;
        if (hasSeahorseOrganization) {
          log.info('User belongs to this organization');
          req.session.organizationChecked = true;
          req.session.save();
          next()
        } else {
          log.error("User does not belong to this organization");
          res.status(401).send('User does not belong to this organization');
        }
      }
    });
  }
}

module.exports = {organizationChecker};

