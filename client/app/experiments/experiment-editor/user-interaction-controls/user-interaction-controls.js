/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function UserInteractionControls() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls.html',
    controller: UserInteractionControlsController,
    controllerAs: 'userInteractionControlsController'
  };
}

function UserInteractionControlsController ($scope) {
  $scope.test = 10;
}

exports.inject = function (module) {
  module.directive('userInteractionControls', UserInteractionControls);
};
