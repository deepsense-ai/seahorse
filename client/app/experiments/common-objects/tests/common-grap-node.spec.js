/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


describe('graphNode', () => {
  var GraphNode = require('../common-graph-node.js');

  var initId = '111-111-111',
      initOperationId = '55-55-55',
      initName = 'Sample operation',
      initVersion = '1.1',
      initX = 100,
      initY = 200,
      initData = {
        'id': initId,
        'operationId': initOperationId,
        'name': initName,
        'version': initVersion,
        'description': 'Aaaaa Bbbbb Cccc...',
        'x': initX,
        'y': initY,
        'input': [],
        'output': [],
        'parameters': {}
      },
      serializedData = {
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
    angular.module('test', ['deepsense.node-parameters']);

    angular.mock.module('test');
    angular.mock.inject((DeepsenseNodeParameters) => {
      initData.parameters = DeepsenseNodeParameters.factory.createParametersList({}, {});
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
