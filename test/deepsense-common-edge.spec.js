/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

describe('Edge', () => {
  let Edge;
  let startNodeId = '11';
  let endNodeId = '2-3';
  let startPortId = 0;
  let endPortId = 1;
  let initData = {
    'startNodeId': startNodeId,
    'startPortId': startPortId,
    'endNodeId': endNodeId,
    'endPortId': endPortId
  };
  let serializedData = {
    'from': {
      'nodeId': startNodeId,
      'portIndex': startPortId
    },
    'to': {
      'nodeId': endNodeId,
      'portIndex': endPortId
    }
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_Edge_) => {
      Edge = _Edge_;
    });
  });

  it('should be defined', () => {
    expect(Edge).toBeDefined();
    expect(Edge).toEqual(jasmine.any(Function));
  });

  it('should have generated id', () => {
    let edge = new Edge(initData);
    let id = `${startNodeId}#${startPortId}_${endNodeId}#${endPortId}`;
    expect(edge.id).toEqual(id);
  });

  it('should have serialize method', () => {
    let edge = new Edge(initData);
    expect(edge.serialize).toEqual(jasmine.any(Function));
    expect(edge.serialize()).toEqual(serializedData);
  });

});
