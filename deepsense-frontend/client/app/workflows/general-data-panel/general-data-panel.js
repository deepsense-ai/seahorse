'use strict';

/* @ngInject */
function GeneralDataPanel() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/general-data-panel/general-data-panel.html',
    replace: true,
    scope: {
      'name': '=',
      'description': '=',
      'state': '=',
      'isReportMode': '='
    },
    controller: 'GeneralDataPanelController as controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('generalDataPanel', GeneralDataPanel);
};
