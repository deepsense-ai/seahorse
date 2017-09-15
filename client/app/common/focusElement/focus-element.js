'use strict';

function focusElement() {
  return {
    link: function (scope, element, attrs) {
      scope.$watch(attrs.focusElement, function (value) {
        if (value === true) {
          element[0].focus();
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('focusElement', focusElement);
};