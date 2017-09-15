'use strict';

import tpl from './attribute-workflow-type.html';

/*@ngInject*/
function AttributeWorkflowType() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      parameterName: '=?'
    },
    link: (scope) => {
      scope.editWorkflow = () => {
        scope.$emit('AttributesPanel.INTERNAL.CLICKED_EDIT_WORKFLOW', {
          parameterName: scope.parameterName
        });
      };
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeWorkflowType', AttributeWorkflowType);
