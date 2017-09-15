'use strict';

/* @ngInject */
function WorkflowEditorStatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/workflows-editor-status-bar.html',
    replace: true,
    scope: {
      workflowType: '=',
      isRunning: '='
    },
    controllerAs: 'controller',
    controller: 'WorkflowStatusBarController'
  };
}

exports.inject = function(module) {
  module.directive('workflowEditorStatusBar', WorkflowEditorStatusBar);
};
