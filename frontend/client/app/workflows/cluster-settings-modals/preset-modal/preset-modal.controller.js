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

/* @ngInject */
function PresetModalCtrl($uibModalInstance, $log, PresetService, PresetModalLabels, preset, type, isSnapshot) {
  const vm = this;

  vm.labels = getLabelsForType(type);
  vm.preset = angular.copy(preset) || {isEditable: true, isDefault: false};
  vm.isSnapshot = isSnapshot;
  vm.focused = undefined;
  vm.isSaving = false;

  vm.isPresetNameUsed = isPresetNameUsed;
  vm.isNameInputInvalid = isNameInputInvalid;
  vm.ok = ok;
  vm.cancel = cancel;
  vm.isEditingEnabled = isEditingEnabled;

  function isPresetNameUsed() {
    return vm.presetForm.presetName.$dirty && PresetService.isNameUsed(vm.presetForm.presetName.$viewValue);
  }

  function isNameInputInvalid() {
    return vm.presetForm.presetName.$dirty && vm.presetForm.presetName.$invalid;
  }

  function ok() {
    vm.preset.clusterType = type;
    const preset = vm.preset;
    if (!PresetService.isValid(preset)) {
      $log.error(preset, PresetService.getErrors());
      vm.errors = formatErrors(PresetService.getErrors(), type);
    } else {
      vm.isSaving = true;
      PresetService.savePreset(preset)
        .then($uibModalInstance.close)
        .catch(function handleFailure(error) {
          $log.error('Problem with saving preset', error, preset);
          vm.isSaving = false;
          return true;
        });
    }
  }

  function cancel() {
    $uibModalInstance.dismiss();
  }

  function getLabelsForType(type) {
    return PresetModalLabels[type];
  }

  function isEditingEnabled() {
    return vm.preset.isEditable && !vm.isSnapshot && !vm.isSaving;
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
