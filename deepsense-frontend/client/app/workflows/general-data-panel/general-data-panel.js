'use strict';

/* @ngInject */
function GeneralDataPanel() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/general-data-panel/general-data-panel.html',
    replace: true,
    scope: {
      'name': '=',
      'workflow': '=',
      'description': '=',
      'publicParams': '=',
      'state': '=',
      'isReportMode': '='
    },
    controller: () => {},
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('generalDataPanel', GeneralDataPanel);
};
