'use strict';

var angular = require('angular');
var errors = angular.module('ds.errors', []);
require('./errors.controller.js').inject(errors);
require('./errors.config.js').inject(errors);
require('./errors.service.js').inject(errors);
module.exports = errors;
