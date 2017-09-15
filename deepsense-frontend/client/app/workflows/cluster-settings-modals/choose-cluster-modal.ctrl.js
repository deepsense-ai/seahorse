'use strict';

const COOKIE_NAME = 'SEAHORSE_DELETE_PRESET_CONFIRMATION';

/* @ngInject */
function ChooseClusterModalCtrl($scope, $uibModalInstance, $log, DeleteModalService, ClusterModalService,
                                PresetService, WorkflowService) {
  const vm = this;

  vm.presets = getPresetList();
  vm.isSnapshot = false;
  vm.error = '';

  vm.openClusterSettingsModal = openClusterSettingsModal;
  vm.deletePresetById = deletePresetById;
  vm.isPresetAssignedToWorkflow = isPresetAssignedToWorkflow;
  vm.selectCurrentPreset = selectCurrentPreset;
  vm.formatPresetType = formatPresetType;
  vm.ok = ok;
  vm.close = close;

  $scope.$watch(getPresetList, (presets) => {
    vm.presets = presets;
  }, true);

  PresetService.fetch().catch((error) => {
    vm.presets = [];
    vm.error = 'There was a problem with downloading presets';
    $log.error('PresetsService fetch failed', error);
  });


  function openClusterSettingsModal(type, preset) {
    ClusterModalService.openCurrentClusterModal(type, preset, vm.isSnapshot);
  }

  function deletePresetById(preset) {
    DeleteModalService.handleDelete(() => PresetService.deletePreset(preset.id), COOKIE_NAME);
  }

  function isPresetAssignedToWorkflow(presetId) {
    const currentPreset = WorkflowService.getRootWorkflow().cluster;
    return presetId === currentPreset.id;
  }

  function selectCurrentPreset(presetId) {
    if (!WorkflowService.isExecutorForCurrentWorkflowRunning()) {
      WorkflowService.bindPresetToCurrentWorkflow(presetId);
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

}

exports.inject = function (module) {
  module.controller('ChooseClusterModalCtrl', ChooseClusterModalCtrl);
};
