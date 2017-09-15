'use strict';

import tpl from './loading-mask.html';

/* @ngInject */
function LoadingMask($timeout) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    scope: {
      'type': '@', /* Color of bar. Possible types: success, info, warning, danger */
      'string': '@' /* String displayed on bar */
    },
    replace: true,
    controller: 'LoadingMaskCtrl',
    controllerAs: 'lmCtrl',
    bindToController: true
  };
}
exports.function = LoadingMask;

exports.inject = function(module) {
  module.directive('loadingMask', LoadingMask);
};
