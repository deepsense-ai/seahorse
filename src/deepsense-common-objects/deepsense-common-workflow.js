'use strict';

angular.module('deepsense.graph-model').
factory('Workflow', /*@ngInject*/function (GraphNode, Edge) {
  function Workflow() {
    let that = this;
    let internal = {
      nodes: {},
      edges: {}
    };

    that.STATUS = {
      'DRAFT': 'status_draft',
      'RUNNING': 'status_running',
      'COMPLETED': 'status_completed',
      'FAILED': 'status_failed',
      'ABORTED': 'status_aborted'
    };

    that.id = null;
    that.name = 'empty';
    that.description = 'empty';
    that.predefColors = [
      '#00B1EB', '#1ab394', '#2f4050', '#f8ac59', '#ed5565', '#DD6D3F'
    ];
    that.STATUS_DEFAULT = that.STATUS.DRAFT;
    that.lastExecutionReportTime = null;

    that.clearGraph = function clearGraph() {
      internal.nodes = {};
      internal.edges = {};
    };

    that.getNodes = function getNodes() {
      return internal.nodes;
    };

    that.getNodesIds = function getNodesIds() {
      return _.map(internal.nodes, (node) => node.id);
    };

    /**
     * @returns node, or undefined if given id doesn't exist
       */
    that.getNodeById = function getNodeById(nodeId) {
      return internal.nodes[nodeId];
    };

    that.getEdges = function getEdges() {
      return internal.edges;
    };

    that.getEdgeById = function getEdgeById(edgeId) {
      return internal.edges[edgeId];
    };

    that.getNeightbours = function getNeightbours(nodeId) {
      return _.map(
        _.filter(that.getEdgesByNodeId(nodeId), (neighbourEdge) => neighbourEdge.startNodeId === nodeId),
        (neighbourEdge) => neighbourEdge.endNodeId
      );
    };

    that.createNode = function createNode(options) {
      let operation = options.operation;

      return new GraphNode({
        'id': options.id,
        'name': operation.name,
        'uiName': options.uiName,
        'color': options.color,
        'operationId': operation.id,
        'version': operation.version,
        'icon': operation.icon,
        'parametersValues': options.parameters || {},
        'description': operation.description,
        'input': operation.ports.input,
        'output': operation.ports.output,
        'x': options.x,
        'y': options.y
      });
    };

    that.createNodes = function createNodes(nodes, operations, thirdPartyData) {
      let getCoordinate = (id, cord) => {
        try {
          return thirdPartyData.gui.nodes[id].coordinates[cord] || 0;
        } catch (e) {
          return 0;
        }
      };
      let getUiName = (id) => {
        return thirdPartyData.gui.nodes[id].uiName;
      };
      let getColor = (id) => {
        return thirdPartyData.gui.nodes[id].color;
      };

      for (let i = 0; i < nodes.length; i++) {
        let data = nodes[i];
        let id = data.id;
        let operation = operations[data.operation.id];
        let node = that.createNode({
          'id': id,
          'uiName': getUiName(id),
          'color': getColor(id),
          'operation': operation,
          'parameters': data.parameters,
          'x': getCoordinate(id, 'x'),
          'y': getCoordinate(id, 'y')
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

    /**
     * @param {Object} node Node that receives knowledge
     * @param {Number} portIndex Index of input port of [[node]] where knowledge is put
     * @return {Object|undefined} knowledge object, or undefined if edge does not exist
     */
    that.getIncomingKnowledge = function getIncomingKnowledge(node, portIndex) {
      let incomingEdge = node.getIncomingEdge(portIndex);
      if (incomingEdge) {
        let {startPortId, startNodeId} = incomingEdge;
        let parentNode = that.getNodeById(startNodeId);
        return parentNode.output[startPortId]
      }
      return undefined;
    };

    that.setStatus = function setStatus(state) {
      if (state && state.status && Object.keys(that.STATUS).indexOf(state.status) > -1) {
        that.state.status = that.STATUS[state.status];
      }
    };

    that.updateState = function updateState(state) {
      if (!state) {
        that.state = null;
      } else {
        for (let id in state.nodes) {
          let node = internal.nodes[id];
          if (node) {
            node.updateState(state.nodes[id]);
          }
        }

        that.state = {};
        that.setStatus(state);

        _.assign(that.state, {
          started: state.started,
          ended: state.ended,
          error: state.error
        });
      }
    };

    that.addNode = function addNode(node) {
      if (that.getNodeById(node.id)) {
        throw new Error('Node ' + node.id + ' already exists');
      }
      internal.nodes[node.id] = node;

      return node;
    };

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

    that.cloneEdges = function cloneEdges (fromNode, toNode) {
      that.createEdges(
        _.chain(fromNode.edges)
          .filter(edge => edge.startNodeId !== fromNode.id)
          .map(edge => {
            let startNodeId = edge.startNodeId;

            if (edge.__connectFromClone) {
              startNodeId = edge.__connectFromClone;
              delete edge.__connectFromClone;
            }

            return {
              from: {
                nodeId: startNodeId,
                portIndex: edge.startPortId
              },
              to: {
                nodeId: toNode.id,
                portIndex: edge.endPortId
              }
            };
          })
          .value()
      );
    };

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

    that.removeNodes = (nodes) => {
      _.each(nodes, (nodeId) => {
          that.removeNode(nodeId);
      });
    };

    that.removeNode = function removeNode(nodeId) {
      try {
        that.removeEdges(nodeId);
        delete internal.nodes[nodeId];
      }
      catch (error) {
        throw new Error('Cannot remove node. Node id: ' + nodeId + ' doesn\'t exist.');
      }
    };

    that.createEdge = function createEdge(data) {
      var edge = new Edge({
        startNodeId: data.from.nodeId,
        startPortId: data.from.portIndex,
        endNodeId: data.to.nodeId,
        endPortId: data.to.portIndex
      });
      return edge;
    };

    that.createEdges = function createEdges(edges) {  // TODO rename to addEdges
      for (var i = 0; i < edges.length; i++) {
        var edge = that.createEdge(edges[i]);
        that.addEdge(edge);
      }
    };

    that.serialize = function serialize() {
      let data = {
        'id': that.id,
        'workflow': {
          'nodes': [],
          'connections': []
        },
        'thirdPartyData': {
          'gui': {
            'name': that.name,
            'description': that.description,
            'predefColors': that.predefColors,
            'nodes': {}
          }
        },
        'metadata': that.metadata,
        'variables': that.variables || {}
      };

      for (let id in internal.nodes) {
        if (internal.nodes.hasOwnProperty(id)) {
          data.workflow.nodes.push(internal.nodes[id].serialize());
          data.thirdPartyData.gui.nodes[id] = {
            uiName: internal.nodes[id].uiName,
            color: internal.nodes[id].color,
            coordinates: {
              x: internal.nodes[id].x,
              y: internal.nodes[id].y
            }
          };
        }
      }

      for (let id in internal.edges) {
        if (internal.edges.hasOwnProperty(id)) {
          data.workflow.connections.push(internal.edges[id].serialize());
        }
      }

      return data;
    };

    that.updateTypeKnowledge = function (knowledge) {
      _.forEach(this.getNodes(), (node) => {
        if (knowledge[node.id]) {
          node.knowledgeErrors = knowledge[node.id].errors.slice();
          if (knowledge[node.id].ports) {
            let newOutputPorts = knowledge[node.id].ports;
            _.forEach(node.output, (port) => {
              let newTypes = newOutputPorts[port.index];
              if (newTypes) {
                port.typeQualifier = newTypes.slice();
              }
            });
          }
        }
      });
    };

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
          edge.state = Edge.STATE_TYPE.ALWAYS;
        } else if (numberOfValidTypes === 0) {
          edge.state = Edge.STATE_TYPE.NEVER;
        } else {
          edge.state = Edge.STATE_TYPE.MAYBE;
        }
      });
    };

    that.setPortTypesFromReport = function setPortTypesFromReport(resultEntities) {
      let nodes = that.getNodes();
      for (let nodeId in nodes) {
        let node = nodes[nodeId];
        if (node.state && node.state.results) {
          let reportIds = node.state.results;
          for (let i = 0; i < reportIds.length; ++i) {
            let reportId = reportIds[i];
            let resultEntity = resultEntities[reportId];
            if (resultEntity) {
              node.output[i].typeQualifier = [resultEntity.className];
            }
          }
        }
      }
    };
  }

  return Workflow;
});
