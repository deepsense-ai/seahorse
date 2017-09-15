/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function TypeListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: 'app/experiments/experiment-editor/attributes-panel/attribute-types/attribute-column-selector/selector-items/type-list-selector-item.html',
    replace: true
  };
}

exports.inject = function (module) {
  module.directive('typeListSelectorItem', TypeListSelectorItem);
};
