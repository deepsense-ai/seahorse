'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.
    state('workflows.latest_report', {
      url: '/:id/report',
      templateUrl: 'app/workflows/workflows-execution-report/workflows-execution-report.html',
      controller: 'WorkflowsReportController as workflow',
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
}

exports.function = WorkflowsConfig;

exports.inject = function (module) {
  module.config(WorkflowsConfig);
};
