'use strict';

import tpl from './report-dataframe-full.html';

function ReportDataframeFull() {
  return {
    scope: {
      'report': '='
    },
    templateUrl: tpl,
    replace: 'true',
    controller: function($scope) {
      $scope._getTableByName = (tableName) => _.find($scope.report.tables, (t) => t.name === tableName);
      $scope.getDataSample = () => $scope._getTableByName('Data Sample');
      $scope.getTableSizes = () => $scope._getTableByName('DataFrame Size');
      $scope.getColumnCount = () => $scope.getTableSizes().values[0][0];
      $scope.getRowCount = () => $scope.getTableSizes().values[0][1];
      $scope.getPreviewRowCount = () => $scope.getDataSample().values.length;
    },
    controllerAs: 'controller'
  };
}

exports.inject = function(module) {
  module.directive('reportDataframeFull', ReportDataframeFull);
};
