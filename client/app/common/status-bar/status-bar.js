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
    controllerAs: 'controller',
    controller: function () {
      _.assign(this, {
        backToHome: () => {
          $rootScope.$broadcast('StatusBar.HOME_CLICK');
        },
        exportWorkflow: () => {
          $rootScope.$broadcast('StatusBar.EXPORT_CLICK');
        },
        saveWorkflow: () => {
          $rootScope.$broadcast('StatusBar.SAVE_CLICK');
        },
        clearWorkflow: () => {
          $rootScope.$broadcast('StatusBar.CLEAR_CLICK');
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('workflowEditorStatusBar', WorkflowEditorStatusBar);
};
