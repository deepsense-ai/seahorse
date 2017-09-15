/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var angular = require('angular');

var errors = angular.module('ds.errors', []);

require('./errors.config.js').inject(errors);

module.exports = errors;
