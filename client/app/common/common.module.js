/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var common = angular.module('ds.common', []);

require('./navigation-bar/navigation-bar.js').inject(common);
require('./navigation-bar/navigation-bar.controller.js').inject(common);
require('./page.service.js').inject(common);

module.exports = common;
