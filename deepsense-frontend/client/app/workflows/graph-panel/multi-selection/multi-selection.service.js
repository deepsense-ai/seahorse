'use strict';

let selectedNodes = [];
class MultiSelectionService {
  /* @ngInject */
  constructor($rootScope) {
    this.$rootScope = $rootScope;

    window.multi = this;
  }

  addNodeIdsToSelection(nodeIds) {
    selectedNodes = _.union(selectedNodes, nodeIds);
  }

  isAlreadyAddedToSelection(node) {
    return this.getSelectedNodeIds().indexOf(node.id) > -1;
  }

  clearSelection() {
    this.$rootScope.$broadcast('MultiSelection.CLEAR_ALL');
    selectedNodes = [];
  }

  removeNodeIdsFromSelection(nodeIds) {
    selectedNodes = _.difference(selectedNodes, nodeIds);
  }

  setSelectedNodeIds(nodeIds) {
    selectedNodes = nodeIds;
  }

  getSelectedNodeIds() {
    return selectedNodes;
  }

}

exports.inject = function(module) {
  module.service('MultiSelectionService', MultiSelectionService);
};
