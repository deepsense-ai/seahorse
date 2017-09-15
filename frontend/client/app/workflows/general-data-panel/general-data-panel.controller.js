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

const ENTER_KEY_CODE = 13;
const ESCAPE_KEY_CODE = 27;

/* @ngInject */
function GeneralDataPanelController() {
  this.editableInputs = {
    name: false,
    description: false
  };

  this.buffer = {
    name: '',
    description: ''
  };

  this.showInput = (input) => {
    this.editableInputs[input] = true;
  };

  this.hideInput = (input) => {
    this.editableInputs[input] = false;
  };

  this.enableEdition = (input) => {
    this.showInput(input);
    this.buffer[input] = this[input];
  };

  this.saveNewValue = (event, input) => {
    if (event.keyCode === ENTER_KEY_CODE) {
      this[input] = this.buffer[input];
      this.hideInput(input);
    } else if (event.keyCode === ESCAPE_KEY_CODE) {
      this.hideInput(input);
    }
  };

}

exports.inject = function (module) {
  module.controller('GeneralDataPanelController', GeneralDataPanelController);
};
