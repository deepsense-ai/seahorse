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
  let initX = 100;
  let initY = 200;
  let initData = {
    'id': initId,
    'operationId': initOperationId,
    'name': initName,
    'version': initVersion,
    'description': 'Aaaaa Bbbbb Cccc...',
    'x': initX,
    'y': initY,
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
    'ui': {
      'x': initX,
      'y': initY
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
    expect(graphNode.status).toBe(graphNode.STATUS_DEFAULT);

    graphNode.setStatus({
      'status': 'QUEUED'
    });
    expect(graphNode.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.setStatus();
    expect(graphNode.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.setStatus(false);
    expect(graphNode.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.setStatus({});
    expect(graphNode.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.setStatus({
      'status': 'x'
    });
    expect(graphNode.status).toBe(graphNode.STATUS.QUEUED);
  });

  it('handle state changes', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.status).toBe(graphNode.STATUS_DEFAULT);
    expect(graphNode.results).toEqual([]);

    let results = ['result-1', 'result-2'];
    graphNode.updateState({
      'status': 'COMPLETED',
      'results': results
    });

    expect(graphNode.status).toBe(graphNode.STATUS.COMPLETED);
    expect(graphNode.results).toEqual(results);

    expect(graphNode.getResult(0)).toEqual(results[0]);
    expect(graphNode.getResult(1)).toEqual(results[1]);
    expect(graphNode.getResult(2)).not.toBeDefined();
  });
});
