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

  $stateProvider.
    state('workflows.latest_report', {
      url: '/:id/latest_report',
      'views': views,
      resolve: {
        report: /* @ngInject */($q, $rootScope, $stateParams, WorkflowsApiClient,
                                Operations, OperationsHierarchyService) =>
        {
          let workflowId = $stateParams.id;
          let deferred = $q.defer();

          Operations.load().
            then(OperationsHierarchyService.load).
            then(() => WorkflowsApiClient.getLatestReport(workflowId)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(data);
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = `Could not load the latest report of the workflow with id ${workflowId}`;
              deferred.reject();
            });

          return deferred.promise;
        }
      }
    });

  $stateProvider.
    state('workflows.report', {
      url: '/report/:reportId',
      views: views,
      resolve: {
        report: /* @ngInject */($q, $rootScope, $stateParams, WorkflowsApiClient,
                                Operations, OperationsHierarchyService) =>
        {
          let reportId = $stateParams.reportId;
          let deferred = $q.defer();

          Operations.load().
            then(OperationsHierarchyService.load).
            then(() => WorkflowsApiClient.getReport(reportId)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(data);
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = `Could not load the report of id ${reportId}`;
              deferred.reject();
            });

          return deferred.promise;
        }
      }
    });

}

exports.function = WorkflowsConfig;

exports.inject = function (module) {
  module.config(WorkflowsConfig);
};
