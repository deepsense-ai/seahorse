/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function ExperimentEditorStatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/status-bar/status-bar.html',
    replace: true,
    scope: {
      'status': '='
    },
    link: function (scope, element) {
      let runExperimentButton = element[0].querySelector('.run-experiment-button');
      let abortExperimentButton = element[0].querySelector('.abort-experiment-button');
      let saveExperimentButton = element[0].querySelector('.save-experiment-button');

      runExperimentButton.addEventListener('click', () => {
        scope.$emit('Experiment.RUN');
      });
      abortExperimentButton.addEventListener('click', () => {
        scope.$emit('Experiment.ABORT');
      });
      saveExperimentButton.addEventListener('click', () => {
        scope.$emit('Experiment.SAVE');
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('experimentEditorStatusBar', ExperimentEditorStatusBar);
};
