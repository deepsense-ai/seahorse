/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function LoadingSpinnerSm() {
  return {
    templateUrl: 'app/common/loading-spinner/loading-spinner-sm.html'
  };
}
exports.function = LoadingSpinnerSm;

exports.inject = function (module) {
  module.directive('loadingSpinnerSm', LoadingSpinnerSm);
};
