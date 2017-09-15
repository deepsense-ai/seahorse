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

import moment from 'moment';
import tpl from './report-chart-panel.html';

var EVENTS = {
  SELECT_COLUMN: 'select-column',
  DESELECT_COLUMN: 'deselect-column'
};

/* @ngInject */
function ReportCtrl($scope, $uibModal, BottomBarService) {
  let that = this;
  let internal = {};
  let obj = {};

  if (this.currentReport) {
    internal.name = this.currentReport.name;
    internal.tables = this.currentReport.tables;
    internal.distributions = this.currentReport.distributions || {};
    internal.reportId = this.currentReport.reportId;
  }

  internal.checkHeight = () => {
    if (!this.currentReport) {
      return false;
    }

    let values = 0;

    _.each(this.currentReport.tables, dataObject => {
      values += dataObject.values.length;
    });

    this.autoHeight = values < 10;
  };

  that.getReport = () => {
    return that.currentReport;
  };

  that.getTables = () => {
    internal.checkHeight();
    return that.currentReport && that.currentReport.tables;
  };

  that.getDistributionObject = colName => {
    if (that.currentReport.distributions) {
      return that.currentReport.distributions[colName];
    }
  };

  that.getDistributionsTypes = () => {
    return that.currentReport && _.reduce(
        that.currentReport.distributions,
        function (acc, distObj, name) {
          let re = /[a-zA-Z0-9_]+/.exec(name);
          if (re) {
            acc[re[0]] = distObj.subtype;
          }
          return acc;
        }, obj
      );
  };

  that.getReportName = () => {
    return that.currentReport && that.currentReport.name;
  };

  that.close = () => {
    BottomBarService.deactivatePanel('reportTab');
  };

  $scope.$on(EVENTS.SELECT_COLUMN, function (event, data) {
    let distObject = that.getDistributionObject(data.colName);
    let colType = data.colType;
    let colTypesMap = data.colTypesMap;
    let distributions = data.distributions;
    let colTypesWithDistributions = {};

    for (let colName in colTypesMap) {
      if (distributions[colName] && distributions[colName].subtype !== 'no_distribution') {
        colTypesWithDistributions[colName] = colTypesMap[colName];
      }
    }

    if (!_.isUndefined(distObject)) {
      $uibModal.open({
        size: 'lg',
        templateUrl: tpl,
        /* @ngInject */
        controller: function ($scope, $uibModalInstance, $filter) {
          _.assign(this, {
            close: () => {
              $uibModalInstance.close();
            },
            colType: colType,
            distObject: distObject,
            columnNames: _.keys(colTypesWithDistributions),
            selectedColumn: distObject.name,
            shortenValues: (value) => {
              if (this.colType === 'numeric') {
                return $filter('precision')(value);
              } else if (this.colType === 'timestamp') {
                return moment(new Date(value)).format('YYYY-MM-DD HH:mm:ss');
              }
              return value;
            }
          });

          $scope.$watch('graphModal.selectedColumn', (newValue, oldValue) => {
            if (newValue !== oldValue) {
              this.distObject = that.getDistributionObject(newValue);
              this.colType = colTypesMap[newValue];
            }
          });
        },
        controllerAs: 'graphModal'
      });
    }
  });

  return that;
}

exports.function = ReportCtrl;
exports.EVENTS = EVENTS;

exports.inject = function (module) {
  module.controller('ReportCtrl', ReportCtrl);
};
