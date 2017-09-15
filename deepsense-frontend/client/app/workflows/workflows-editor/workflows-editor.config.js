'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {

  $stateProvider.state('workflows.editor', {
    url: '/:id/editor',
    templateUrl: 'app/workflows/workflows-editor/workflows-editor.html',
    controller: 'WorkflowsEditorController as workflow',
    resolve: {
      workflowWithResults: /* @ngInject */ ($q, $rootScope, $stateParams, $state, NotificationService,
        WorkflowService, Operations, OperationsHierarchyService, ServerCommunication) => {
        ServerCommunication.init($stateParams.id);
        return $q.all([
          WorkflowService.downloadWorkflow($stateParams.id),
          Operations.load().then(OperationsHierarchyService.load)
        ]).then(([workflow, ..._]) => {
          $rootScope.stateData.dataIsLoaded = true;
          return workflow;
        }).catch((error) => {
          console.error(`Problem with opening workflow ${$stateParams.id}`, error);
          $state.go('home', {}, {reload: true});
          NotificationService.showError({
            title: 'Problem with opening workflow',
            message: `Problem occured while opening workflow with id ${$stateParams.id}`
          });
        });
      }
    }
  });

}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
