'use strict';

/* @ngInject */
function WorkflowStatusBarController($scope, UserService, ClusterModalService, ClusterService,
                                     SessionStatus, SessionManager, WorkflowService, WorkflowStatusBarService) {

  const vm = this;

  vm.workflow = WorkflowService.getCurrentWorkflow();
  vm.workflowId = vm.workflow.id;
  vm.currentPreset = getCurrentPreset();

  vm.getCurrentPreset = getCurrentPreset;
  vm.formatPresetType = formatPresetType;
  vm.openCurrentPresetModal = openCurrentPresetModal;
  vm.openClusterSettings = openClusterSettings;
  vm.getMenuItems = getMenuItems;
  vm.getCurrentUser = getCurrentUser;
  vm.isOwner = isOwner;
  vm.isViewerMode = isViewerMode;

  function getCurrentPreset() {
    return WorkflowService.isExecutorForCurrentWorkflowRunning() ?
      SessionManager.clusterInfoForWorkflowId(vm.workflowId)
      : ClusterService.getPresetByWorkflowId(vm.workflowId);
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
    return vm.workflow.sessionStatus === SessionStatus.NOT_RUNNING;
  }

  $scope.$watch(getCurrentPreset, (newValue, oldValue) => {
    if (newValue && newValue !== oldValue) {
      vm.currentPreset = newValue;
    }
  });

}

exports.inject = function (module) {
  module.controller('WorkflowStatusBarController', WorkflowStatusBarController);
};
