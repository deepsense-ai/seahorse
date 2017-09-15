/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

function ReportSidePanel() {
  return {
    templateUrl: 'app/reports/report-side-panel/report-side-panel.html',
    replace: 'true',
    scope: true,
    controller: function($scope) {
      _.assign(this, {
        extendedSidePanel: false,
        distObject: null,
        shrinkPanel: () => {
          $scope.$emit(REPORT_EVENTS.HIDE_DETAILS);
        }
      });

      $scope.$on(REPORT_EVENTS.EXTEND_SIDE_PANEL, (event, data) => {
        this.extendedSidePanel = true;
        this.distObject = $scope.report.getDistributionObject(data.colName);
      });
      $scope.$on(REPORT_EVENTS.SHRINK_SIDE_PANEL, () => {
        this.extendedSidePanel = false;
      });
    },
    controllerAs: 'reportSidePanel'
  };
}

exports.inject = function (module) {
  module.directive('reportSidePanel', ReportSidePanel);
};
