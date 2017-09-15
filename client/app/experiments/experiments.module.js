/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var angular = require('angular');

/**
 * Experiments module.
 */
var experiments = angular.module('ds.experiments', []);

require('./experiment-list.js').inject(experiments);
require('./experiment.js').inject(experiments);
require('./experiments.config.js').inject(experiments);

module.exports = experiments;
