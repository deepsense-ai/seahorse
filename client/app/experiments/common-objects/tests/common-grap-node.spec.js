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
      initParameters = [{
        'id': '3-3',
        'value': 5
      }],
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
        'parameters': initParameters
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
        'parameters': initParameters
      };


  it('should be defined', () => {
    expect(GraphNode).toBeDefined();
    expect(GraphNode).toEqual(jasmine.any(Function));
  });


  it('should have serialaze method', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.serialize).toEqual(jasmine.any(Function));
    expect(graphNode.serialize()).toEqual(serializedData);
  });

});
