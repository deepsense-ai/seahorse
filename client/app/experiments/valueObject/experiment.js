/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Edge = require('./edge.js');
var GraphNode = require('./graphNode.js');

function Experiment() {

  var that = this;

  var internal = {};
  internal.nodes = [];
  internal.edges = [];

  that.getNodes = function () {
    return internal.nodes;
  };

  that.getEdges = function () {
    return internal.edges;
  };

  that.createNodes = function createNodes(nodes, operations) {
    for (var i = 0; i < nodes.length; i++) {
      var operation = operations[nodes[i].operation.id];
      var node = new GraphNode({
        id: nodes[i].id,
        description: operation.description,
        name: operation.name,
        x: nodes[i].ui.x,
        y: nodes[i].ui.y,
        input: operation.ports.input,
        output: operation.ports.output
      });
      internal.nodes.push(node);
    }
  };

  that.createConnections = function createConnections(connections) {
    for (var i = 0; i < connections.length; i++) {
      var edge = new Edge({
        startId: connections[i].from.node,
        startPortId: connections[i].from.node.port,
        endId: connections[i].to.node,
        endPortId: connections[i].to.node.port
      });
      internal.edges.push(edge);
    }
  };
}

module.exports = Experiment;
