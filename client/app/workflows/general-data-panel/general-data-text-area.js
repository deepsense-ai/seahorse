'use strict';

/* @ngInject */
function AutoTextArea(WorkflowService) {
  return {
    restrict: 'E',
    scope: {
      'value': '='
    },
    link: (scope, element) => {
      let textarea = element[0].children[0];
      autosize(textarea);

      scope.$applyAsync(()=> {
        autosize.update(textarea);
      });

      scope.saveWorkflow = () => {
        WorkflowService.saveWorkflow();
      };
    },
    template: '<textarea class="o-panel__description form-control" ng-model="value" ng-blur="saveWorkflow()"></textarea>'
  };
}

exports.inject = (module) => {
  module.directive('autoTextArea', AutoTextArea);
};
