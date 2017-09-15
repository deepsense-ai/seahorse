/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

angular
  .module('deepsense.graph-model')
  .factory('Workflow', /* @ngInject */function (GraphNode, Edge) {
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

      // TODO rename to getChildren, or at least to getCholtren
      that.getNeightbours = function getNeightbours(nodeId) {
        return _.map(
          _.filter(that.getEdgesByNodeId(nodeId), (neighbourEdge) => neighbourEdge.startNodeId === nodeId),
          (neighbourEdge) => neighbourEdge.endNodeId
        );
      };

      that.createNode = function createNode(options) {
        options = angular.copy(options);
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
          'y': options.y,
          'nodeGetter': that.getNodeById
        });
      };

      that.createNodes = function createNodes(nodes, operations, thirdPartyData) {
        let getAverageCoordinates = (nodes, axis) => {
          if (Object.keys(nodes).length > 0) {
            let coordinate = 0;
            for (let key in nodes) {
              coordinate += nodes[key].coordinates[axis];
            }
            return Math.floor(coordinate / Object.keys(nodes).length);
          }
          return 0;
        };

        let getCoordinate = (id, axis) => {
          try {
            return thirdPartyData.gui.nodes[id].coordinates[axis] || 0;
          } catch (e) {
            return getAverageCoordinates(thirdPartyData.gui.nodes, axis);
          }
        };

        let getUiName = (id) => {
          if (thirdPartyData.gui.nodes[id]) {
            return thirdPartyData.gui.nodes[id].uiName;
          }
          return '';
        };

        let getColor = (id) => {
          if (thirdPartyData.gui.nodes[id]) {
            return thirdPartyData.gui.nodes[id].color;
          }
          return '#00B1EB';
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
        } else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot create edge between nodes. Start node id: ' + edge.startNodeId + ' doesn\'t exist.');
        } else if (!that.getNodeById(edge.endNodeId)) {
          throw new Error('Cannot create edge between nodes. End node id: ' + edge.endNodeId + ' doesn\'t exist.');
        }

        internal.edges[edge.id] = edge;
        that.getNodeById(edge.startNodeId).edges[edge.id] = edge;
        that.getNodeById(edge.endNodeId).edges[edge.id] = edge;
      };

      that.cloneEdges = function cloneEdges(fromNode, toNode) {
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
        } else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot remove edge between nodes. Start node id: ' + edge.startNodeId + ' doesn\'t exist.');
        } else if (!that.getNodeById(edge.startNodeId)) {
          throw new Error('Cannot remove edge between nodes. End node id: ' + edge.endNodeId + ' doesn\'t exist.');
        }

        let startNode = internal.nodes[edge.startNodeId];
        let endNode = internal.nodes[edge.endNodeId];

        delete internal.edges[edge.id];
        delete startNode.edges[edge.id];
        delete endNode.edges[edge.id];

        // Refreshing params of target node. Dynamic params can change because of lost knowledge.
        endNode.refreshParameters();
      };

      that.removeNodes = (nodes) => {
        _.each(nodes, (nodeId) => {
            that.removeNode(nodeId);
        });
      };

      that.removeNode = function removeNode(nodeId) {
        that.removeEdges(nodeId);
        delete internal.nodes[nodeId];
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
            let newOutputPorts = knowledge[node.id].ports;
            if (newOutputPorts) {
              _.forEach(node.output, (port) => {
                let {types, result} = newOutputPorts[port.index];
                // 'types' is array of identifier of possible output types.
                // 'result' is optional field with detailed information about resulting entity.
                if (types) {
                  port.typeQualifier = types.slice();
                }
                port.result = result;
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
            numberOfValidTypes += OperationsHierarchyService.IsDescendantOf(outputType, inputTypes) ? 1 : 0;
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
              if (resultEntity && _.isObject(node.output[i])) {
                node.output[i].typeQualifier = [resultEntity.className];
              }
            }
          }
        }
      };
    }

    return Workflow;
  });
