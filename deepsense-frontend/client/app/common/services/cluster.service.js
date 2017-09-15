'use strict';

const WORKFLOW_TO_PRESET_MAPPING_ROOT = 'workflow-preset';

/* @ngInject */
function ClusterService(StorageService, PresetService) {
  const vm = this;
  vm.getPresetConfigObject = getPresetConfigObject;
  vm.getPresetByWorkflowId =  getPresetByWorkflowId;
  vm.bindPresetToWorkflow = bindPresetToWorkflow;

  return vm;

  /**
   * Gets the config object used by the Executor API
   * @param {String} workflowId
   * @return {Object}
     */
  function getPresetConfigObject (workflowId) {
    return {
      workflowId: workflowId,
      cluster: getPresetByWorkflowId(workflowId)
    };
  }

  /**
   * Get the preset object given by workflowId.
   * If no preset found in mapping, will:
   * ---Create default mapping
   * ---Return default mapping
   * @param {String} workflowId
   * @returns {Object|undefined}
     */
  function getPresetByWorkflowId(workflowId) {
    let presetId = StorageService.get(WORKFLOW_TO_PRESET_MAPPING_ROOT, workflowId);
    let preset = PresetService.getPresetById(presetId);
    if (_.isUndefined(preset)) {
      preset = PresetService.getDefaultPreset();
      presetId = preset.id;
      bindPresetToWorkflow(workflowId, presetId);
    }
    return preset;
  }

  /**
   * Map preset to workflow
   * @param {String} workflowId
   * @param {String} presetId
   * @returns {*}
     */
  function bindPresetToWorkflow(workflowId, presetId) {
    return StorageService.set(WORKFLOW_TO_PRESET_MAPPING_ROOT, workflowId, presetId);
  }
}

exports.inject = function(module) {
  module.service('ClusterService', ClusterService);
};
