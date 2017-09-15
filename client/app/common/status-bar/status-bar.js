/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function ExperimentEditorStatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/status-bar/status-bar.html',
    replace: true,
    scope: {},
    link: function (scope) {
      scope.runExperiment = function runExperiment () {
        scope.$emit('Experiment.RUN');
      };

      scope.abortExperiment = function abortExperiment () {
        scope.$emit('Experiment.ABORT');
      };

      scope.saveExperiment = function saveExperiment () {
        scope.$emit('Experiment.SAVE');
      };
    }
  };
}

exports.inject = function (module) {
  module.directive('experimentEditorStatusBar', ExperimentEditorStatusBar);
};
