'use strict';

import tpl from './public-params-list.html';

function PublicParamsList() {
  return {
    scope: {
      'workflow': '=',
      'publicParams': '='
    },
    templateUrl: tpl,
    replace: 'true',
    controller: ($scope) => {

      // When node is deleted, public params for that node should also be deleted
      $scope.$watch(() => $scope.workflow.getNodes(), (newNodes) => {
        $scope.publicParams = _.reject($scope.publicParams, pp => _.isUndefined(newNodes[pp.nodeId]));
      });

      $scope.isDuplicate = (publicParam) => {
        let withRequestedName = _.filter($scope.publicParams, (p) => p.publicName === publicParam.publicName);
        return withRequestedName.length > 1;
      };
      $scope.getNodeName = (publicParam) => {
        let node = $scope.workflow.getNodeById(publicParam.nodeId);
        return node.uiName || node.name;
      };
    }
  };
}

exports.inject = function(module) {
  module.directive('publicParamsList', PublicParamsList);
};
