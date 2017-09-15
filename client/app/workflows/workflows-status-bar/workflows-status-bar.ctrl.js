'use strict';

/* @ngInject */
function WorkflowStatusBarController($stateParams, WorkflowService,
  WorkflowsApiClient, WorkflowStatusBarService, config) {

  this.data = WorkflowStatusBarService.data;

  _.assign(this, {
    getReportName() {
        let name = WorkflowService.getMainWorkflow()
          .name;
        return name.toLowerCase()
          .split(' ')
          .join('-');
      },

      getDocsHost() {
        return config.docsHost;
      },

      exportReportLink: WorkflowsApiClient.getDownloadReportUrl($stateParams.reportId)
  });
}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
