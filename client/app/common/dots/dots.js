'use strict';

/* @ngInject */
function Dots($timeout) {
  return {
    scope: {
      dynamicDotsStatus: '='
    },
    link: (scope, element) => {
      let dots = null;

      let processDots = function processDots () {
        const LENGTH  = 19;
        const SPEED   = 450;

        if (dots) {
          dots.html(dots.html().length >= LENGTH ? '.' : dots.html() + '.');
          $timeout(processDots, SPEED);
        }
      };

      let process = function process (newButtonStatus) {
        switch (newButtonStatus) {
          case 'loading':
            if (!dots) {
              dots = angular.element(element)
                .after('<span class="o-dots">.......</span>').next();
              processDots();
              element.attr('disabled', 'disabled');
            }
            break;
          default:
            dots.remove();
            dots = null;
            element.removeAttr('disabled', 'disabled');
        }
      };

      scope.$watch('dynamicDotsStatus', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          process(newValue);
        }
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('dynamicDots', Dots);
};
