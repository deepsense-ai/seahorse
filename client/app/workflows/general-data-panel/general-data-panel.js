'use strict';

/* @ngInject */
function GeneralDataPanel ($modal) {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/general-data-panel/general-data-panel.html',
    replace: true,
    scope: {
      'data': '=',
      'additionalData': '='
    },
    link: function (scope, element) {},
    controller: 'GeneralDataPanelController',
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('generalDataPanel', GeneralDataPanel);
};
