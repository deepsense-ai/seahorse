/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 07.07.15.
 */

'use strict';

/*@ngInject*/
function LabelToFirstUnlabeled ($timeout) {
  return {
    restrict: 'A',
    link: (scope, element) => {
      scope.$applyAsync(() => {
        var nextSibling = element[0].nextElementSibling;
        var uniqueId = String(Math.random()).substring(2);
        var nextCheckbox;

        if (
          nextSibling &&
          (
            nextCheckbox = nextSibling.querySelector('input') ||
            nextSibling.querySelector('textarea') ||
            nextSibling.querySelector('button')
          ) &&
          nextCheckbox.parentNode.tagName !== 'LABEL'
        ) {
          nextCheckbox.id = uniqueId;
          $('label', element).attr('for', uniqueId);
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('labelToFirstUnlabeled', LabelToFirstUnlabeled);