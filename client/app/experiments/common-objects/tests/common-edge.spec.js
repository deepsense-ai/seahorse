/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


describe('edge', () => {
  var Edge = require('../common-edge.js');

  var startNodeId = '11',
      endNodeId = '2-3',
      startPortId = 0,
      endPortId = 1,
      initData = {
        'startNodeId': startNodeId,
        'startPortId': startPortId,
        'endNodeId': endNodeId,
        'endPortId': endPortId
      },
      serializedData = {
        'from': {
          'nodeId': startNodeId,
          'portIndex': startPortId
        },
        'to': {
          'nodeId': endNodeId,
          'portIndex': endPortId
        }
      };


  it('should be defined', () => {
    expect(Edge).toBeDefined();
    expect(Edge).toEqual(jasmine.any(Function));
  });

  it('should have generated id', () => {
    let edge = new Edge(initData),
        id = startNodeId + '#' + startPortId + '_' + endNodeId + '#' + endPortId;
    expect(edge.id).toEqual(id);
  });

  it('should have serialaze method', () => {
    let edge = new Edge(initData);
    expect(edge.serialize).toEqual(jasmine.any(Function));
    expect(edge.serialize()).toEqual(serializedData);
  });

});
