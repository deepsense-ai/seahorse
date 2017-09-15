/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var EVENTS = {
  'SELECT_COLUMN': 'select-column',
  'DESELECT_COLUMN': 'deselect-column'
};

/* @ngInject */
function Report(PageService, $scope, $stateParams, $modal, EntitiesAPIClient) {
  let that = this;
  let internal = {};
  let entityId = $stateParams.id;

  that.messageError = '';

  EntitiesAPIClient.getReport(entityId).then((data) => {
    internal.tables = data.tables;
    internal.distributions = data.distributions || {};
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

    PageService.setTitle(`Report: ${data.name}`);
  }, () => {
    that.messageError = `The report with id equals to ${entityId} does not exist!`;
  });

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

  that.getReportType = function getReportType() {
    let tables = that.getTables();
    if (_.has(tables, 'Data Sample')) {
      return 'Data Sample';
    } else if (_.has(tables, 'CrossValidateRegressor')) {
      return 'CrossValidateRegressor';
    } else if (_.has(tables, 'EvaluateRegression')) {
      return 'EvaluateRegression';
    }
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
