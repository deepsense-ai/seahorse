'use strict';

class WorkflowStatusBarService {
  constructor($rootScope, config, version) {

    this.$rootScope = $rootScope;
    this.config = config;
    this.version = version;

    let menuItems = {
      clear: {
        label: 'Clear',
        icon: 'fa-trash-o',
        callFunction: () => $rootScope.$broadcast('StatusBar.CLEAR_CLICK')
      },
      documentation: {
        label: 'Documentation',
        icon: 'fa-book',
        href: this.config.docsHost + '/docs/' + this.version.getDocsVersion() + '/index.html',
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
      aborting: {
        label: 'Aborting...',
        icon: 'fa-ban',
        color: '#216477',
        additionalClass: 'disabled'
      },
      closeInnerWorkflow: {
        label: 'Close inner workflow',
        icon: 'fa-ban',
        color: '#216477',
        callFunction: () => this.$rootScope.$broadcast('StatusBar.CLOSE-INNER-WORKFLOW')
      }
    };

    menuItems.disabledClear = angular.copy(menuItems.clear);
    menuItems.disabledClear.additionalClass = 'disabled';

    menuItems.disabledExport = angular.copy(menuItems.export);
    menuItems.disabledExport.additionalClass = 'disabled';

    this._menuItemViews = {
      editor: [menuItems.clear, menuItems.export, menuItems.documentation, menuItems.run],
      running: [menuItems.disabledClear, menuItems.disabledExport, menuItems.documentation, menuItems.abort],
      aborting: [menuItems.disabledClear, menuItems.disabledExport, menuItems.documentation, menuItems.aborting],
      editInnerWorkflow: [menuItems.documentation, menuItems.closeInnerWorkflow]
    };
  }

  getMenuItems(workflowType, workflowStatus) {
    let view = this._getView(workflowType, workflowStatus);
    return this._menuItemViews[view];
  }

  _getView(workflowType, workflowStatus) {
    switch (workflowType) {
      case 'root':
        return workflowStatus;
      case 'inner':
        if (workflowStatus === 'editor') {
          return 'editInnerWorkflow';
        } else {
          throw 'Cannot run inner workflow';
        }
    }
  }

}

exports.inject = function(module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
