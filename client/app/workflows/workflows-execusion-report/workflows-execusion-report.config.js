'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.
    state('workflows_latest_report', {
      url: '/workflows/:id/report',
      templateUrl: 'app/workflows/workflows-execution-report/workflows-execution-report.html',
      controller: 'WorkflowsReportController as workflow',
      resolve: {
        report: /* @ngInject */($q, $rootScope, $stateParams, WorkflowsApiClient,
                                Operations, OperationsHierarchyService) =>
        {
          let workflowId = $stateParams.id;

          return Operations.load().
            then(OperationsHierarchyService.load).
            then(() => WorkflowsApiClient.getLatestReport(workflowId)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              return data;
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = `Could not load the latest report of the workflow with id ${workflowId}`;
            });
        }
      }
    });
}

exports.function = WorkflowsConfig;

exports.inject = function (module) {
  module.config(WorkflowsConfig);
};
