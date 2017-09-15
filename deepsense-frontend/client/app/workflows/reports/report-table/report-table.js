'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

function ReportTable() {
  return {
    templateUrl: 'app/workflows/reports/report-table/report-table.html',
    scope: {
      'table': '=',
      // Even though distributions are strictly connected to tables at the moment, we have them
      // at root level next to tables for now.
      'distributions': '=',
      'datatypesVisible': '=?'
    },
    controller: function($scope, $rootScope) {
      $scope.reportWidth = window.innerWidth / 2;
      $scope.maxLength = $scope.reportWidth / $scope.table.columnNames.length;

      $scope.getColumnType = (columnName) => {
        // Not using zip to avoid object allocation every digest cycle.
        let indexOfColumn = $scope.table.columnNames.indexOf(columnName);
        return $scope.table.columnTypes[indexOfColumn];
      };

      $scope.getDistributionType = (columnName) => {
        return $scope.distributions && $scope.distributions[columnName];
      };

      $scope.getTriggerEventBasedOnDescriptionLength = (value) => {
        return value.length > $scope.maxLength ? 'mouseenter' : 'none';
      };

      this.showDistribution = (columnName) => {
        if ($scope.getDistributionType(columnName)) {
          $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
            colName: columnName
          });
        }
      };

    },
    controllerAs: 'controller'
  };
}

exports.inject = function(module) {
  module.directive('reportTable', ReportTable);
};
