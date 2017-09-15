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
    controller: function($scope, $rootScope, $filter) {
      let columnTypes = [];

      $scope.reportWidth = window.innerWidth / 2;
      const maxLength = $scope.reportWidth / $scope.table.columnNames.length;
      const map = {};

      $scope.table.columnNames.forEach((name) => {
        const indexOfColumn = $scope.table.columnNames.indexOf(name);
        const columnType = $scope.table.columnTypes[indexOfColumn];
        map[name] = columnType;
        columnTypes[indexOfColumn] = columnType;
      });

      $scope.getColumnType = (columnName) => {
        // Not using zip to avoid object allocation every digest cycle.
        const indexOfColumn = $scope.table.columnNames.indexOf(columnName);
        return $scope.table.columnTypes[indexOfColumn];
      };

      $scope.getDistributionType = (columnName) => {
        return $scope.distributions && $scope.distributions[columnName];
      };

      $scope.isLongEnoughToBeCutOff = (value) => {
        if (value) {
          return value.length > maxLength;
        }
        return false;
      };

      $scope.shortenValues = (value, index) => {
        if (columnTypes[index] === 'numeric') {
          return $filter('precision')(value);
        }
        return $filter('cut')(value, true, maxLength, ' ...');
      };

      this.showDistribution = (columnName) => {
        if ($scope.getDistributionType(columnName)) {
          $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
            colName: columnName,
            colType: $scope.getColumnType(columnName),
            colTypesMap: map,
            distributions: $scope.distributions
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
