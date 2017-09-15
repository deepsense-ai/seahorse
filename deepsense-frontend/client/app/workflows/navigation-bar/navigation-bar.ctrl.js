'use strict';

/* @ngInject */
function NavigationController(config, WorkflowService) {
  _.assign(this, {
      getAPIVersion() {
        return config.editorVersion;
      },
      getCurrentWorkflow() {
        return WorkflowService.getCurrentWorkflow();
      }
  });
}

exports.inject = function(module) {
  module.controller('NavigationController', NavigationController);
};
