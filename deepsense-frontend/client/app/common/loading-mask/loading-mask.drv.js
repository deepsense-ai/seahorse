'use strict';

/* @ngInject */
function LoadingMask($timeout) {
  return {
    restrict: 'E',
    templateUrl: 'app/common/loading-mask/loading-mask.html',
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
