/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import startEditingHTML from './additional-html/starting-popover.html';
import startExecutorHTML from './additional-html/running-executor-popover.html';
import executorErrorHTML from './additional-html/executor-error.html';

import {sessionStatus} from 'APP/enums/session-status.js';

/* ngInject */
function WorkflowStatusBarService($rootScope, WorkflowService, UserService) {

  const service = this;

  service.getMenuItems = getMenuItems;

  const isOwner = () => WorkflowService.getCurrentWorkflow().owner.id === UserService.getSeahorseUser().id;

  const menuItems = {
    clone: {
      label: 'Clone',
      callFunction: () => $rootScope.$broadcast('StatusBar.CLONE_WORKFLOW')
    },
    export: {
      label: 'Export',
      callFunction: () => $rootScope.$broadcast('StatusBar.EXPORT_CLICK')
    },
    run: {
      label: 'Run',
      forOwnerOnly: true,
      callFunction: () => $rootScope.$broadcast('StatusBar.RUN')
    },
    startEditing: {
      label: 'Start editing',
      forOwnerOnly: true,
      icon: 'fa-play',
      callFunction: () => $rootScope.$emit('StatusBar.START_EDITING'),
      additionalHtmlForOwner: startEditingHTML
    },
    startingEditing: {
      label: 'Starting...',
      icon: 'fa-cog',
      additionalClass: 'menu-item-disabled',
      additionalIconClass: 'fa-spin',
      additionalHtmlForOwner: startExecutorHTML
    },
    executorError: {
      label: 'Executor error',
      icon: 'fa-ban',
      additionalClass: 'disabled',
      additionalHtmlForOwner: executorErrorHTML
    },
    stopEditing: {
      label: 'Stop editing',
      icon: 'fa-stop',
      callFunction: () => $rootScope.$emit('StatusBar.STOP_EDITING')
    },
    abort: {
      label: 'Abort',
      callFunction: () => $rootScope.$broadcast('StatusBar.ABORT')
    },
    aborting: {
      label: 'Aborting...',
      additionalClass: 'menu-item-disabled'
    },
    closeInnerWorkflow: {
      label: 'Close inner workflow',
      icon: 'fa-ban',
      callFunction: () => $rootScope.$broadcast('StatusBar.CLOSE-INNER-WORKFLOW')
    }
  };

  menuItems.disabledClone = angular.copy(menuItems.clone);
  menuItems.disabledClone.additionalClass = 'menu-item-disabled';

  menuItems.disabledStartEditing = angular.copy(menuItems.startEditing);
  menuItems.disabledStartEditing.additionalClass = 'menu-item-disabled';

  menuItems.disabledStopEditing = angular.copy(menuItems.stopEditing);
  menuItems.disabledStopEditing.additionalClass = 'menu-item-disabled';

  menuItems.disabledExport = angular.copy(menuItems.export);
  menuItems.disabledExport.additionalClass = 'menu-item-disabled';

  menuItems.disabledRun = angular.copy(menuItems.run);
  menuItems.disabledRun.additionalClass = 'menu-item-disabled';

  const _menuItemViews = {
    editorExecutorRunning: [menuItems.stopEditing, menuItems.clone, menuItems.run, menuItems.export],
    editorExecutorCreating: [menuItems.startingEditing, menuItems.clone, menuItems.disabledRun, menuItems.export],
    editorExecutorNotRunning: [menuItems.startEditing, menuItems.clone, menuItems.disabledRun, menuItems.export],
    editorExecutorError: [menuItems.executorError, menuItems.clone, menuItems.disabledRun, menuItems.export],
    editorReadOnlyForNotOwner: [menuItems.disabledStartEditing, menuItems.clone, menuItems.disabledRun, menuItems.export],
    running: [menuItems.disabledStopEditing, menuItems.clone, menuItems.abort, menuItems.export],
    aborting: [menuItems.disabledStopEditing, menuItems.disabledClone, menuItems.aborting, menuItems.disabledExport],
    editInnerWorkflow: [menuItems.closeInnerWorkflow]
  };

  function getMenuItems(workflow) {
    let view = _getView(workflow);
    return _menuItemViews[view];
  }

  function _getView(workflow) {
    // TODO Refactor this code.
    switch (workflow.workflowType) {
      case 'root':
        if (!isOwner()) {
          return 'editorReadOnlyForNotOwner';
        }
        switch (workflow.workflowStatus) {
          case 'editor':
            switch (workflow.sessionStatus) {
              case sessionStatus.NOT_RUNNING:
                return 'editorExecutorNotRunning';
              case sessionStatus.CREATING:
                return 'editorExecutorCreating';
              case sessionStatus.RUNNING:
                return 'editorExecutorRunning';
              case sessionStatus.ERROR:
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
      // no default
    }
  }

  return service;
}

exports.inject = function (module) {
  module.service('WorkflowStatusBarService', WorkflowStatusBarService);
};
