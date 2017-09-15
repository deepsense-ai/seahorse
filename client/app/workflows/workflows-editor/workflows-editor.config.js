'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.
    state('workflows_editor', {
      url: '/workflows/:id/editor',
      templateUrl: 'app/workflows/workflows-editor/workflows-editor.html',
      controller: 'WorkflowsEditorController as workflow',
      resolve: {
        workflow: /* @ngInject */($q, $rootScope, $stateParams,
                                  WorkflowsApiClient, Operations, OperationsHierarchyService) =>
        {
          let workflowId = $stateParams.id;

          return Operations.load().
            then(OperationsHierarchyService.load).
            then(() => WorkflowsApiClient.getWorkflow(workflowId)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              return data;
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = `Could not load the workflow with id ${workflowId}`;
            });
        }
      }
    });
}

exports.function = WorkflowsConfig;

exports.inject = function (module) {
  module.config(WorkflowsConfig);
};
