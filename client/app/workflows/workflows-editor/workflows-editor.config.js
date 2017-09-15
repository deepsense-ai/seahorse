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
      workflowWithResults: /* @ngInject */ ($q, $state, $rootScope, $stateParams,
        $timeout, WorkflowsApiClient, Operations, OperationsHierarchyService,
        ErrorService, ServerCommunication) => {
        let workflowWithResultsDeferred = $q.defer();
        ServerCommunication.init($stateParams.id);

        $rootScope.$on('ServerCommunication.MESSAGE.workflowWithResults', (event, data) => {
          workflowWithResultsDeferred.resolve(data);
        });

        return $q.all([
          workflowWithResultsDeferred.promise,
          Operations.load().then(OperationsHierarchyService.load)
        ]).then((results) => {
          $rootScope.stateData.dataIsLoaded = true;

          let deferred = $q.defer();
          deferred.resolve(results);
          return deferred.promise;
        });
      }
    }
  });
}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
