'use strict';

/* @ngInject */
function NavigationController($state, config, WorkflowService) {
  _.assign(this, {
      goToHomeView() {
        $state.go('home', {}, {reload: true});
      },
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
