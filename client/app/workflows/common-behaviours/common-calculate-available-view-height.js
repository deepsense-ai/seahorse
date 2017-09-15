'use strict';

/* @ngInject */
function CalculateAvailableViewHeight() {
  return {
    restrict: 'A',
    link: function(scope, element) {
      let siblings = (el) => {
        return Array.prototype.filter.call(el.parentNode.children, function(child) {
          return child !== el;
        });
      };
      let allSiblingsHeight = (siblings) => {
        return _.reduce(_.map(siblings, el => $(el)
          .outerHeight(true)), (current, next) => current + next);
      };

      element.css('height',
        `calc(100% - ${allSiblingsHeight(siblings(element[0]))}px)`);
    }
  };
}

exports.inject = function(module) {
  module.directive('calculateAvailableViewHeight', CalculateAvailableViewHeight);
};
