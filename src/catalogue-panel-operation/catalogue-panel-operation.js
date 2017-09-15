function OperationItemView() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'catalogue-panel-operation/catalogue-panel-operation.html',
    scope: {
      'id': '@',
      'name': '@',
      'icon': '@'
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

      elem.on('mousedown', () => {
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

namespace.directive('operationItem', OperationItemView);
