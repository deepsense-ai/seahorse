/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
