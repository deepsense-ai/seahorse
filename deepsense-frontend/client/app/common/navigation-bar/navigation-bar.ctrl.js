'use strict';

/* @ngInject */
function NavigationController($rootScope, config, PageService, WorkflowService) {
  _.assign(this, {
    home() {
        $rootScope.$broadcast('StatusBar.HOME_CLICK');
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
