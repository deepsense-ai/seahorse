/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentsConfig($stateProvider) {
  $stateProvider
    .state('lab.experiments', {
      url: '/experiments',
      templateUrl: 'app/experiments/experiment-browser/experiment-browser.html',
      controller: 'ExperimentListController as experimentList',
      resolve: {
        /* @ngInject */
        experiments: ($q, $rootScope, ExperimentAPIClient) => {
          let deferred = $q.defer();

          ExperimentAPIClient.getList().
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(data);
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = 'Could not load the experiments list';
              deferred.reject();
            });

          return deferred.promise;
        }
      }
    });
}
exports.function = ExperimentsConfig;

exports.inject = function (module) {
  module.config(ExperimentsConfig);
};
