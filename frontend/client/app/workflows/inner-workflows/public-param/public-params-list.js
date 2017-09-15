/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
