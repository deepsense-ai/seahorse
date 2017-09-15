'use strict';

/*@ngInject*/
function NodeStatusIcon() {
  return {
    restrict: 'E',
    scope: {
      'node': '='
    },
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/node-status-icon/node-status-icon.html',
    controller: function($scope) {

      $scope.isNonEmpty = function() {
        return $scope.calculateClass() !== '';
      };

      $scope.calculateClass = function() {

        if (!$scope.node.state) {
          return '';
        }

        switch ($scope.node.state.status) {
          case 'status_completed':
            return 'status-icon--completed fa-check';
          case 'status_running':
            return 'status-icon--running fa-cog spinning';
          case 'status_queued':
            return 'status-icon--queued fa-clock-o';
          case 'status_aborted':
            return 'status-icon--aborted fa-exclamation';
          case 'status_failed':
            return 'status-icon--failed fa-ban';
          default:
            if ($scope.node.knowledgeErrors.length > 0) {
              return 'fa-exclamation-circle error-icon';
            } else {
              return '';
            }
        }
      };
    }
  };
}

exports.inject = function(module) {
  module.directive('nodeStatusIcon', NodeStatusIcon);
};
