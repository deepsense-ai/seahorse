'use strict';

/* @ngInject */
function ButtonWait($timeout) {
  return {
    scope: {
      buttonWaitStatus: '='
    },
    link: (scope, element) => {
      let dots = null;

      let processDots = function processDots () {
        const LENGTH  = 7;
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
                .after('<span class="o-dots pull-left">...</span>').next();
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

      scope.$watch('buttonWaitStatus', (newValue, oldValue) => {
        if (newValue !== oldValue) {
          process(newValue);
        }
      });
    }
  };
}
exports.function = ButtonWait;

exports.inject = function (module) {
  module.directive('buttonWait', ButtonWait);
};
