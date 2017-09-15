/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Port = require('./port.js');

function GraphNode(options) {
  var that = this;
  var internal = {};

  that.init = function init() {
    that.name = options.name;
    that.id = options.id;
    that.type = options.type;
    that.description = options.description;
    that.input = internal.fetchPorts('input', options.input);
    that.output = internal.fetchPorts('output', options.output);
    that.x = options.x;
    that.y = options.y;
    that.parameters = options.parameters;
  };

  internal.fetchPorts = function fetchPorts(type, ports) {
    var array = [];
    for (var i = 0; i < ports.length; i++) {
      var port = new Port({
        portId: type + '-' + ports[i].portIndex + '-' + that.id,
        portIndex: ports[i].portIndex,
        required: ports[i].required,
        typeQualifier: ports[i].typeQualifier
      });
      array.push(port);
    }
    return array;
  };

  that.init();
}

module.exports = GraphNode;
