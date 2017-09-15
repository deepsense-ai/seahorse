'use strict';
var module = angular.module('test', ['deepsense.attributes-panel']);
module.constant('config', {});
module.controller('TestCtrl', ['$scope', '$element', 'Model', function ($scope, $element, Model) {
  var nodes = Model.getNodes();

  _.assign($scope, {
    currentNode: nodes[4].id,
    testData: nodes,
    getNode: function() {
      return _.find(nodes, function(node) {
        return node.id === $scope.currentNode;
      });
    }
  });

  $scope.togglePre = function togglePre (preID) {
    $('#' + preID).toggle();
  };

  $scope.$on('AttributePanel.UNSELECT_NODE', function () {
    $scope.currentNode = null;

    $scope.$apply();
  });
}]);
