/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var angular = require('angular');

/**
 * Reports module.
 */
var reports = angular.module('ds.reports', []);

require('./report-table.js').inject(reports);
require('./reports.controller.js').inject(reports);
require('./reports.config.js').inject(reports);
require('./table-data/table-data.js').inject(reports);
require('./table-statistics/table-statistics.js').inject(reports);

module.exports = reports;
