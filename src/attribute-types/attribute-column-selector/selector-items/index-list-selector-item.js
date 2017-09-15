/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

/*@ngInject*/
function IndexListSelectorItem() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/selector-items/index-list-selector-item.html',
    replace: true
  };
}

angular.module('deepsense.attributes-panel').
    directive('indexListSelectorItem', IndexListSelectorItem);
