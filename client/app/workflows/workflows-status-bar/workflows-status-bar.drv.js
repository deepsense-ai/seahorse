'use strict';

import tpl from './workflows-editor-status-bar.html';

/* @ngInject */
function WorkflowEditorStatusBar() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    controllerAs: 'controller',
    controller: 'WorkflowStatusBarController'
  };
}

exports.inject = function(module) {
  module.directive('workflowEditorStatusBar', WorkflowEditorStatusBar);
};
