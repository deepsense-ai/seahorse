'use strict';

/* @ngInject */
function WorkflowStatusBarController($rootScope, $stateParams, WorkflowService,
  WorkflowsApiClient, WorkflowStatusBarService, TimeService, config) {

  this.data = WorkflowStatusBarService.data;

  _.assign(this, {
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

      exportReportLink: WorkflowsApiClient.getDownloadReportUrl($stateParams.reportId)
  });
}

exports.inject = function(module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
