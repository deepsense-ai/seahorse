'use strict';

class WorkflowStatusBarService {
  constructor($rootScope, $stateParams, config) {

    this.$rootScope = $rootScope;
    this.$stateParams = $stateParams;
    this.config = config;

    this.data = {
      menuItems: [{
        label: 'Clear',
        icon: 'fa-trash-o',
        callFunction: () => $rootScope.$broadcast('StatusBar.CLEAR_CLICK')
      }, {
        label: 'Documentation',
        icon: 'fa-book',
        href: this.config.docsHost + '/docs/latest/index.html',
        target: '_blank'
      }, {
        label: 'Export',
        icon: 'fa-angle-double-down',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.EXPORT_CLICK')
      }, {
        label: 'Run',
        icon: 'fa-play',
        callFunction: this.executionRun.bind(this)
      }]
    };
  }

  executionRun() {
    this.createAbortButton();
    this.$rootScope.$broadcast('StatusBar.RUN');
  }

  executionAbort() {
    this.createRunButton();
    this.$rootScope.$broadcast('StatusBar.ABORT');
  }

  createAbortButton() {
    let abortButton = {
      label: 'Abort',
      icon: 'fa-ban',
      callFunction: this.executionAbort.bind(this)
    };
    this.data.menuItems.pop();
    this.data.menuItems.push(abortButton);
  }

  createRunButton() {
    let runButton = {
      label: 'Run',
      icon: 'fa-play',
      callFunction: this.executionRun.bind(this)
    };
    this.data.menuItems.pop();
    this.data.menuItems.push(runButton);
  }

}

exports.inject = function(module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
