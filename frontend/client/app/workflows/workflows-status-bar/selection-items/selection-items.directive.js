'use strict';

import tpl from './selection-items.html';

/* @ngInject */
function SelectionItems() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: true,
    controller: 'SelectionItemsController',
    controllerAs: 'vm',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('selectionItems', SelectionItems);
};
