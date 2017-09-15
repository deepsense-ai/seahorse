/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ReportsConfig($stateProvider) {
  $stateProvider.state('lab.report', {
      url: '/report/:id',
      templateUrl: 'app/reports/reports.html',
      controller: 'Report',
      controllerAs: 'report'
  });
}
exports.function = ReportsConfig;

exports.inject = function (module) {
  module.config(ReportsConfig);
};
