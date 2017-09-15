'use strict';

const angular = require('angular');
const enums = angular.module('ds.enums', []);

require('./session-status.js').inject(enums);

module.exports = enums;
