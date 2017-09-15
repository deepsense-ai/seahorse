'use strict';

const SCHEMA = require('./preset.schema.json');
const jsonSchema = require('jsen');

/* @ngInject */
function PresetService(PresetsApiService, WorkflowService) {
  const validate = jsonSchema(SCHEMA);
  const vm = this;

  vm.fetch = fetch;
  vm.getAll = getAll;
  vm.createPreset = createPreset;
  vm.deletePreset = deletePreset;
  vm.updatePreset = updatePreset;
  vm.savePreset = savePreset;
  vm.isNameUsed = isNameUsed;
  vm.isValid = isValid;
  vm.getErrors = getErrors;

  let presets;

  fetch();

  /**
   * @returns {Promise}
   */
  function fetch() {
    return PresetsApiService.getAll()
      .then((result) => {
        presets = result;
        return result;
      })
      .then(() => WorkflowService.fetchCluster(WorkflowService.getRootWorkflow()));
  }

  /**
   * @return {Array|undefined}
   */
  function getAll() {
    return presets;
  }

  /**
   * @param {Object} presetCandidate
   * @return {Promise}
   */
  function createPreset(presetCandidate) {
    return PresetsApiService.create(presetCandidate)
      .then(fetch);

  }

  /**
   * @param {Number} id
   * @return {Promise}
   */
  function deletePreset(id) {
    return PresetsApiService.remove(id)
      .then(fetch);
  }

  /**
   * @param {Object} presetCandidate
   * @return {Promise}
   */
  function updatePreset(presetCandidate) {
    return PresetsApiService.update(presetCandidate.id, presetCandidate)
      .then(fetch);
  }

  /**
   * @param {Object} presetCandidate
   * @return {Promise}
   */
  function savePreset(presetCandidate) {
    return presetCandidate.id ? updatePreset(presetCandidate) : createPreset(presetCandidate);
  }

  /**
   * @param {Object} preset
   * @returns {Boolean}
   */
  function isValid(preset) {
    return validate(preset);
  }

  /**
   * Gets the errors from previous validation call.
   * To be used only after (isValid, createPreset or updatePreset) because validate.errors is stateful
   * and stores information only about previous validate() call.
   * @returns {Array|undefined}
   */
  function getErrors() {
    return validate.errors;
  }

  /**
   * @param {String} name
   * @returns {Boolean}
   */
  function isNameUsed(name) {
    if (presets) {
      return Object.keys(presets).filter((key) => {
          return presets[key].name.toLowerCase() === name.toLowerCase();
        }).length > 0;
    } else {
      return false;
    }
  }
}

exports.inject = function(module) {
  module.service('PresetService', PresetService);
};
