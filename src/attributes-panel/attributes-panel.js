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
        let heightOfOthers = jQuery(
          '> .ibox-title--main',
          '.operation-attributes-panel'
        ).outerHeight(true);
        let container = element[0];
        let body = container.querySelector('.ibox-content');

        angular.element(body).css('height', 'calc(100% - ' + heightOfOthers + 'px)');
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('deepsenseOperationAttributes', OperationAttributes);
