'use strict';

class WorkflowStatusBarService {
  constructor($rootScope, config) {

    this.$rootScope = $rootScope;
    this.config = config;

    let menuItems = {
      clear: {
        label: 'Clear',
        icon: 'fa-trash-o',
        callFunction: () => $rootScope.$broadcast('StatusBar.CLEAR_CLICK')
      },
      documentation: {
        label: 'Documentation',
        icon: 'fa-book',
        href: this.config.docsHost + '/docs/latest/index.html',
        target: '_blank'
      },
      export: {
        label: 'Export',
        icon: 'fa-angle-double-down',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.EXPORT_CLICK')
      },
      run: {
        label: 'Run',
        icon: 'fa-play',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.RUN')
      },
      abort: {
        label: 'Abort',
        icon: 'fa-ban',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.ABORT')
      },
      closeInnerWorkflow: {
        label: 'Close inner workflow',
        icon: 'fa-ban',
        color: '#216477',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.CLOSE-INNER-WORKFLOW')
      }
    };

    this._menuItemViews = {
      edit: [menuItems.clear, menuItems.documentation, menuItems.export, menuItems.run],
      running: [menuItems.clear, menuItems.documentation, menuItems.export, menuItems.abort],
      editInnerWorkflow: [menuItems.documentation, menuItems.closeInnerWorkflow]
    };
  }

  getMenuItems(workflowType, isRunning) {
    let view = this._getView(workflowType, isRunning);
    return this._menuItemViews[view];
  }

  _getView(workflowType, isRunning) {
    switch (workflowType) {
      case 'root':
        let view;
        if (isRunning) {
          view = 'running';
        } else {
          view = 'edit';
        }
        return view;
      case 'inner':
        if (isRunning) {
          throw 'Cannot run inner workflow';
        } else {
          return 'editInnerWorkflow';
        }
    }
  }

}

exports.inject = function(module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
