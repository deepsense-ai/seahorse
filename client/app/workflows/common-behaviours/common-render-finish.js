/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

function RenderFinish($timeout) {
  return {
    restrict: 'A',
    link: function (scope) {
      if (scope.$last === true) {
        $timeout(function () {
          scope.experiment.onRenderFinish();
        });
      }
    }
  };
}

exports.inject = function (module) {
  module.directive('onRenderFinish', RenderFinish);
};
