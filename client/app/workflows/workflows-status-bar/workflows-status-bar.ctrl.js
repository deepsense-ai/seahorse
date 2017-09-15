'use strict';

import {sessionStatus} from 'APP/enums/session-status.js';

/* @ngInject */
function WorkflowStatusBarController($scope, UserService, ClusterModalService, DatasourcesPanelService,
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
