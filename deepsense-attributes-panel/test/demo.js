'use strict';
var module = angular.module('test', ['deepsense.attributes-panel']);
module.constant('config', {});
module.factory('version', function() {
  return {
    getDocsVersion: () => '1.0'
  };
});
module.controller('TestCtrl', ['$scope', '$element', 'Model', function ($scope, $element, Model) {
  let nodes = Model.getNodes();
  let workflowId = Model.getWorkflowId();
  let testCases = Model.getTestCases();

  _.assign($scope, {
    currentNode: nodes[0].id,
    testData: nodes,
    getTestCases: () => testCases,
    getNode: () => _.find(nodes, node => node.id === $scope.currentNode),
    getTestCase: () => _.find(testCases, tc => tc.node.id === $scope.currentNode),
    getWorkflowId: () => workflowId,
    isInnerWorkflow: () => isInnerWorkflow
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
