/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentEditorStatusBar($rootScope) {
  return {
    restrict: 'E',
    templateUrl: 'app/common/status-bar/status-bar.html',
    replace: true,
    scope: {},
    link: function (scope) {

      scope.exportExperiment = function exportExperiment () {
        $rootScope.$broadcast('Experiment.EXPORT');
      };

      scope.saveExperiment = function saveExperiment () {
        $rootScope.$broadcast('Experiment.SAVE');
      };

      scope.clearExperiment = function clearExperiment () {
        $rootScope.$broadcast('StatusBar.CLEAR_CLICK');
      };
    }
  };
}

exports.inject = function (module) {
  module.directive('experimentEditorStatusBar', ExperimentEditorStatusBar);
};
