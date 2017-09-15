/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

describe('port', () => {
  let Port;
  let initNodeId = 'node1';
  let initType = 'portType';
  let initPortIndex = 1;
  let initData = {
    'nodeId': initNodeId,
    'type': initType,
    'portIndex': initPortIndex,
    'required': true,
    'typeQualifier': []
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_Port_) => {
      Port = _Port_;
    });
  });

  it('should be defined', () => {
    expect(Port).toBeDefined();
    expect(Port).toEqual(jasmine.any(Function));
  });

  it('should have generated id', () => {
    let port = new Port(initData);
    let id = initType + '-' + initPortIndex + '-' + initNodeId;
    expect(port.id).toEqual(id);
  });
});
