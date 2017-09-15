/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var angular = require('angular');


/**
 * Providers module.
 */
var providers = angular.module('ds.providers', []);

require('./BaseAPIClient.factory.js').inject(providers);
require('./ExperimentAPIClient.factory.js').inject(providers);
require('./OperationsAPIClient.factory.js').inject(providers);
require('./EntitiesAPIClient.factory.js').inject(providers);
require('./ReportOptions.service.js').inject(providers);
require('./ReportOptions.controller.js').inject(providers);
require('./Operations.factory.js').inject(providers);

module.exports = providers;
