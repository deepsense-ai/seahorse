'use strict';

/* @ngInject */
function ChooseClusterModalCtrl($scope, $uibModalInstance, ClusterService, ClusterModalService, PresetService, WorkflowService, ConfirmationModalService) {
  const vm = this;

  vm.presets = getPresetList();
  vm.isSnapshot = false;

  vm.openClusterSettingsModal = openClusterSettingsModal;
  vm.deletePresetById = deletePresetById;
  vm.isPresetAssignedToWorkflow = isPresetAssignedToWorkflow;
  vm.selectCurrentPreset = selectCurrentPreset;
  vm.formatPresetType = formatPresetType;
  vm.ok = ok;
  vm.close = close;

  function openClusterSettingsModal(type, preset) {
    ClusterModalService.openCurrentClusterModal(type, preset, vm.isSnapshot);
  }

  function deletePresetById(preset) {
    ConfirmationModalService.showModal({
      message: `The operation will delete preset ${preset.name}. Deletion cannot be undone afterwards.`
    }).then(() => {
      PresetService.deletePreset(preset.id);
    });
  }

  function isPresetAssignedToWorkflow(presetId) {
    const workflowId = WorkflowService.getCurrentWorkflow().id;
    const currentPreset = ClusterService.getPresetByWorkflowId(workflowId);
    return presetId === currentPreset.id;
  }

  function selectCurrentPreset(presetId) {
    const isExecutorBusy = WorkflowService.isExecutorForCurrentWorkflowRunning();
    if (!isExecutorBusy) {
      const workflowId = WorkflowService.getCurrentWorkflow().id;
      ClusterService.bindPresetToWorkflow(workflowId, presetId);
    }
  }

  function getPresetList() {
    return PresetService.getAll();
  }

  function formatPresetType(type) {
    return ClusterModalService.formatPresetType(type);
  }

  function close() {
    $uibModalInstance.dismiss();
  }

  function ok() {
    $uibModalInstance.close();
  }

  $scope.$watch(getPresetList, (presets) => {
    vm.presets = presets;
  }, true);

}

exports.inject = function (module) {
  module.controller('ChooseClusterModalCtrl', ChooseClusterModalCtrl);
};
