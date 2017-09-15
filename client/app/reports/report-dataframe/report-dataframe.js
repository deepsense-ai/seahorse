/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

function ReportDataframe() {
  return {
    scope: {
      'data': '=',
      'distributionsTypes': '='
    },
    templateUrl: 'app/reports/report-dataframe/report-dataframe.html',
    replace: 'true',
    controller: function($scope) {
      this.extendedMainPanel = true;
      $scope.$on(REPORT_EVENTS.SHRINK_SIDE_PANEL, () => { this.extendedMainPanel = true; });
      $scope.$on(REPORT_EVENTS.EXTEND_SIDE_PANEL, () => { this.extendedMainPanel = false; });

      this.tableData = this.data['Data Sample'];
      this.tableSizes = this.data['DataFrame Size'];

      this.tableColumnsData = {};
      _.forEach(this.distributionsTypes, function (distType, colName) {
        let icon = distType === 'categorical' ? 'fa-pie-chart' : 'fa-bar-chart-o';
        this.tableColumnsData[colName] = {
          'icon': icon
        };
      }.bind(this));
    },
    controllerAs: 'reportDataframe',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('reportDataframe', ReportDataframe);
};
