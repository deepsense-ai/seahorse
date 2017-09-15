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
