'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {

  $stateProvider.state('workflows.editor', {
    url: '/:id/editor',
    templateUrl: 'app/workflows/workflows-editor/workflows-editor.html',
    controller: 'WorkflowsEditorController as workflow',
    resolve: {
      workflowWithResults: /* @ngInject */ ($q, $rootScope, $stateParams,
        WorkflowService, Operations, OperationsHierarchyService, ServerCommunication) => {
        ServerCommunication.init($stateParams.id);
        return $q.all([
          WorkflowService.downloadWorkflow($stateParams.id),
          Operations.load().then(OperationsHierarchyService.load)
        ]).then(([workflow, ..._]) => {
          $rootScope.stateData.dataIsLoaded = true;
          return workflow;
        });
      }
    }
  });

}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
