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
