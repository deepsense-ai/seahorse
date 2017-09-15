'use strict';

/* @ngInject */
function WorkflowStatusBarController($rootScope, $stateParams, WorkflowService,
  additionalControls, WorkflowsApiClient, TimeService, config) {
  _.assign(this, {
    exportWorkflow() {
        $rootScope.$broadcast('StatusBar.EXPORT_CLICK');
      },
      saveWorkflow() {
        $rootScope.$broadcast('StatusBar.SAVE_CLICK');
      },
      clearWorkflow() {
        $rootScope.$broadcast('StatusBar.CLEAR_CLICK');
      },
      lastExecutedReport() {
        if (this.reportHasBeenUploaded()) {
          $rootScope.$broadcast('StatusBar.LAST_EXECUTION_REPORT');
        }
      },
      getReportName() {
        let name = WorkflowService.getWorkflow()
          .name;
        return name.toLowerCase()
          .split(' ')
          .join('-');
      },

      getDocsHost() {
        return config.docsHost;
      },

      exportReportLink: WorkflowsApiClient.getDownloadReportUrl($stateParams.reportId),
      reportHasBeenUploaded: () => !_.isNull(WorkflowService.getWorkflow()
        .lastExecutionReportTime),
      getLastExecutionTooltipMessage() {
        if (this.reportHasBeenUploaded()) {
          let lastExecutionTime = WorkflowService.getWorkflow()
            .lastExecutionReportTime;
          let diffMsg = TimeService.getVerboseDateDiff(lastExecutionTime);
          return `<div class="c-workflows-status-bar__item--last-execution__tooltip--executed">
          The last report has been uploaded
          <span class="c-workflows-status-bar__item--last-execution__tooltip--executed--highlight">${diffMsg}</span>
          ago
        </div>`;
        } else {
          return `No report is available`;
        }
      }
  });

  if (additionalControls) {
    _.assign(this, {
      run() {
        $rootScope.$broadcast('StatusBar.RUN');
      }
    });
  }
}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
