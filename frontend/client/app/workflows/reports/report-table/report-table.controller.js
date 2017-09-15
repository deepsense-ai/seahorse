/**
 * Copyright 2017, deepsense.ai
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

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

/* @ngInject */
function ReportTableController($scope, $rootScope, $filter) {
  const controller = this;
  const map = {};
  const columnTypes = [];

  let maxLength;

  controller.getColumnType = getColumnType;
  controller.getDistributionType = getDistributionType;
  controller.isLongEnoughToBeCutOff = isLongEnoughToBeCutOff;
  controller.shortenValues = shortenValues;
  controller.showDistribution = showDistribution;

  activate();

  function activate() {
    controller.reportWidth = window.innerWidth / 2;
    maxLength = controller.reportWidth / controller.table.columnNames.length;

    $scope.$watch(() => controller.table.columnNames, () => {
      controller.table.columnNames.forEach((name) => {
        const indexOfColumn = controller.table.columnNames.indexOf(name);
        const columnType = controller.table.columnTypes[indexOfColumn];
        map[name] = columnType;
        columnTypes[indexOfColumn] = columnType;
      });
    });
  }

  function getColumnType(columnName) {
    // Not using zip to avoid object allocation every digest cycle.
    const indexOfColumn = controller.table.columnNames.indexOf(columnName);
    return controller.table.columnTypes[indexOfColumn];
  }

  function getDistributionType(columnName) {
    return controller.distributions && controller.distributions[columnName];
  }

  function isLongEnoughToBeCutOff(value) {
    if (value) {
      return value.length > maxLength;
    }
    return false;
  }

  function shortenValues(value, index) {
    if (columnTypes[index] === 'numeric') {
      return $filter('precision')(value);
    }
    return $filter('cut')(value, true, maxLength, ' ...');
  }

  function showDistribution(columnName) {
    if (controller.getDistributionType(columnName)) {
      $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
        colName: columnName,
        colType: controller.getColumnType(columnName),
        colTypesMap: map,
        distributions: controller.distributions
      });
    }
  }
}

exports.inject = function(module) {
  module.controller('reportTableController', ReportTableController);
};
