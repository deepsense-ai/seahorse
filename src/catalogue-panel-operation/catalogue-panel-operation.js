function OperationItemView() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'catalogue-panel-operation/catalogue-panel-operation.html',
    scope: {
      id:'@',
      name:'@',
      icon:'@',
      additionalDirectives: '='
    }
  };
}

namespace.directive('operationItem', OperationItemView);
