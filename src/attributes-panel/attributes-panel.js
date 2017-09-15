/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function OperationAttributes($timeout, AttributesPanelService) {
  return {
    restrict: 'E',
    scope: {
      node: '=',
      disabledMode: '='
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
          let heightOfOthers = jQuery(
            '> .ibox-title--main',
            container
          ).outerHeight(true);
          let body = container.querySelector('.ibox-content');

          angular.element(body).css('height', 'calc(100% - ' + heightOfOthers + 'px)');

          if (scope.disabledMode) {
            AttributesPanelService.setDisabledMode();
            AttributesPanelService.disableElements(container);
          }
        });
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('deepsenseOperationAttributes', OperationAttributes);
