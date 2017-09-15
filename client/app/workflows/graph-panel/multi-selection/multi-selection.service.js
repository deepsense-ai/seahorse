'use strict';

let selectedNodes = [];
class MultiSelectionService {
  /* @ngInject */
  constructor($rootScope) {
    this.$rootScope = $rootScope;
  }

  addNodesToSelection(nodes) {
    selectedNodes = _.union(selectedNodes, nodes);
  }

  isAlreadyAddedToSelection(node) {
    return this.getSelectedNodes().indexOf(node.id) > -1;
  }

  clearSelection() {
    this.$rootScope.$broadcast('MultiSelection.CLEAR_ALL');
    selectedNodes = [];
  }

  removeNodesFromSelection(nodes) {
    selectedNodes = _.difference(selectedNodes, nodes);
  }

  setSelectedNodes(nodes) {
    selectedNodes = nodes;
  }

  getSelectedNodes() {
    return selectedNodes;
  }

}

exports.inject = function(module) {
  module.service('MultiSelectionService', MultiSelectionService);
};
