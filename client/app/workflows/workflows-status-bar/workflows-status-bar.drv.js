'use strict';

/* @ngInject */
function WorkflowEditorStatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/workflows-editor-status-bar.html',
    replace: true,
    scope: {},
    controllerAs: 'controller',
    controller: 'WorkflowStatusBarController'
  };
}

/* @ngInject */
function WorkflowReportStatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/workflows-report-status-bar.html',
    replace: true,
    scope: {},
    controllerAs: 'controller',
    controller: 'WorkflowStatusBarController'
  };
}

exports.inject = function(module) {
  module.directive('workflowEditorStatusBar', WorkflowEditorStatusBar);
  module.directive('workflowReportStatusBar', WorkflowReportStatusBar);
};
