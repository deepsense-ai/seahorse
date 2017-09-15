'use strict';

import tpl from './general-data-panel.html';

/* @ngInject */
function GeneralDataPanel() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: true,
    controller: 'GeneralDataPanelController',
    controllerAs: '$ctrl',
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
