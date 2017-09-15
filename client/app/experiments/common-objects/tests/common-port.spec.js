/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


describe('port', () => {
  var Port = require('../common-port.js');

  var initNodeId = 'node1',
      initType = 'portType',
      initPortIndex = 1,
      initData = {
        'nodeId': initNodeId,
        'type': initType,
        'portIndex': initPortIndex,
        'required': true,
        'typeQualifier': []
      };


  it('should be defined', () => {
    expect(Port).toBeDefined();
    expect(Port).toEqual(jasmine.any(Function));
  });

  it('should generate method', () => {
    let port = new Port(initData);
    expect(port.generateId).toEqual(jasmine.any(Function));

    let id = initType + '-' + initPortIndex + '-' + initNodeId;
    expect(port.id).toEqual(id);
    expect(port.generateId(initData)).toEqual(id);
  });
});
