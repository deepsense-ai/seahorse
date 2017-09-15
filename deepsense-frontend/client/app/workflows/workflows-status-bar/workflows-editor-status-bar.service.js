'use strict';

/* ngInject */
function WorkflowStatusBarService($rootScope, config, version, SessionStatus) {

  const service = this;

  service.getMenuItems = getMenuItems;

  const menuItems = {
    clear: {
      label: 'Clear',
      icon: 'fa-trash-o',
      callFunction: () => $rootScope.$broadcast('StatusBar.CLEAR_CLICK')
    },
    documentation: {
      label: 'Documentation',
      icon: 'fa-book',
      href: config.docsHost + '/docs/' + version.getDocsVersion() + '/index.html',
      target: '_blank'
    },
    export: {
      label: 'Export',
      icon: 'fa-angle-double-down',
      callFunction: () => $rootScope.$broadcast('StatusBar.EXPORT_CLICK')
    },
    run: {
      label: 'Run',
      icon: 'fa-play',
      callFunction: () => $rootScope.$broadcast('StatusBar.RUN')
    },
    startExecutor: {
      label: 'Start executor',
      icon: 'fa-play',
      callFunction: () => $rootScope.$emit('StatusBar.START_EXECUTOR')
    },
    startingExecutor: {
      label: 'Start executor...',
      icon: 'fa-cog',
      additionalClass: 'disabled',
      additionalIconClass: 'fa-spin'
    },
    executorError: {
      label: 'Executor error',
      icon: 'fa-ban',
      color: '#BF2828',
      additionalClass: 'disabled'
    },
    stopExecutor: {
      label: 'Stop executor',
      icon: 'fa-ban',
      callFunction: () => $rootScope.$emit('StatusBar.STOP_EXECUTOR')
    },
    abort: {
      label: 'Abort',
      icon: 'fa-ban',
      callFunction: () => $rootScope.$broadcast('StatusBar.ABORT')
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
      callFunction: () => $rootScope.$broadcast('StatusBar.CLOSE-INNER-WORKFLOW')
    }
  };

  menuItems.disabledClear = angular.copy(menuItems.clear);
  menuItems.disabledClear.additionalClass = 'disabled';

  menuItems.disabledExport = angular.copy(menuItems.export);
  menuItems.disabledExport.additionalClass = 'disabled';

  menuItems.disabledRun = angular.copy(menuItems.run);
  menuItems.disabledRun.additionalClass = 'disabled';

  const _menuItemViews = {
    editorExecutorRunning: [menuItems.clear, menuItems.export, menuItems.documentation, menuItems.stopExecutor, menuItems.run],
    editorExecutorCreating: [menuItems.disabledClear, menuItems.export, menuItems.documentation, menuItems.startingExecutor, menuItems.disabledRun],
    editorExecutorNotRunning: [menuItems.disabledClear, menuItems.export, menuItems.documentation, menuItems.startExecutor, menuItems.disabledRun],
    editorExecutorError: [menuItems.disabledClear, menuItems.export, menuItems.documentation, menuItems.executorError, menuItems.disabledRun],
    running: [menuItems.disabledClear, menuItems.disabledExport, menuItems.documentation, menuItems.abort],
    aborting: [menuItems.disabledClear, menuItems.disabledExport, menuItems.documentation, menuItems.aborting],
    editInnerWorkflow: [menuItems.documentation, menuItems.closeInnerWorkflow]
  };

  function getMenuItems(workflow) {
    let view = _getView(workflow);
    return _menuItemViews[view];
  }

  function _getView(workflow) {
    // TODO Refactor this code.
    switch (workflow.workflowType) {
      case 'root':
        switch (workflow.workflowStatus) {
          case 'editor':
            switch (workflow.sessionStatus) {
              case SessionStatus.NOT_RUNNING:
                return 'editorExecutorNotRunning';
              case SessionStatus.CREATING:
                return 'editorExecutorCreating';
              case SessionStatus.RUNNING:
                return 'editorExecutorRunning';
              case SessionStatus.ERROR:
                return 'editorExecutorError';
              default:
                throw `Unsupported session status: ${workflow.sessionStatus}`;
            }
          case 'aborting':
          case 'running':
            return workflow.workflowStatus;
          default:
            throw `Unsupported workflow status: ${workflow.workflowStatus}`;
        }
      case 'inner':
        if (workflow.workflowStatus === 'editor') {
          return 'editInnerWorkflow';
        } else {
          throw 'Cannot run inner workflow';
        }
    }
  }

  return service;
}

exports.inject = function (module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
