'use strict';

/*@ngInject*/
function AttributeCodeSnippetType() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-code-snippet/attribute-code-snippet-type.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeCodeSnippetType', AttributeCodeSnippetType);
