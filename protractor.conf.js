/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var config = require('./package.json');


exports.config = {
  specs: [config.files.tests.e2e],
  baseUrl: 'http://localhost:' + config.dev.server.port
};
