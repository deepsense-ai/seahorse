'use strict';

var angular = require('angular');

var reports = angular.module('ds.reports', []);

require('./reports.controller.js').inject(reports);
require('./reports.factory.js').inject(reports);
require('./report.directive.js').inject(reports);

require('./report-dataframe-full/report-dataframe-full.js').inject(reports);
require('./report-default/report-default.js').inject(reports);
require('./report-table/report-table.controller.js').inject(reports);
require('./report-table/report-table.directive.js').inject(reports);

require('./charts/distribution-categorical-chart.js').inject(reports);
require('./charts/distribution-continuous-chart.js').inject(reports);

require('./charts/box-plot.js').inject(reports);
require('./charts/column-plot.js').inject(reports);
require('./charts/pie-plot.js').inject(reports);
require('./charts/basic-line-plot.js').inject(reports);

module.exports = reports;
