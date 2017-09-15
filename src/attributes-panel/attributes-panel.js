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
          } else {
            AttributesPanelService.setEnabledMode();
          }
        });
      });
    },
    controller: function ($scope, $element, $timeout, $modal) {
      this.showErrorMessage = function showErrorMessage () {
        $modal.open({
          scope: $scope,
          template: `
            <h2>Error message:</h2>
            <pre class="o-error-trace">{{node.state.error.message}}</pre>`,
          windowClass: 'o-modal--error'
        });
      };

      this.closePanel = function () {
        $element.addClass('fadeOut');
        $timeout(() => {
          $scope.$emit('AttributePanel.UNSELECT_NODE');
        }, 1000, false);
      };
    },
    controllerAs: 'panel'
  };
}

angular.module('deepsense.attributes-panel').
    directive('deepsenseOperationAttributes', OperationAttributes);
