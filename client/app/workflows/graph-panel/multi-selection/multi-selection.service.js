'use strict';

exports.inject = (module) => {
  module.service('MultiSelectionService', () => {

    let internal = {};
    internal.selectedNodes = [];

    class MultiSelectionService {

      constructor() {
        internal.selectedNodes = [];
      }

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

    return new MultiSelectionService();
  });
};
