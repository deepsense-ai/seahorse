'use strict';

/* @ngInject */
function ReportsConfig($stateProvider) {
  $stateProvider.state('workflows.reportEntity', {
    url: '/reportEntity/:reportEntityId',
    templateUrl: 'app/workflows/reports/reports.html',
    views: {
      'reportView': {
        templateUrl: 'app/workflows/reports/reports.html',
        controller: 'ReportCtrl',
        controllerAs: 'report'
      }
    },
    resolve: {
      report: /* @ngInject */ ($q, $rootScope, $stateParams, $timeout, Report) => {
        let deferred = $q.defer();
        try {
          let reportEntity = Report.getReportEntity($stateParams.reportEntityId);
          $rootScope.stateData.dataIsLoaded = true;
          deferred.resolve(reportEntity.report);
        } catch (e) {
          $rootScope.stateData.errorMessage = 'Could not load the report';
          deferred.reject();
        }
        return deferred.promise;
      }
    }
  });
}

exports.function = ReportsConfig;

exports.inject = function(module) {
  module.config(ReportsConfig);
};
