'use strict';

/* @ngInject */
function LastExecutionReportService($timeout, $rootScope, config, WorkflowService, WorkflowsApiClient) {
  let internal = {
    timeoutPromise: null
  };

  function setLastExecutionReportTime(workflow, lastExecutionReportTime) {
    if (_.isNull(workflow.lastExecutionReportTime)) {
      $rootScope.$broadcast('LastExecutionReportService.REPORT_HAS_BEEN_UPLOADED');
    }
    workflow.lastExecutionReportTime = lastExecutionReportTime;
  }

  function requestForLastExecutionReportTime() {
    let workflowId = WorkflowService.getWorkflow().id;

    WorkflowsApiClient.getResultsUploadTime(workflowId).
      then((data) => {
        let workflow = WorkflowService.getWorkflow();
        if (workflow.id === workflowId) {
          setLastExecutionReportTime(workflow, data.resultsUploadTime);
        }
      }).
      catch(() => {
        let workflow = WorkflowService.getWorkflow();
        if (workflow.id === workflowId) {
          workflow.lastExecutionReportTime = null;
        }
      }).
      then(() => {
        internal.timeoutPromise = $timeout(requestForLastExecutionReportTime, config.resultsRefreshInterval);
      });
  }

  _.assign(this, {
    setTimeout: () => {
      requestForLastExecutionReportTime();
    },
    clearTimeout: () => {
      $timeout.cancel(internal.timeoutPromise);
    }
  });
}

exports.inject = function (module) {
  module.service('LastExecutionReportService', LastExecutionReportService);
};
