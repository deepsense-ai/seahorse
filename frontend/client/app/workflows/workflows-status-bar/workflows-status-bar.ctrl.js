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

import {sessionStatus} from 'APP/enums/session-status.js';

/* @ngInject */
function WorkflowStatusBarController($rootScope, $scope, $log, UserService, ClusterModalService, DatasourcesPanelService,
                                     SessionManager, WorkflowService, WorkflowStatusBarService, LibraryService, PredefinedUser) {

  const vm = this;

  vm.workflow = WorkflowService.getCurrentWorkflow();
  vm.workflowId = vm.workflow.id;
  vm.rootId = vm.workflow.id;
  vm.currentPreset = getCurrentPreset();
  vm.uploadingFiles = [];

  vm.progressValue = 100;

  vm.getCurrentPreset = getCurrentPreset;
  vm.formatPresetType = formatPresetType;
  vm.openCurrentPresetModal = openCurrentPresetModal;
  vm.openClusterSettings = openClusterSettings;
  vm.getMenuItems = getMenuItems;
  vm.getCurrentUser = getCurrentUser;
  vm.isOwner = isOwner;
  vm.isViewerMode = isViewerMode;
  vm.openDatasources = openDatasources;
  vm.predefinedUserId = PredefinedUser.id;
  vm.sparkUiAddress = undefined;
  vm.isSparkUiAvailable = isSparkUiAvailable;
  vm.sparkUiAdditionalClasses = getSparkUiAdditionalClasses();

  $rootScope.$on('ServerCommunication.MESSAGE.heartbeat', (event, data) => {
       if (data.sparkUiAddress !== null) {
        vm.sparkUiAddress = data.sparkUiAddress;
       } else {
        vm.sparkUiAddress = undefined;
       }
  });

  $scope.$watch(getCurrentPreset, (newValue, oldValue) => {
    if (newValue && newValue !== oldValue) {
      vm.currentPreset = newValue;
    }
  });

  $scope.$watch(LibraryService.getUploadingFiles, (newValue) => {
    vm.isUploadInProgress = newValue.filter((value) => value.status === 'uploading').length > 0;
  }, true);

  $scope.$watch(() => WorkflowService.getCurrentWorkflow(), (newValue) => {
    vm.workflow = newValue;
    vm.workflowId = newValue.id;
  });

  watchForSparkUiAdditionalClassesChanges(() => vm.sparkUiAddress);
  watchForSparkUiAdditionalClassesChanges(() => vm.workflow.sessionStatus);

  function watchForSparkUiAdditionalClassesChanges(variableToWatch) {
      $scope.$watch(variableToWatch, (newValue, oldValue) => {
        if (oldValue !== newValue) {
          vm.sparkUiAdditionalClasses = getSparkUiAdditionalClasses();
        }
      });
  }

  function getSparkUiAdditionalClasses() {
    return isSparkUiAvailable() ? '' : 'menu-item-disabled';
  }

  function getCurrentPreset() {
    return WorkflowService.isExecutorForCurrentWorkflowRunning() ?
      SessionManager.clusterInfoForWorkflowId(vm.rootId) :
      WorkflowService.getRootWorkflow().cluster;
  }

  function formatPresetType(type) {
    return ClusterModalService.formatPresetType(type);
  }

  function openCurrentPresetModal(preset) {
    const {clusterType} = preset;
    const isSnapshot = WorkflowService.isExecutorForCurrentWorkflowRunning();
    ClusterModalService.openCurrentClusterModal(clusterType, preset, isSnapshot);
  }

  function openClusterSettings() {
    ClusterModalService.openClusterSelectionModal();
  }

  function getMenuItems(workflow) {
    return WorkflowStatusBarService.getMenuItems(workflow);
  }

  function getCurrentUser() {
    return UserService.getSeahorseUser();
  }

  function isOwner() {
    return vm.workflow.owner.id === UserService.getSeahorseUser().id;
  }

  function isSparkUiAvailable() {
    return vm.workflow.sessionStatus === sessionStatus.RUNNING && vm.sparkUiAddress !== undefined;
  }

  function isViewerMode() {
    return vm.workflow.sessionStatus === sessionStatus.NOT_RUNNING;
  }

  function openDatasources() {
    DatasourcesPanelService.openDatasourcesForBrowsing();
  }
}

exports.inject = function (module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
