'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  let views = {
    'navBar': {
      template: '<workflow-report-status-bar ng-show="stateData.dataIsLoaded"></workflow-report-status-bar>'
    },
    'reportView': {
      templateUrl: 'app/workflows/workflows-execution-report/workflows-execution-report.html',
      controller: 'WorkflowsReportController as workflow'
    }
  };

  $stateProvider.state('workflows.latest_report', {
    url: '/:id/latest_report',
    'views': views,
    resolve: {
      report: /* @ngInject */ ($q, $state, $rootScope, $stateParams, WorkflowsApiClient) => {
        let workflowId = $stateParams.id;
        let deferred = $q.defer();
        WorkflowsApiClient.getLatestReport(workflowId).then((data) => {
            $rootScope.stateData.dataIsLoaded = true;
            $state.go('workflows.report', {
              reportId: data.executionReport.id
            });
          })
          .catch(() => {
            $state.go('ConflictState', {
              id: $stateParams.reportId,
              type: 'report'
            });
            deferred.reject();
          });
        return deferred.promise;
      }
    }
  });

  $stateProvider.state('workflows.report', {
    url: '/report/:reportId',
    views: views,
    resolve: {
      report: /* @ngInject */ ($q, $state, $rootScope, $stateParams, WorkflowsApiClient,
        Operations, OperationsHierarchyService, ErrorService) => {
        let reportId = $stateParams.reportId;
        let deferred = $q.defer();
        Operations.load()
          .then(OperationsHierarchyService.load)
          .then(() => WorkflowsApiClient.getReport(reportId))
          .then((data) => {
            $rootScope.stateData.dataIsLoaded = true;
            deferred.resolve(data);
          })
          .catch((error) => {
            $state.go(ErrorService.getErrorState(error.status), {
              id: $stateParams.reportId,
              type: 'report'
            });
            deferred.reject();
          });
        return deferred.promise;
      }
    }
  });
}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
