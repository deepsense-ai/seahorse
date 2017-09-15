'use strict';

let selectedNodes = [];
class MultiSelectionService {

  /* @ngInject */
  constructor() {}

  addNodesToSelection(nodes) {
    selectedNodes = _.union(selectedNodes, nodes);
  }

  isAlreadyAddedToSelection(node) {
    return this.getSelectedNodes().indexOf(node.id) > -1;
  }

  clearSelection() {
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
