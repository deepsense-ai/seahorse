'use strict';

const PRESETS_ROOT = 'presets';
const SCHEMA = require('./preset.schema.json');
const jsonSchema = require('jsen');
const validate = jsonSchema(SCHEMA);
const DEFAULT_PRESET = {
  'id': '1_local',
  'name': 'default',
  'clusterType': 'local',
  'uri': '',
  'userIP': '',
  'executorMemory': '2',
  'totalExecutorCores': 2,
  'executorCores': 2,
  'numExecutors': 2,
  'params': '--num-executors 2 --verbose',
  'isEditable': false,
  'isDefault': true
};
const VALIDATED_DEFAULT_PRESET = validate.build(DEFAULT_PRESET);

/* @ngInject */
function PresetService($log, StorageService) {
  const vm = this;
  vm.getAll = getAll;
  vm.getPresetById = getPresetById;
  vm.createPreset =  createPreset;
  vm.deletePreset = deletePreset;
  vm.updatePreset = updatePreset;
  vm.savePreset = savePreset;
  vm.isNameUsed = isNameUsed;
  vm.isValid = isValid;
  vm.getErrors = getErrors;
  vm.getDefaultPreset = getDefaultPreset;

  validateStore();

  return vm;

  /**
   * Gets all available presets. Adds default, non-deletable configuration
   * @return {Object}
     */
  function getAll() {
    let storedPresets = StorageService.getRoot(PRESETS_ROOT);
    storedPresets[VALIDATED_DEFAULT_PRESET.id] = VALIDATED_DEFAULT_PRESET;
    return storedPresets;
  }

  /**
   * Gets the preset based on id
   * @param {String} id
   * @return {Object|undefined}
   */
  function getPresetById(id) {
    let all = getAll();
    return all[id];
  }

  /**
   * Validates presetCandidate using predefined schema
   * adds default fields to the preset and
   * if possible, stores it in the StorageService
   * @param {Object} presetCandidate
   * @return {boolean}
     */
  function createPreset(presetCandidate) {
    let id = `${Date.now()}_` + presetCandidate.clusterType;
    let preset, validationResult;

    preset = validate.build(presetCandidate);
    preset.id = id;
    validationResult = isValid(preset);
    if (validationResult) {
      StorageService.set(PRESETS_ROOT, id, preset);
      return true;
    } else {
      $log.warn('Invalid preset provided', validate.errors);
      return false;
    }

  }

  /**
   * Checks if preset is editable, if yes removes it from StorageService
   * @param {String}  id of the preset to be removed
   * @return {boolean}
     */
  function deletePreset(id) {
    let all = getAll();
    let preset = all[id] || {};
    if (preset.isEditable) {
      StorageService.remove(PRESETS_ROOT, id);
      return true;
    } else {
      $log.warn('Trying to delete non-editable preset', preset);
      return false;
    }
  }

  /**
   * Checks if preset is editable and if new candidate is valid.
   * If both checks passed, adds default params and updates StorageService
   * @param presetCandidate
   * @return {boolean}
     */
  function updatePreset(presetCandidate) {
    let all = getAll();
    let previous = all[presetCandidate.id] || {isEditable: true};
    let preset;

    if (isValid(presetCandidate)) {
      if (previous.isEditable) {
        preset = validate.build(presetCandidate);
        StorageService.set(PRESETS_ROOT, preset.id, preset);
        return true;
      } else {
        $log.warn('Trying to update, non-editable preset', presetCandidate);
      }
    } else {
      $log.warn('Trying to update with invalidPreset', presetCandidate);
    }
    return false;
  }

  /**
   * Checks if preset is new one, or already defined and calls create or update accordingly
   * @param {Object} preset
   * @return {boolean}
   */
  function savePreset(presetCandidate) {
    return (presetCandidate.id) ? updatePreset(presetCandidate) : createPreset(presetCandidate);
  }

  /**
   * Checks if preset is a valid one
   * @param {Object} preset
   * @returns {boolean}
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
   * Checks if the name is already defined in the storage
   * @param {String} name
   * @returns {boolean}
     */
  function isNameUsed(name) {
    let allPresets = getAll();
    return Object.keys(allPresets).filter((key) => {
      return allPresets[key].name.toLowerCase() === name.toLowerCase();
    }).length > 0;
  }

  /**
   * Return non-deletable, default, preset
   */
  function getDefaultPreset() {
    return VALIDATED_DEFAULT_PRESET;
  }

  /**
   * Check localStorage presets root integrity and remove invalid schemas
   */
  function validateStore() {
    let allPresets = getAll();
    Object.keys(allPresets).forEach((key) => {
      if (!validate(allPresets[key])) {
        $log.warn('invalid preset detected', allPresets[key], validate.errors);
        deletePreset(allPresets[key].id);
      }
    });
  }
}

exports.inject = function(module) {
  module.service('PresetService', PresetService);
};
