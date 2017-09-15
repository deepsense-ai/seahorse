'use strict';

class WorkflowStatusBarService {
  constructor($rootScope, $stateParams, config) {
    this.data = {
      menuItems: [{
        label: 'Clear',
        icon: 'fa-trash-o',
        callFunction: () => $rootScope.$broadcast('StatusBar.CLEAR_CLICK')
      }, {
        label: 'Documentation',
        icon: 'fa-book',
        href: config.docsHost + '/docs/latest/index.html',
        target: '_blank'
      }, {
        label: 'Notebook',
        icon: 'fa-terminal',
        href: config.docsHost + '/notebooks/' + $stateParams.id + '.ipynb',
        target: '_blank'
      }, {
        label: 'Export',
        icon: 'fa-angle-double-down',
        callFunction: () => $rootScope.$broadcast('StatusBar.EXPORT_CLICK')
      }, {
        label: 'Run',
        icon: 'fa-play',
        callFunction: () => $rootScope.$broadcast('StatusBar.RUN')
      }]
    };
  }
}

exports.inject = function(module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
