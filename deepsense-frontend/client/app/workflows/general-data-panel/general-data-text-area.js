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

      scope.$applyAsync(() => {
        autosize.update(textarea);
      });
    },
    template: '<textarea class="o-panel__description form-control" placeholder="Enter description" ng-model="value" rows="1"></textarea>'
  };
}

exports.inject = (module) => {
  module.directive('autoTextArea', AutoTextArea);
};
