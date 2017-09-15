'use strict';

/* @ngInject */
function SelectionItems() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/selection-items/selection-items.html',
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
