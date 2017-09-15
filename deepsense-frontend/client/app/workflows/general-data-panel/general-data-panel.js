'use strict';

/* @ngInject */
function GeneralDataPanel() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/general-data-panel/general-data-panel.html',
    replace: true,
    scope: true,
    controller: 'GeneralDataPanelCtrl',
    controllerAs: 'controller',
    bindToController: {
      'name': '=',
      'workflow': '=',
      'description': '=',
      'publicParams': '=',
      'state': '='
    }
  };
}

/* @ngInject */
function GeneralDataPanelCtrl() {

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
    if (event.keyCode === 13) { // Enter key code
      this[input] = this.buffer[input];
      this.hideInput(input);
    } else if (event.keyCode === 27) { // Escape key code
      this.hideInput(input);
    }
  }

}

exports.inject = function (module) {
  module.directive('generalDataPanel', GeneralDataPanel);
  module.controller('GeneralDataPanelCtrl', GeneralDataPanelCtrl);
};
