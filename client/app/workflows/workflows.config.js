'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.
    state('workflows_editor', {
      url: '/workflows/editor/:id',
      templateUrl: 'app/workflows/workflows-editor/workflows-editor.html',
      controller: 'WorkflowsController as workflow',
      resolve: {
        workflow: /* @ngInject */($q, $rootScope, $stateParams, Operations, OperationsHierarchyService, WorkflowsApiClient) => {
          let deferred = $q.defer();

          Operations.load().
            then(OperationsHierarchyService.load).
            then(() => WorkflowsApiClient.getWorkflow($stateParams.id)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(data);
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = 'Could not load the workflow';
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
