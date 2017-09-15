/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Edge = require('./common-edge.js');
var GraphNode = require('./common-graph-node.js');
var ParameterFactory = require('./common-parameter-factory.js');

function Experiment() {

  var that = this;
  var internal = {};
  internal.nodes = {};
  internal.edges = {};
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

  that.addNode = function addNode(node) {
    internal.nodes[node.id] = node;
  };

  that.createNode = function createNode(nodeID, operation, paramValues = {}, x = 0, y = 0) {
    let paramSchemas = operation.parameters || {};
    let node = new GraphNode({
      id: nodeID,
      name: operation.name,
      operationId: operation.id,
      version: operation.version,
      parameters: ParameterFactory.createParametersList(paramValues, paramSchemas),
      description: operation.description,
      input: operation.ports.input,
      output: operation.ports.output,
      x: x,
      y: y
    });

    return node;
  };

  that.createNodes = function createNodes(nodes, operations) {
    for (var i = 0; i < nodes.length; i++) {
      var operation = operations[nodes[i].operation.id];
      var node = that.createNode(nodes[i].id, operation, nodes[i].parameters, nodes[i].ui.x, nodes[i].ui.y);
      that.addNode(node);
    }
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
    if (edgeId in internal.edges) {
      delete internal.edges[edgeId];
      return true;
    }
    return false;
  };

  that.setData = function setData(data) {
    internal.id = data.id;
    internal.name = data.name;
    internal.description = data.description;
  };

  that.getParametersSchemaById = function getParametersSchemaById(id) {
    return internal.experiment.getParametersSchema()[id];
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
    internal.edges[edge.id] = edge;

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

    for (let id in internal.edges) {
      data.graph.edges.push(internal.edges[id].serialize());
    }

    return data;
  };
}

module.exports = Experiment;
