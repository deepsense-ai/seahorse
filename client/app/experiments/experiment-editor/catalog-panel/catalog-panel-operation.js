"use strict";

function OperationItemView() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/catalog-panel/catalog-panel-operation.html',
    scope: {
      id:'@',
      name:'@',
      icon:'@'
    },
    link: function (scope, element, attrs) {
      element.on('click', function () {
        console.log(element);
      });
    }
  }
}

exports.inject = function (module) {
  module.directive('operationItem', OperationItemView);
};


