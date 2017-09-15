/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Edge = require('./common-edge.js');
var GraphNode = require('./common-graph-node.js');

function Experiment() {

  var that = this;
  var internal = {};
  internal.nodes = {};
  internal.edges = [];
  internal.parameters = {};

  that.getNodes = function getNodes() {
    return internal.nodes;
  };

  that.getNodeById = function getNodeById(nodeId) {
    var nodes = that.getNodes();
    return nodes[nodeId];
  };

  that.getEdges = function getEdges() {
    return internal.edges;
  };

  that.getParametersSchema = function getParametersSchema() {
    return internal.parameters;
  };

  that.saveParametersSchema = function saveParametersSchema(operations) {
    for (let operationId in operations) {
      if (operations.hasOwnProperty(operationId)) {
        internal.parameters[operationId] = operations[operationId].parameters;
      }
    }
  };

  that.createNodes = function createNodes(nodes, operations) {
    for (var i = 0; i < nodes.length; i++) {
      var operation = operations[nodes[i].operation.id];
      var node = new GraphNode({
        id: nodes[i].id,
        operationId: operation.id,
        description: operation.description,
        name: operation.name,
        x: nodes[i].ui.x,
        y: nodes[i].ui.y,
        input: operation.ports.input,
        output: operation.ports.output,
        parameters: nodes[i].parameters
      });
      internal.nodes[nodes[i].id] = node;
    }
  };

  that.createConnections = function createConnections(connections) {
    for (var i = 0; i < connections.length; i++) {
      var edge = new Edge({
        startNodeId: connections[i].from.node,
        startPortId: connections[i].from.portIndex,
        endNodeId: connections[i].to.node,
        endPortId: connections[i].to.portIndex
      });
      internal.edges.push(edge);
    }
  };
}

module.exports = Experiment;
