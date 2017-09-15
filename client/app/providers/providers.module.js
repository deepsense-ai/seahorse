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

require('./factory.BaseAPIClient.js').inject(providers);
require('./factory.ExperimentAPIClient.js').inject(providers);


module.exports = providers;
