/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function WorkflowEditorStatusBar($rootScope) {
  return {
    restrict: 'E',
    templateUrl: 'app/common/status-bar/status-bar.html',
    replace: true,
    scope: {},
    link: function (scope) {
      scope.exportWorkflow = function exportWorkflow () {
        $rootScope.$broadcast('Workflow.EXPORT');
      };

      scope.saveWorkflow = function saveWorkflow () {
        $rootScope.$broadcast('StatusBar.SAVE_CLICK');
      };

      scope.clearWorkflow = function clearWorkflow () {
        $rootScope.$broadcast('StatusBar.CLEAR_CLICK');
      };
    }
  };
}

exports.inject = function (module) {
  module.directive('workflowEditorStatusBar', WorkflowEditorStatusBar);
};
