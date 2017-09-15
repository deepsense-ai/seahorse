/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function OperationAttributes($timeout) {
  return {
    restrict: 'E',
    scope: {
      node: '='
    },
    templateUrl: 'attributes-panel/attributes-panel.html',
    replace: true,
    link: (scope, element) => {
      scope.closePanel = function () {
        element.addClass('fadeOut');
        $timeout(() => {
          scope.$emit('AttributePanel.UNSELECT_NODE');
        }, 1000, false);
      };

      scope.$watch('node', function() {
        scope.$applyAsync(() => {
          let container = element[0];
          let header = container.querySelector('.panel-heading');
          let body = container.querySelector('.panel-body');
          let footer = container.querySelector('.panel-footer');

          angular.element(body).css('height', (container.offsetHeight - header.offsetHeight - footer.offsetHeight - 2) + 'px');
        });
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('deepsenseOperationAttributes', OperationAttributes);
