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
          'node': startNodeId,
          'portIndex': startPortId
        },
        'to': {
          'node': endNodeId,
          'portIndex': endPortId
        }
      };


  it('should be defined', () => {
    expect(Edge).toBeDefined();
    expect(Edge).toEqual(jasmine.any(Function));
  });

  it('should have methods for setting & getting id', () => {
    let edge = new Edge(initData);
    expect(edge.setId).toEqual(jasmine.any(Function));
    expect(edge.getId).toEqual(jasmine.any(Function));

    let id = 'test-1';
    edge.setId(id);
    expect(edge.getId()).toEqual(id);
  });

  it('should have serialaze method', () => {
    let edge = new Edge(initData);
    expect(edge.serialize).toEqual(jasmine.any(Function));
    expect(edge.serialize()).toEqual(serializedData);
  });

});
