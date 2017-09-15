/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function StatusBar() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/status-bar/status-bar.html',
    replace: true,
    scope: {
      'status': '='
    },
    link: function (scope, element, attrs) {
      element[0].querySelector('.run-experiment-button').addEventListener('click', () => {
        scope.$emit('Experiment.RUN', {});
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('statusBar', StatusBar);
};
