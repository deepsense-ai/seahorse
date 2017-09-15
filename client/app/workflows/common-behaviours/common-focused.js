/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 01.07.15.
 */


'use strict';

/* @ngInject */
function Focused($timeout) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      $timeout(() => {
        element[0].focus();
      }, false);
    }
  };
}

exports.inject = function (module) {
  module.directive('focused', Focused);
};
