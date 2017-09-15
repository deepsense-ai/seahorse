'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.
  state('workflows.editor', {
    url: '/:id/editor',
    views: {
      'navBar': {
        template: '<workflow-editor-status-bar ng-show="stateData.dataIsLoaded"></workflow-editor-status-bar>'
      },
      'reportView': {
        templateUrl: 'app/workflows/workflows-editor/workflows-editor.html',
        controller: 'WorkflowsEditorController as workflow'
      }
    },
    resolve: {
      workflow: /* @ngInject */ ($q, $rootScope, $stateParams,
        WorkflowsApiClient, Operations, OperationsHierarchyService) => {
        let workflowId = $stateParams.id;
        let deferred = $q.defer();

        Operations.load()
          .
        then(OperationsHierarchyService.load)
          .
        then(() => WorkflowsApiClient.getWorkflow(workflowId))
          .
        then((data) => {
            $rootScope.stateData.dataIsLoaded = true;
            deferred.resolve(data);
          })
          .
        catch((error) => {
          $rootScope.stateData.errorMessage = `Could not load the workflow \n ${error.statusText}`;
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
