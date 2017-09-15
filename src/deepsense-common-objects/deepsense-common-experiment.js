/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

angular.module('deepsense.graph-model').
  factory('Experiment', /*@ngInject*/function(GraphNode, Edge) {
    function Experiment() {
      var that = this;
      var internal = {};
      internal.nodes = {};
      internal.edges = {};

      that.STATUS = {
        'DRAFT':     'status_draft',
        'RUNNING':   'status_running',
        'COMPLETED': 'status_completed',
        'FAILED':    'status_failed',
        'ABORTED':   'status_aborted'
      };
      that.STATUS_DEFAULT = that.STATUS.DRAFT;

      /**
       * Returns experiment id.
       *
       * @return {string}
       */
      that.getId = function getId() {
        return internal.id;
      };

      that.getNodes = function getNodes() {
        return internal.nodes;
      };

      that.getNodeById = function getNodeById(nodeId) {
        return internal.nodes[nodeId];
      };

      that.getEdges = function getEdges() {
        return internal.edges;
      };

      that.getEdgeById = function getEdgeById(edgeId) {
        return internal.edges[edgeId];
      };

      /**
       * Creates graph node.
       *
       * @param {object} options
       *
       * @return {GraphNode}
       */
      that.createNode = function createNode(options) {
        let operation = options.operation;

        return new GraphNode({
          'id': options.id,
          'name': operation.name,
          'operationId': operation.id,
          'version': operation.version,
          'icon': operation.icon,
          'parametersValues': options.parameters || {},
          'description': operation.description,
          'input': operation.ports.input,
          'output': operation.ports.output,
          'x': options.x,
          'y': options.y,
          'state': options.state
        });
      };

      that.createNodes = function createNodes(nodes, operations, state) {
        for (let i = 0; i < nodes.length; i++) {
          let data = nodes[i],
            id = data.id,
            operation = operations[data.operation.id],
            node = that.createNode({
              'id': id,
              'operation': operation,
              'parameters': data.parameters,
              'x': data.ui.x,
              'y': data.ui.y,
              'state': state.nodes[id]
            });
          that.addNode(node);
        }
      };

      that.removeEdges = function removeEdges(nodeId) {
        for (var edge in internal.nodes[nodeId].edges) {
          that.removeEdge(internal.nodes[nodeId].edges[edge]);
        }
      };

      that.getEdgesByNodeId = function getEdgesByNodeId(nodeId) {
        return internal.nodes[nodeId].edges;
      };

      that.setData = function setData(data) {
        internal.id = data.id;
        internal.name = data.name;
        internal.description = data.description;
      };

      /**
       * Sets experiment status.
       *
       * @param {object} state
       */
      that.setStatus = function setStatus(state) {
        if (state && state.status && Object.keys(that.STATUS).indexOf(state.status) > -1) {
          that.status = that.STATUS[state.status];
        }
      };

      /**
       * Returns experiment status.
       *
       * @return {[type]}
       */
      that.getStatus = function getStatus() {
        return that.status || that.STATUS_DEFAULT;
      };

      /**
       * Updates experiment state.
       *
       * @param {object} state
       */
      that.updateState = function updateState(state) {
        for (let id in state.nodes) {
          let node = internal.nodes[id];
          if (node) {
            node.updateState(state.nodes[id]);
          }
        }
        that.setStatus(state);
      };

      /**
       * Checks if experiment is in run state.
       *
       * @return {boolean}
       */
      that.isRunning = function isRunning() {
        return that.getStatus() === that.STATUS.RUNNING;
      };

      /**
       * Add node from internal data.
       *
       * @param {Node} node
       */
      that.addNode = function addNode(node) {
        if (that.getNodeById(node.id)) {
          throw new Error('Node ' + node.id + ' already exists');
        }
        internal.nodes[node.id] = node;
      };

      /**
       * Add edge from internal data.
       *
       * @param {Edge} edge
       */
      that.addEdge = function addEdge(edge) {

        if (!edge.id) {
          throw new Error('Cannot add edge without id set.');
        }
        else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot create edge between nodes. Start node id: ' + edge.startNodeId + ' doesn\'t exist.');
        }
        else if (!that.getNodeById(edge.endNodeId)) {
          throw new Error('Cannot create edge between nodes. End node id: ' + edge.endNodeId + ' doesn\'t exist.');
        }

        internal.edges[edge.id] = edge;
        that.getNodeById(edge.startNodeId).edges[edge.id] = edge;
        that.getNodeById(edge.endNodeId).edges[edge.id] = edge;
      };

      /**
       * Removes edge
       *
       * @param {Edge} edge
       */
      that.removeEdge = function removeEdge(edge) {
        if (!edge.id) {
          throw new Error('Cannot remove edge. Edge id: ' + edge.id + ' doesn\'t exist.');
        }
        else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot remove edge between nodes. Start node id: ' + edge.startNodeId + ' doesn\'t exist.');
        }
        else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot remove edge between nodes. End node id: ' + edge.endNodeId + ' doesn\'t exist.');
        }

        delete internal.edges[edge.id];
        delete internal.nodes[edge.startNodeId].edges[edge.id];
        delete internal.nodes[edge.endNodeId].edges[edge.id];
      };

      /**
       * Removes node
       *
       * @param {string} nodeId
       */
      that.removeNode = function removeNode(nodeId) {
        try {
          that.removeEdges(nodeId);
          delete internal.nodes[nodeId];
        }
        catch (error) {
          throw new Error('Cannot remove node. Node id: ' + nodeId + ' doesn\'t exist.');
        }
      };

      /**
       * Create edge.
       *
       * @param {object} data
       *
       * @return {Edge}
       */
      that.createEdge = function createEdge(data) {
        var edge = new Edge({
          startNodeId: data.from.nodeId,
          startPortId: data.from.portIndex,
          endNodeId: data.to.nodeId,
          endPortId: data.to.portIndex
        });
        return edge;
      };

      /**
       * Create edges.
       *
       * @param {object} edges
       */
      that.createEdges = function createEdges(edges) {
        for (var i = 0; i < edges.length; i++) {
          var edge = that.createEdge(edges[i]);
          that.addEdge(edge);
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

      /**
       * Updates type knowledge concerning output ports
       *
       * @param {Object} typeKnowledge
       */
      that.updateTypeKnowledge = function (typeKnowledge) {
        _.forEach(this.getNodes(), (node) => {
          let newOutputPorts = typeKnowledge[node.id];
          if (newOutputPorts) {
            _.forEach(node.output, (port) => {
              let newTypes = newOutputPorts[port.index];
              if (newTypes) {
                port.typeQualifier = newTypes.slice();
              }
            });
          }
        });
      };

      /**
       * Updates all edges' states.
       *
       * @param {OperationsHierarchyService} OperationsHierarchyService
       */
      that.updateEdgesStates = function (OperationsHierarchyService) {
        let nodes = this.getNodes();

        _.forEach(this.getEdges(), (edge) => {
          let startNode = nodes[edge.startNodeId];
          let endNode = nodes[edge.endNodeId];
          let outputTypes = _.find(startNode.output, (port) => port.index === edge.startPortId).typeQualifier;
          let inputTypes = _.find(endNode.input, (port) => port.index === edge.endPortId).typeQualifier;

          let numberOfValidTypes = 0;
          _.each(outputTypes, (outputType) => {
            numberOfValidTypes += (OperationsHierarchyService.IsDescendantOf(outputType, inputTypes) ? 1 : 0);
          });

          if (numberOfValidTypes === outputTypes.length) {
            edge.state = edge.STATE_TYPE.ALWAYS;
          } else if (numberOfValidTypes === 0) {
            edge.state = edge.STATE_TYPE.NEVER;
          } else {
            edge.state = edge.STATE_TYPE.MAYBE;
          }

          //console.log(`(${startNode.name}, ${outputTypes}) -> (${endNode.name}, ${inputTypes})  ${edge.state}`);
        });
      };
    }

    return Experiment;
  });