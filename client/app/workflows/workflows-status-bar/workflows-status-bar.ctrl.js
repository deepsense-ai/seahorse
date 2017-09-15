'use strict';

/* @ngInject */
function WorkflowStatusBarController($rootScope, additionalControls) {
  _.assign(this, {
    backToHome () {
      $rootScope.$broadcast('StatusBar.HOME_CLICK');
    },
    exportWorkflow () {
      $rootScope.$broadcast('StatusBar.EXPORT_CLICK');
    },
    saveWorkflow () {
      $rootScope.$broadcast('StatusBar.SAVE_CLICK');
    },
    clearWorkflow () {
      $rootScope.$broadcast('StatusBar.CLEAR_CLICK');
    },
    lastExecutedReport () {
      $rootScope.$broadcast('StatusBar.LAST_EXECUTION_REPORT');
    }
  });

  if (additionalControls) {
    _.assign(this, {
      run () {
        $rootScope.$broadcast('StatusBar.RUN');
      }
    });
  }
}

exports.inject = function (module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
