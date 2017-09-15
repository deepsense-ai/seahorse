'use strict';

import tpl from './workflows-editor.html';

/* @ngInject */
function WorkflowsConfig($stateProvider) {

  $stateProvider.state('workflows.editor', {
    url: '/:id/editor',
    templateUrl: tpl,
    controller: 'WorkflowsEditorController as workflow',
    resolve: {
      workflowWithResults: /* @ngInject */ ($q, $rootScope, $stateParams, $state, NotificationService,
        WorkflowService, Operations, OperationsHierarchyService, ServerCommunication, UserService) => {
        return $q.all([
            WorkflowService.downloadWorkflow($stateParams.id),
            Operations.load().then(OperationsHierarchyService.load)
          ])
          .then(([workflow, ..._]) => {
            const workflowOwnedByCurrentUser = UserService.getSeahorseUser().id === workflow.workflowInfo.ownerId;
            if (workflowOwnedByCurrentUser) {
              /* eslint-disable no-console */
              console.log('Current user is workflows owner. Registering to topics...');
              /* eslint-enable no-console */
              ServerCommunication.init(workflow.id);
            }
            $rootScope.stateData.dataIsLoaded = true;
            return workflow;
          })
          .catch((error) => {
            /* eslint-disable no-console */
            console.error(`Problem with opening workflow ${$stateParams.id}`, error);
            /* eslint-enable no-console */
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
