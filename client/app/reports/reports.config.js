/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function ReportsConfig($stateProvider) {
  $stateProvider.state('lab.report', {
      url: '/report/:id',
      templateUrl: 'app/reports/reports.html',
      controller: 'Report',
      controllerAs: 'report',
      resolve: {
        /* @ngInject */
        report: ($q, $stateParams, $rootScope, EntitiesApiClient) => {
          let deferred = $q.defer();

          EntitiesApiClient.getReport($stateParams.id).
            then((data) => {
              $rootScope.stateData.dataIsLoaded = true;
              deferred.resolve(JSON.parse(data.entity.report));
            }).
            catch(() => {
              $rootScope.stateData.errorMessage = 'Could not load the report';
              deferred.reject();
            });

          return deferred.promise;
        }
      }
  });
}

exports.function = ReportsConfig;

exports.inject = function (module) {
  module.config(ReportsConfig);
};
