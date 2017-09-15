'use strict';

require('./attribute-code-snippet-type.ctrl.js');

/*@ngInject*/
function AttributeCodeSnippetType() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-code-snippet/attribute-code-snippet-type.html',
    replace: false,
    scope: {
      value: '=',
      language: '='
    },
    bindToController: true,
    controller: 'AttributeCodeSnippetTypeCtrl',
    controllerAs: 'acstCtrl'
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributeCodeSnippetType', AttributeCodeSnippetType);

