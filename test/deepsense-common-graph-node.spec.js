/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

describe('graphNode', () => {
  let GraphNode;
  let DeepsenseNodeParameters;
  let initId = '111-111-111';
  let initOperationId = '55-55-55';
  let initName = 'Sample operation';
  let initVersion = '1.1';
  let initData = {
    'id': initId,
    'operationId': initOperationId,
    'name': initName,
    'version': initVersion,
    'description': 'Aaaaa Bbbbb Cccc...',
    'input': [],
    'output': [],
    'parametersValues': {}
  };
  let serializedData = {
    'id': initId,
    'operation': {
      'id': initOperationId,
      'name': initName,
      'version': initVersion
    },
    'parameters': {}
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_GraphNode_, _DeepsenseNodeParameters_) => {
      GraphNode = _GraphNode_;
      DeepsenseNodeParameters = _DeepsenseNodeParameters_;
    });
  });

  it('should be defined', () => {
    expect(GraphNode).toBeDefined();
    expect(GraphNode).toEqual(jasmine.any(Function));
  });

  it('should have serialize method', () => {
    let graphNode = new GraphNode(initData);

    expect(graphNode.serialize).toEqual(jasmine.any(Function));
    expect(graphNode.serialize()).toEqual(serializedData);
  });

  it('should be able to set parameters', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.serialize()).toEqual(serializedData);

    graphNode.setParameters({}, DeepsenseNodeParameters);
    expect(graphNode.serialize()).toEqual(serializedData);
  });

  it('handle status changes', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.state).toBe(null);

    graphNode.updateState({
      'status': 'QUEUED'
    });
    expect(graphNode.state.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.updateState();
    expect(graphNode.state).toBe(null);

    graphNode.updateState(false);
    expect(graphNode.state).toBe(null);

    graphNode.updateState({});
    expect(graphNode.state.status).toBe(graphNode.STATUS.DRAFT);

    graphNode.updateState({
      'status': 'x'
    });
    expect(graphNode.state.status).toBe(graphNode.STATUS.DRAFT);
  });

  it('handle state changes', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.state).toBe(null);

    let results = ['result-1', 'result-2'];
    graphNode.updateState({
      'status': 'COMPLETED',
      'results': results
    });

    expect(graphNode.state.status).toBe(graphNode.STATUS.COMPLETED);
    expect(graphNode.state.results).toEqual(results);

    expect(graphNode.getResult(0)).toEqual(results[0]);
    expect(graphNode.getResult(1)).toEqual(results[1]);
    expect(graphNode.getResult(2)).not.toBeDefined();
  });
});
