/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentConfig($stateProvider) {
  $stateProvider
    .state('lab.experiment', {
      url: '/experiment/:id',
      templateUrl: 'app/experiments/experiment-editor/experiment-editor.html',
      controller: 'ExperimentController as experiment',
      resolve: {
        /* @ngInject */
        experiment: ($q, $rootScope, $stateParams, Operations, OperationsHierarchyService, ExperimentApiClient) => {
          let deferred = $q.defer();

          Operations.load().
            then(OperationsHierarchyService.load).
            then(() => ExperimentApiClient.getData($stateParams.id)).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(data);
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = 'Could not load the experiment';
              deferred.reject();
            });

          return deferred.promise;
        }
      }
    });
}
exports.function = ExperimentConfig;

exports.inject = function (module) {
  module.config(ExperimentConfig);
};
