'use strict';

/* @ngInject */
function PresetModalCtrl($uibModalInstance, PresetService, PresetModalLabels, preset, type, isSnapshot) {
  const vm = this;

  vm.labels = getLabelsForType(type);
  vm.preset = angular.copy(preset) || {isEditable: true};
  vm.isSnapshot = isSnapshot;
  vm.focused = undefined;

  vm.isPresetNameUsed = isPresetNameUsed;
  vm.isNameInputInvalid = isNameInputInvalid;
  vm.ok = ok;
  vm.cancel = cancel;
  vm.isEditingEnabled = isEditingEnabled;

  $uibModalInstance.rendered.then(()=>{
    const uriInputs = $('.preset-modal .uri-input');
    if (uriInputs.length > 0)
      uriInputs[0].focus();
  });

  function isPresetNameUsed() {
    return vm.presetForm.presetName.$dirty && PresetService.isNameUsed(vm.presetForm.presetName.$viewValue);
  }

  function isNameInputInvalid() {
    return vm.presetForm.presetName.$dirty && vm.presetForm.presetName.$invalid;
  }

  function ok() {
    vm.preset.clusterType = type;
    if (!PresetService.isValid(vm.preset)) {
      vm.errors = formatErrors(PresetService.getErrors(), type);
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

  function formatErrors(errors, type) {
    let errorObject = {};
    errors.forEach((error) => {
      /** Work around for schema validator limitations with oneOf statement.
      Current implementation does not filter errors to closest match in schema defined inside oneOf and instead
      it returns all errors from all schemas including conditional hadoop user requirement in yarn */
      if (error.path !== 'hadoopUser' || type === 'yarn') {
        errorObject[error.path] = error.message;
      }
    });
    return errorObject;
  }

}

exports.inject = function (module) {
  module.controller('PresetModalCtrl', PresetModalCtrl);
};
