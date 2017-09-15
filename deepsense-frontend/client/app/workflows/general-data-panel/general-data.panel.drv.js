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
      'disabledMode': '=',
      'state': '='
    }
  };
}

exports.inject = function (module) {
  module.directive('generalDataPanel', GeneralDataPanel);
};
