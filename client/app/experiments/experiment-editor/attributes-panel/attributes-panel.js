/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function OperationAttributesView($timeout) {
  return {
    restrict: 'E',
    scope: false,
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attributes-panel.html',
    replace: true,
    link: (scope, element, attrs) => {
      scope.closePanel = function () {
        element.addClass('fadeOut');
        $timeout(() => { scope.experiment.unselectNode(); }, 1000, false);
      };

      scope.$watch('experiment.getSelectedNode()', function() {
        let container = element[0];
        let header = container.querySelector('.panel-heading');
        let body = container.querySelector('.panel-body');
        let footer = container.querySelector('.panel-footer');

        scope.$applyAsync(() => {
          angular.element(body).css('height', (container.offsetHeight - header.offsetHeight - footer.offsetHeight - 2) + 'px');
        });
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('operationAttributesView', OperationAttributesView);
};
