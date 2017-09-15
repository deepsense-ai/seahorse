'use strict';

/* @ngInject */
function Keyboard($rootScope) {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      Mousetrap.bind(['del', 'backspace'], () => {
        $rootScope.$broadcast('Keyboard.KEY_PRESSED_DEL');
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
