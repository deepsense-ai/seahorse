'use strict';

var EVENTS = {
  'SELECT_COLUMN': 'select-column',
  'DESELECT_COLUMN': 'deselect-column'
};

/* @ngInject */
function ReportCtrl(report, $scope, $state, $uibModal, PageService) {
  PageService.setTitle(`Report: ${report.name}`);

  let that = this;
  let internal = {};

  internal.name = report.name;
  internal.tables = report.tables;
  internal.distributions = report.distributions || {};
  internal.reportId = report.reportId;
  internal.distributionsTypes = _.reduce(
    internal.distributions,
    function(acc, distObj, name) {
      let re = /[a-zA-Z0-9_]+/.exec(name);
      if (re) {
        acc[re[0]] = distObj.subtype;
      }
      return acc;
    }, {}
  );

  that.back = () => {
    $state.go('workflows.report', {
      'reportId': internal.reportId
    });
  };

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

  $scope.$on(EVENTS.SELECT_COLUMN, function(event, data) {
    let distObject = that.getDistributionObject(data.colName);

    if (!_.isUndefined(distObject)) {
      $uibModal.open({
        size: 'lg',
        templateUrl: 'app/workflows/reports/report-chart-panel.html',
        /* @ngInject */
        controller: function($scope, $uibModalInstance) {
          _.assign(this, {
            close: () => {
              $uibModalInstance.close();
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

exports.function = ReportCtrl;
exports.EVENTS = EVENTS;

exports.inject = function(module) {
  module.controller('ReportCtrl', ReportCtrl);
};
