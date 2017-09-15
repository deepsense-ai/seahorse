/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var angular = require('angular');

/**
 * Reports module.
 */
var reports = angular.module('ds.reports', []);

require('./reports.controller.js').inject(reports);
require('./reports.config.js').inject(reports);

require('./report-side-panel/report-side-panel.js').inject(reports);
require('./report-table/report-table.js').inject(reports);

require('./report-table/table-data/table-data.js').inject(reports);
require('./report-table/table-header/table-header.js').inject(reports);

require('./charts/distribution-categorical-chart.js').inject(reports);
require('./charts/distribution-continuous-chart.js').inject(reports);

require('./charts/box-plot.js').inject(reports);
require('./charts/column-plot.js').inject(reports);
require('./charts/pie-plot.js').inject(reports);

module.exports = reports;
