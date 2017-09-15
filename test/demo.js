'use strict';
var module = angular.module('test', ['deepsense.attributes-panel']);
module.constant('config', {});
module.controller('TestCtrl', ['$scope', '$element', 'Model', function ($scope, $element, Model) {
  let nodes = Model.getNodes();
  let workflowId = Model.getWorkflowId();

  _.assign($scope, {
    currentNode: nodes[1].id,
    testData: nodes,
    getNode: () => _.find(nodes, node => node.id === $scope.currentNode),
    getWorkflowId: () => workflowId
  });

  $scope.togglePre = function togglePre (preID) {
    $('#' + preID).toggle();
  };

  $scope.$on('AttributesPanel.OPEN_INNER_WORKFLOW', function(e, data) {
    console.log('Received OPEN_INNER_WORKFLOW event', data);
  });

  $scope.$on('AttributePanel.UNSELECT_NODE', function () {
    $scope.currentNode = null;

    $scope.$apply();
  });
}]);
