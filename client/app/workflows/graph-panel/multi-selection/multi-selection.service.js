'use strict';

let internal = {};
internal.selectedNodes = [];

class MultiSelectionService {
  /* @ngInject */
  constructor() {}

  addNodesToSelection(nodes) {
    internal.selectedNodes = _.union(internal.selectedNodes, nodes);
  }

  clearSelection() {
    internal.selectedNodes = [];
  }

  removeNodesFromSelection(nodes) {
    internal.selectedNodes = _.difference(internal.selectedNodes, nodes);
  }

  setSelectedNodes(nodes) {
    internal.selectedNodes = nodes;
  }

  getSelectedNodes() {
    return internal.selectedNodes;
  }
}

exports.inject = function(module) {
  module.service('MultiSelectionService', MultiSelectionService);
};
