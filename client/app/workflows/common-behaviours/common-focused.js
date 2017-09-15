'use strict';

/* @ngInject */
function Focused($timeout) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      $timeout(() => {
        element[0].focus();
      }, 50, false);
    }
  };
}

exports.inject = function (module) {
  module.directive('focused', Focused);
};
