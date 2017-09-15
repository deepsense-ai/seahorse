/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function OperationAttributesView() {
  return {
    restrict: 'E',
    scope: false,
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attributes-panel.html',
    replace: true,
    link: (scope, element, attrs) => {
      scope.closePanel = function () {
        scope.experiment.unselectNode();
      };

      scope.$watch('experiment.getSelectedNode()', function() {
        let container = element[0],
            header = container.querySelector('.panel-heading'),
            body = container.querySelector('.panel-body'),
            footer = container.querySelector('.panel-footer');

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
