'use strict';

/* @ngInject */
function PresetModalCtrl($uibModalInstance, PresetService, PresetModalLabels, preset, type, isSnapshot) {
  const vm = this;

  vm.labels = getLabelsForType(type);
  vm.preset = angular.copy(preset) || {isEditable: true};
  vm.isSnapshot = isSnapshot;

  vm.isPresetNameUsed = isPresetNameUsed;
  vm.isUriInputInvalid = isUriInputInvalid;
  vm.isNameInputInvalid = isNameInputInvalid;
  vm.ok = ok;
  vm.cancel = cancel;
  vm.isEditingEnabled = isEditingEnabled;

  function isPresetNameUsed() {
    return vm.presetForm.presetName.$dirty && PresetService.isNameUsed(vm.presetForm.presetName.$viewValue);
  }

  function isUriInputInvalid() {
    return vm.presetForm.uri.$dirty && vm.presetForm.uri.$invalid;
  }

  function isNameInputInvalid() {
    return vm.presetForm.presetName.$dirty && vm.presetForm.presetName.$invalid;
  }

  function ok() {
    vm.preset.clusterType = type;
    if (!PresetService.isValid(vm.preset)) {
      vm.errors = formatErrors(PresetService.getErrors());
    } else {
      PresetService.savePreset(vm.preset);
      $uibModalInstance.close();
    }
  }

  function cancel() {
    $uibModalInstance.dismiss();
  }

  function getLabelsForType(type) {
    return PresetModalLabels[type];
  }

  function isEditingEnabled() {
    return vm.preset.isEditable && !vm.isSnapshot;
  }

  function formatErrors(errors) {
    let errorObject = {};
    errors.forEach((error) => {
      errorObject[error.path] = error.message;
    });
    return errorObject;
  }

}

exports.inject = function (module) {
  module.controller('PresetModalCtrl', PresetModalCtrl);
};
