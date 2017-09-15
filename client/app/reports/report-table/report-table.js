/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

function ReportTable() {
  return {
    scope: {
      'data': '=',
      'distributionsTypes': '='
    },
    templateUrl: 'app/reports/report-table/report-table.html',
    replace: 'true',
    controller: function($scope) {
      this.extendedMainPanel = true;
      $scope.$on(REPORT_EVENTS.SHRINK_SIDE_PANEL, () => { this.extendedMainPanel = true; });
      $scope.$on(REPORT_EVENTS.EXTEND_SIDE_PANEL, () => { this.extendedMainPanel = false; });
    },
    controllerAs: 'reportTable',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('reportTable', ReportTable);
};
