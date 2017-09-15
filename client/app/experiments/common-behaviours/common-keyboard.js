/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

/* @ngInject */
function Keyboard($rootScope) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      Mousetrap.bind('del', () => {
        $rootScope.$broadcast('Keyboard.KEY_PRESSED',{key:'delete'});
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('keyboard', Keyboard);
};
