/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function LoadingSpinner() {
  return {
    templateUrl: 'app/common/loading-spinner/loading-spinner.html'
  };
}
exports.function = LoadingSpinner;

exports.inject = function (module) {
  module.directive('loadingSpinner', LoadingSpinner);
};
