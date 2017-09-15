/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Port = require('./common-port.js');

function GraphNode(options) {
  var that = this;

  that.init = function init() {
    that.name = options.name;
    that.id = options.id;
    that.operationId = options.operationId;
    that.type = options.type;
    that.description = options.description;
    that.input = that.fetchPorts('input', options.input);
    that.output = that.fetchPorts('output', options.output);
    that.x = options.x;
    that.y = options.y;
    that.parameters = options.parameters;
  };

  that.init();
}

GraphNode.prototype.fetchPorts = function fetchPorts(type, ports) {
  var array = [];
  for (var i = 0; i < ports.length; i++) {
    var port = new Port({
      portId: type + '-' + ports[i].portIndex + '-' + this.id,
      portIndex: ports[i].portIndex,
      required: ports[i].required,
      typeQualifier: ports[i].typeQualifier
    });
    array.push(port);
  }
  return array;
};

GraphNode.CLICK = "GraphNode.CLICK";

module.exports = GraphNode;
