'use strict';

function OperationItemView() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/catalog-panel/catalog-panel-operation.html',
    scope: {
      id:'@',
      name:'@',
      icon:'@'
    }
  };
}

exports.inject = function (module) {
  module.directive('operationItem', OperationItemView);
};


