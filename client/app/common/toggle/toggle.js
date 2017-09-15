'use strict';

/* @ngInject */
function Toggle() {
  return {
    restrict: 'A',
    link: (scope, element, attrs) => {

      let classesOn = `${attrs.toggleAnimateIn}`;
      let classesOff = `${attrs.toggleAnimateOut}`;

      scope.$on(attrs.toggleOn, () => {
        element
          .removeClass(classesOn)
          .addClass(classesOff);
      });

      scope.$on(attrs.toggleOff, () => {
        element
          .removeClass(classesOff)
          .addClass(classesOn);
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('toggle', Toggle);
};
