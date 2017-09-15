/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function AttributesList() {
  return {
    restrict: 'E',
    templateUrl: 'attributes-list/attributes-list.html',
    replace: true,
    scope: {
      'parametersList': '='
    },
    link: function (scope, element) {
      scope.noParamValues = () => (Object.keys(scope.parametersList.parameters).length === 0);

      scope.$watch('parametersList.parameters', () => {
        scope.$applyAsync(() => {
          let els = element[0].parentNode.querySelectorAll(`#attributes-list-${ scope.$id } > .parameter-item > .ibox-title > label`);
          for (let i = 0; i < els.length; ++i) {
            let el = els[i];
            el.addEventListener('click', () => {
              let control = el.parentNode.nextSibling.nextSibling;
              let input = control.querySelector('input');
              let textarea = control.querySelector('textarea');
              if (input) {
                input.focus();
              } else {
                textarea.focus();
              }
            });
          }
        });
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('attributesList', AttributesList);
