/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

function ReportCrossValidateRegressor() {
  return {
    scope: {
      'data': '='
    },
    templateUrl: 'app/workflows/reports/report-cross-validate-regressor/report-cross-validate-regressor.html',
    replace: 'true',
    controller: function() {
      this.tableData = this.data['Cross-validate report table'];
    },
    controllerAs: 'reportCrossValidateRegressor',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('reportCrossValidateRegressor', ReportCrossValidateRegressor);
};
