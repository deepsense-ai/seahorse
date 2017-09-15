function OperationItemView() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'catalog-panel-operation/catalog-panel-operation.html',
    scope: {
      id:'@',
      name:'@',
      icon:'@',
      additionalDirectives: '='
    }
  };
}

namespace.directive('operationItem', OperationItemView);
