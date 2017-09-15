'use strict';

/*@ngInject*/
function AttributeWorkflowType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-workflow/attribute-workflow-type.html',
    replace: true,
    scope: {
      parameterName: '=?'
    },
    link: (scope) => {
      scope.editWorkflow = () => {
        scope.$emit('AttributesPanel.INTERNAL.CLICKED_EDIT_WORKFLOW', {
          parameterName: scope.parameterName
        })
      }
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeWorkflowType', AttributeWorkflowType);
