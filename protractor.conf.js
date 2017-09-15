/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var config = require('./config.json');


exports.config = {
  specs: [config.files.tests.e2e],
  baseUrl: config.env.frontend.host + ':' + config.env.frontend.port
};
