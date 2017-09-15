'use strict';

/*@ngInject*/
function OperationAttributes(AttributesPanelService) {
  return {
    restrict: 'E',
    scope: {
      node: '=',
      disabledMode: '='
    },
    templateUrl: 'attributes-panel/attributes-panel.html',
    replace: true,
    link: (scope, element) => {
      scope.selected = 'parameters';
      scope.$watch('node', function() {
        scope.$applyAsync(() => {
          let container = element[0];
          let heightOfOthers = _.reduce(jQuery(
              '> .ibox-title--main, > .c-attributes-tabs',
              container
            ).map((index, el) => $(el).outerHeight(true)), (prev, next) => {
              return prev + next;
            });
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
            <h2>Error title:</h2>
            <pre class="o-error-trace">{{::node.state.error.title || 'No title'}}</pre>
            <h2>Error message:</h2>
            <pre class="o-error-trace">{{::node.state.error.message || 'No message'}}</pre>
            <div ng-if="::node.state.error.details.stacktrace">
              <h2>Stack trace:</h2>
              <pre class="o-error-trace">{{::node.state.error.details.stacktrace}}</pre>
            </div>`,
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
