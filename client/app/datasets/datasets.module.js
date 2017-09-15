/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var datasets = angular.module('ds.datasets', []);

require('./dataset-list.js').inject(datasets);
require('./datasets.config.js').inject(datasets);

module.exports = datasets;
