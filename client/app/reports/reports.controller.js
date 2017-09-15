/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var EVENTS = {
  'SELECT_COLUMN': 'select-column',
  'DESELECT_COLUMN': 'deselect-column'
};

/* @ngInject */
function Report(report, $scope, $modal, PageService) {
  PageService.setTitle(`Report: ${report.name}`);

  let that = this;
  let internal = {};

  internal.name = report.name;
  internal.tables = report.tables;
  internal.distributions = report.distributions || {};
  internal.distributionsTypes = _.reduce(
    internal.distributions,
    function (acc, distObj, name) {
      let re = /[a-zA-Z0-9]+/.exec(name);
      if (re) {
        acc[re[0]] = distObj.subtype;
      }
      return acc;
    },
    {}
  );

  that.getTables = function getTables() {
    return internal.tables;
  };

  that.getDistributionObject = function getDistributionObject(colName) {
    if (internal.distributions) {
      return internal.distributions[colName];
    }
  };

  that.getDistributionsTypes = function getDistributionsTypes() {
    return internal.distributionsTypes;
  };

  that.getReportName = function getReportName() {
    return internal.name;
  };

  $scope.$on(EVENTS.SELECT_COLUMN, function (event, data) {
    let distObject = that.getDistributionObject(data.colName);

    if (!_.isUndefined(distObject)) {
      $modal.open({
        size: 'lg',
        templateUrl: 'app/reports/report-chart-panel.html',
        controller: function ($scope, $modalInstance) {
          _.assign(this, {
            close: () => {
              $modalInstance.close();
            },
            distObject: distObject,
            columnNames: _.keys(internal.distributions),
            selectedColumn: distObject.name
          });

          $scope.$watch('graphModal.selectedColumn', (newValue, oldValue) => {
            if (newValue !== oldValue) {
              this.distObject = that.getDistributionObject(newValue);
            }
          });
        },
        controllerAs: 'graphModal'
      });
    }
  });

  return that;
}

exports.function = Report;
exports.EVENTS = EVENTS;

exports.inject = function (module) {
  module.controller('Report', Report);
};
