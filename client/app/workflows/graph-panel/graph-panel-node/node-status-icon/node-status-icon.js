'use strict';

import tpl from './node-status-icon.html';

/* @ngInject */
function NodeStatusIcon() {

  function getDefaultClass(node) {
    if (node.knowledgeErrors.length > 0) {
      return 'fa-exclamation-circle error-icon';
    } else {
      return '';
    }
  }

  return {
    restrict: 'E',
    scope: {
      'node': '='
    },
    templateUrl: tpl,
    controller: function($scope, WorkflowService, UserService) {

      $scope.isOwner = function() {
        return WorkflowService.getCurrentWorkflow().owner.id === UserService.getSeahorseUser().id;
      };

      $scope.$watch('node.knowledgeErrors', () => {
        var errors = $scope.node.getFancyKnowledgeErrors();
        $scope.tooltipMessage = errors ? errors : '';
      });

      $scope.isNonEmpty = function() {
        return $scope.calculateClass() !== '';
      };

      $scope.calculateClass = function() {

        if (!$scope.node.state) {
          return getDefaultClass($scope.node);
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
            return getDefaultClass($scope.node);
        }
      };
    }
  };
}

exports.inject = function(module) {
  module.directive('nodeStatusIcon', NodeStatusIcon);
};
