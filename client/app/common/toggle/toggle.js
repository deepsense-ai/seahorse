'use strict';

/* @ngInject */
function Toggle() {

  return {
    restrict: 'A',
    link: (scope, element, attrs) => {

      let classesOn = `${attrs.toggleAnimateIn}`;
      let classesOff = `${attrs.toggleAnimateOut}`;

      function toggleOn() {
        element
          .removeClass(classesOn)
          .addClass(classesOff)
          .one('animationend', function() {
            $(this).removeClass(classesOn);
          });
      }

      function toggleOff() {
        element
          .removeClass(classesOff)
          .addClass(classesOn);
      }

      if (!_.isUndefined(attrs.toggleInit) && !scope.$eval(attrs.toggleInit)) {
        toggleOff();
      }

      attrs.toggleOn.split(' ').forEach((eventName) => {
        scope.$on(eventName, () => {
          toggleOn();
        });
      });

      attrs.toggleOff.split(' ').forEach((eventName) => {
        scope.$on(eventName, () => {
          toggleOff();
        });
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('toggle', Toggle);
};
