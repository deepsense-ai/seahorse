/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


describe('graphNode', () => {
  var GraphNode = require('../common-graph-node.js');
  var ParameterFactory = require('./../common-parameter-factory.js');

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
        'parameters': ParameterFactory.createParametersList({}, {})
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

});
