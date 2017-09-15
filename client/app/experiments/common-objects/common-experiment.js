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

  /**
   * Removes edge form internal data.
   *
   * @param {string} edgeId
   *
   * @return {boolean}
   */
  that.removeEdge = function removeEdge(edgeId) {
    for (let i = internal.edges.length - 1; i >= 0; i--) {
      let edge = internal.edges[i];
      if (edge.getId() === edgeId) {
        internal.edges.splice(i, 1);
        return true;
      }
    }
    return false;
  };

  that.setData = function setData(data) {
    internal.id = data.id;
    internal.name = data.name;
    internal.description = data.description;
  };

  that.createNodes = function createNodes(nodes, operations) {
    for (var i = 0; i < nodes.length; i++) {
      var operation = operations[nodes[i].operation.id];
      var node = new GraphNode({
        id: nodes[i].id,
        name: operation.name,
        operationId: operation.id,
        version: operation.version,
        description: operation.description,
        x: nodes[i].ui.x,
        y: nodes[i].ui.y,
        input: operation.ports.input,
        output: operation.ports.output,
        parameters: nodes[i].parameters
      });
      internal.nodes[nodes[i].id] = node;
    }
  };


  /**
   * Create connection.
   *
   * @param {object} data
   *
   * @return {Edge}
   */
  that.createConnection = function createConnection(data) {
    var edge = new Edge({
      startNodeId: data.from.node,
      startPortId: data.from.portIndex,
      endNodeId: data.to.node,
      endPortId: data.to.portIndex
    });
    internal.edges.push(edge);

    return edge;
  };

  /**
   * Create connections.
   *
   * @param {object} connections
   */
  that.createConnections = function createConnections(connections) {
    for (var i = 0; i < connections.length; i++) {
      that.createConnection(connections[i]);
    }
  };

  /**
   * Serializes full experiment data to transfer format.
   *
   * @return {object}
   */
  that.serialize = function serialize() {
    let data = {
      'id': internal.id,
      'name': internal.name,
      'description': internal.description,
      'graph': {
        'nodes': [],
        'edges': []
      }
    };

    for (let id in internal.nodes) {
      data.graph.nodes.push(internal.nodes[id].serialize());
    }

    for (let i = 0, k = internal.edges.length; i < k; i++) {
      data.graph.edges.push(internal.edges[i].serialize());
    }

    return data;
  };
}

module.exports = Experiment;
