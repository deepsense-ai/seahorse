'use strict';

import Mousetrap from 'mousetrap';

/* @ngInject */
function Keyboard($rootScope, $uibModalStack) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      Mousetrap.bind(['del', 'backspace'], () => {
        if (_.isUndefined($uibModalStack.getTop())) {
          $rootScope.$broadcast('Keyboard.KEY_PRESSED_DEL');
        }
        return false;
      });

      Mousetrap.bind('esc', () => {
        $rootScope.$broadcast('Keyboard.KEY_PRESSED_ESC');
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('keyboard', Keyboard);
};
