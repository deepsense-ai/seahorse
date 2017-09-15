/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function UserInteractionControls() {
  return {
    restrict: 'E',
    replace: true,
    scope: {},
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls.html'
  };
}

exports.inject = function (module) {
  module.directive('userInteractionControls', UserInteractionControls);
};
