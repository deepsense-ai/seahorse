'use strict';

import tpl from './catalogue-panel-operation.html';

function OperationItemView() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: tpl,
    scope: {
      'id': '@',
      'name': '@',
      'icon': '@',
      'isRunning': '='
    },
    link: (scope, elem) => {
      scope.highlight = false;
      scope.$on('ConnectionHinter.HIGHLIGHT_OPERATIONS', (_, data) => {
        scope.highlight = data[scope.id];
        scope.$digest();
      });
      scope.$on('ConnectionHinter.DISABLE_HIGHLIGHTINGS', () => {
        scope.highlight = false;
        scope.$digest();
      });
      scope.getIcons = function () {
        return scope.icon.split(' ');
      };

      elem.on('mousedown', () => {
        $('.popover').hide();
      });
      elem.on('wheel', () => {
        $('.popover').hide();
      });
      elem.on('dragstart', () => {
        $('.popover').hide();
      });
      elem.on('mouseover', () => {
        $('.popover').show();
      });
      elem.on('mouseup', () => {
        $('.popover').show();
      });
    }
  };
}
angular.module('deepsense-catalogue-panel').directive('operationItem', OperationItemView);
