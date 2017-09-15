/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

let EVENTS = {
  'EXTEND_PANEL': 'extend-panel',
  'SHRINK_PANEL': 'shrink-panel'
};

function ReportSidePanel() {
  return {
    templateUrl: 'app/reports/report-side-panel/report-side-panel.html',
    replace: 'true',
    scope: true,
    controller: function($scope) {
      $scope.$on(EVENTS.EXTEND_PANEL, function(event, data) {
        $scope.distObject = $scope.report.getDistributionObject(data.colName);
        $scope.$digest();
      });

      $scope.distObject = null;
    },
    controllerAs: 'reportSidePanel'
  };
}

exports.EVENTS = EVENTS;

exports.inject = function (module) {
  module.directive('reportSidePanel', ReportSidePanel);
};
