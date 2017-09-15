'use strict';

describe('MultiSelectionService', () => {
  let MultiSelectionService;

  beforeEach(() => {
    let module = angular.module('service', []);
    require('./multi-selection.service').inject(module);
    angular.mock.module('service');
    angular.mock.inject((_MultiSelectionService_) => {
      MultiSelectionService = _MultiSelectionService_;
    });
  });

  it('should return empty array is nothing is selected', () => {
    expect(MultiSelectionService.getSelectedNodes()).toEqual([]);
  });

  it('should return nodeId when an element was added to selection', () => {
    let nodeId = 'node-' + Math.random().toString();
    MultiSelectionService.addNodesToSelection([nodeId]);
    expect(MultiSelectionService.getSelectedNodes()).toContain(nodeId);
  });

  it('should return return the same amount of elements that are added to selection if there are no duplicates', () => {
    const numberOfNodesAdded = 100;
    let nodes = [];
    for (let i = 0; i < numberOfNodesAdded; i++) {
      let nodeId = 'node-' + Math.random().toString();
      MultiSelectionService.addNodesToSelection([nodeId]);
      nodes.push(nodeId);
    }
    expect(MultiSelectionService.getSelectedNodes()).toEqual(nodes);
  });

  it('shouldnt remove the added element', () => {
    let nodeId = 'node-' + Math.random().toString();
    MultiSelectionService.addNodesToSelection([nodeId]);
    MultiSelectionService.removeNodesFromSelection([nodeId]);
    expect(MultiSelectionService.getSelectedNodes()).toEqual([]);
  });

  it('shouldnt add the same elements twice', () => {
    let nodeId = 'node-' + Math.random().toString();
    MultiSelectionService.addNodesToSelection([nodeId]);
    MultiSelectionService.addNodesToSelection([nodeId]);
    expect(MultiSelectionService.getSelectedNodes()).toEqual([nodeId]);
  });

});
