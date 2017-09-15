/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var home = angular.module('ds.home', []);

require('./home.ctrl.js').inject(home);
require('./home.config.js').inject(home);

module.exports = home;
