/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/* @ngInject */
function DeployModelService($modal, ModelApiClient) {
  this.showModal = (data) => {
    $modal.open({
      templateUrl: 'app/experiments/experiment-editor/graph-panel/deploy-model/deploy-model.tmpl.html',
      controller: ($scope, $modalInstance) => {
        $scope.close = function () {
          $modalInstance.close();
        };

        ModelApiClient.deployModel(data.id).
          then((response) => {
            $scope.linkValue = response.data.link;
            // TODO: mock
            $scope.features = '{ \n    "features" : [1.1, 1.9, 5.7, 7.7] \n}';
            $scope.scores = '{ \n    "score": 0.1 \n}';
          }).
          catch((error) => {
            console.error('deploy api call error', error);
            $scope.error = true;
          });
      }
    });
  };
}

exports.inject = function (module) {
  module.service('DeployModelService', DeployModelService);
};

