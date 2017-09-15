'use strict';

/* @ngInject */
function WorkflowStatusBarController($rootScope, $stateParams, WorkflowService,
                                     additionalControls, WorkflowsApiClient) {
  _.assign(this, {
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
    },
    getReportName () {
      let name = WorkflowService.getWorkflow().name;
      return name.toLowerCase().split(' ').join('-');
    },
    exportReportLink: WorkflowsApiClient
      .getDownloadReportUrl($stateParams.reportId)
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
