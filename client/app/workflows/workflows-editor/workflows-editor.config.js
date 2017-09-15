'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.state('workflows.editor', {
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
      workflow: /* @ngInject */ ($q, $state, $rootScope, $stateParams,
        WorkflowsApiClient, Operations, OperationsHierarchyService, ErrorService) => {
        let deferred = $q.defer();
        Operations.load()
          .then(OperationsHierarchyService.load)
          .then(() => WorkflowsApiClient.getWorkflow($stateParams.id))
          .then((data) => {
            $rootScope.stateData.dataIsLoaded = true;
            deferred.resolve(data);
          })
          .catch((error) => {
            $state.go(ErrorService.getErrorState(error.status), {
              id: $stateParams.reportId,
              type: 'workflow'
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
