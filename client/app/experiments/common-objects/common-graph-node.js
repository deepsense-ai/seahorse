/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Port = require('./common-port.js');

function GraphNode(options) {
  console.log(options);
  this.name = options.name;
  this.id = options.id;
  this.operationId = options.operationId;
  this.version = options.version;
  this.type = options.type;
  this.type = options.type;
  this.description = options.description;
  this.input = this.fetchPorts('input', options.input);
  this.output = this.fetchPorts('output', options.output);
  this.x = options.x;
  this.y = options.y;
  this.parameters = options.parameters;
}

GraphNode.prototype.fetchPorts = function fetchPorts(type, ports) {
  var array = [];
  for (var i = 0; i < ports.length; i++) {
    var port = new Port({
      nodeId: this.id,
      type: type,
      portIndex: ports[i].portIndex,
      required: ports[i].required,
      typeQualifier: ports[i].typeQualifier
    });
    array.push(port);
  }
  return array;
};

/**
 * Serializes node data to transfer format.
 *
 * @return {object}
 */
GraphNode.prototype.serialize = function serialize() {
  let data = {
    'id': this.id,
    'operation': {
      'id': this.operationId,
      'name': this.name,
      'version': this.version
    },
    'parameters': this.parameters.serialize(),
    'ui': {
      'x': this.x,
      'y': this.y
    }
  };

  return data;
};

GraphNode.CLICK = 'GraphNode.CLICK';
GraphNode.MOVE = 'GraphNode.MOVE';

module.exports = GraphNode;
