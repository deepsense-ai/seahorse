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

import jsPlumb from 'jsplumb';

const POSITION_MAP = {
  OUTPUT: {
    left: 'BottomLeft',
    center: 'BottomCenter',
    right: 'BottomRight'
  },
  INPUT: {
    left: [0.10, 0, 0, -1], // Top middle left part
    center: 'TopCenter',
    right: [0.90, 0, 0, -1] // Top middle right part
  }
};

const NEW_NODE_NODE = {
  id: 'new-node',
  input: [{
    id: 'input-0-new-node',
    portPosition: 'center',
    index: 0
  }],
  output: [],
  originalOutput: []
};

const NEW_NODE_EDGE = {
  id: 'new-node-edge',
  endNodeId: NEW_NODE_NODE.id,
  endPortId: 0
};


class AdapterService {
  constructor(WorkflowService, GraphStyleService, Report) {
    'ngInject';

    this.WorkflowService = WorkflowService;
    this.GraphStyleService = GraphStyleService;
    this.Report = Report;

    this.selectedPortId = null;
  }

  initialize(container) {
    this.container = container;
    this.reset();
  }

  bindEvents() {
    // Edge management
    jsPlumb.bind('connection', (jsPlumbEvent, originalEvent) => {
      this.stopDragging();
      if (!originalEvent) {
        return;
      }

      const data = {
        from: {
          nodeId: jsPlumbEvent.sourceId.slice('node-'.length),
          portIndex: jsPlumbEvent.sourceEndpoint.getParameter('portIndex')
        },
        to: {
          nodeId: jsPlumbEvent.targetId.slice('node-'.length),
          portIndex: jsPlumbEvent.targetEndpoint.getParameter('portIndex')
        }
      };
      const edge = this.workflow.createEdge(data);
      this.workflow.addEdge(edge);
      //TODO remove, as it shouldn't be here
      this.WorkflowService.updateEdgesStates();
      jsPlumbEvent.connection.setParameter('edgeId', edge.id);

      if (!this.WorkflowService.canAddNewConnection(edge)) {
        this.workflow.removeEdge(edge);
        jsPlumb.detach(jsPlumbEvent.connection);
      }
    });

    jsPlumb.bind('connectionDetached', (jsPlumbEvent, originalEvent) => {
      if (this.workflow) {
        const edge = this.workflow.getEdgeById(jsPlumbEvent.connection.getParameter('edgeId'));
        if (edge && jsPlumbEvent.targetEndpoint.isTarget && jsPlumbEvent.sourceEndpoint.isSource && originalEvent) {
          this.workflow.removeEdge(edge);
        }
      }
    });

    jsPlumb.bind('connectionMoved', (jsPlumbEvent) => {
      const edge = this.workflow.getEdgeById(jsPlumbEvent.connection.getParameter('edgeId'));
      if (edge) {
        this.workflow.removeEdge(edge);
      }
    });

    jsPlumb.bind('connectionDrag', (connection) => {
      this.startDragging(connection);
    });

    jsPlumb.bind('connectionDragStop', () => {
      this.stopDragging();
    });

    jsPlumb.bind('connectionAborted', (connection, originalEvent) => {
      if ($(originalEvent.target).closest('core-canvas').length === 0 ||
          originalEvent.target.classList.contains('output')) {
        return;
      }
      this.onConnectionAbort({newNodeData: {
        x: originalEvent.clientX,
        y: originalEvent.clientY,
        endpoint: connection.endpoints[0]
      }});
    });
  }

  setZoom(zoom) {
    jsPlumb.setZoom(zoom);
  }

  setWorkflow(workflow) {
    this.workflow = workflow;
    this.edges = workflow.getEdges();
    this.nodes = workflow.getNodes();
  }

  setNewNodeData(newNodeData) {
    this.newNodeData = newNodeData;
  }

  setOnConnectionAbortFunction(fn) {
    this.onConnectionAbort = fn;
  }

  setMouseOverOnPortFunction(fn) {
    this.onMouseOver = fn;
  }

  setMouseOutOnPortFunction(fn) {
    this.onMouseOut = fn;
  }

  setMouseClickOnPortFunction(fn) {
    this.onMouseClick = fn;
  }

  reset() {
    jsPlumb.deleteEveryEndpoint();
    jsPlumb.unbind('connection');
    jsPlumb.unbind('connectionDetached');
    jsPlumb.unbind('connectionMoved');
    jsPlumb.unbind('connectionDrag');
    jsPlumb.unbind('connectionAborted');
    jsPlumb.setContainer(this.container);
    this.bindEvents();
  }

  render() {
    this.reset();
    this.renderPorts(this.getNodesToRender(this.nodes));
    this.renderEdges(this.getEdgesToRender(this.edges));
    jsPlumb.repaintEverything();
  }

  getNodesToRender(inputNodes) {
    const nodes = Object.assign({}, inputNodes);
    if (this.newNodeData && this.newNodeData.nodeId) {
      const node = Object.assign({}, NEW_NODE_NODE);
      node.input[0].typeQualifier = [...this.newNodeData.typeQualifier];
      nodes[node.id] = node;
    }
    return nodes;
  }

  getEdgesToRender(inputEdges) {
    const edges = Object.assign({}, inputEdges);
    if (this.newNodeData && this.newNodeData.nodeId) {
      const edge = Object.assign({}, NEW_NODE_EDGE);
      edge.startNodeId = this.newNodeData.nodeId;
      edge.startPortId = this.newNodeData.portIndex;
      edges[edge.id] = edge;
    }
    return edges;
  }

  renderPorts(nodes) {
    for (const nodeId of Object.keys(nodes)) {
      const element = this.container.querySelector(`#node-${nodeId}`);
      const node = nodes[nodeId];
      this.renderOutputPorts(element, node.originalOutput, nodes[nodeId]);
      this.renderInputPorts(element, node.input, nodes[nodeId]);
    }
  }

  renderOutputPorts(element, ports, node) {
    ports.forEach((port) => {
      const reportEntityId = node.getResult(port.index);
      const hasReport = this.Report.hasReportEntity(reportEntityId);

      const style = this.GraphStyleService.getStyleForPort(port);

      const jsPlumbPort = jsPlumb.addEndpoint(element, style, {
        anchor: POSITION_MAP.OUTPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.bind('mouseover', (endpoint) => {
        this.onMouseOver(endpoint.canvas, port);
        if (endpoint.isSource && !this.isConnectionDragged) {
          jsPlumbPort.addClass('port-active');
        }
      });

      jsPlumbPort.bind('mouseout', () => {
        this.onMouseOut();
        if (jsPlumbPort.id !== this.selectedPortId) {
          jsPlumbPort.removeClass('port-active');
        }
      });

      jsPlumbPort.bind('click', (reference) => {
        if (hasReport) {
          this.selectedPortId = jsPlumbPort.id;
          this.onMouseClick({reference, port});
          this.removeActivePortClasses();
          jsPlumbPort.addClass('port-active');
        }
      });

      const portType = this.GraphStyleService.getOutputTypeFromQualifier(port.typeQualifier[0]);
      const isDataOutput = portType === 'default';

      if (isDataOutput) {
        jsPlumbPort.addClass('dataframe-output-port');
      }

      if (hasReport) {
        jsPlumbPort.addClass('has-report sa sa-chart');
      }

      jsPlumbPort.addClass('output');
      jsPlumbPort.addClass(portType);
      jsPlumbPort.setParameter('portIndex', port.index);
      jsPlumbPort.setParameter('nodeId', node.id);
    });
  }

  startDragging(connection) {
    this.isConnectionDragged = true;
    this.GraphStyleService.enablePortHighlighting(this.nodes, connection.endpoints[0]);
  }

  stopDragging() {
    this.isConnectionDragged = false;
    this.GraphStyleService.disablePortHighlighting(this.nodes);
  }

  renderInputPorts(node, ports) {
    ports.forEach((port) => {
      const style = this.GraphStyleService.getStyleForPort(port);
      const portType = this.GraphStyleService.getOutputTypeFromQualifier(port.typeQualifier[0]);

      const jsPlumbPort = jsPlumb.addEndpoint(node, style, {
        anchor: POSITION_MAP.INPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.bind('mouseover', (endpoint) => {
        this.onMouseOver(endpoint.canvas, port);
        if (!this.isConnectionDragged) {
          jsPlumbPort.addClass('port-active');
        }
      });

      jsPlumbPort.bind('mouseout', () => {
        this.onMouseOut();
        jsPlumbPort.removeClass('port-active');
      });

      jsPlumbPort.addClass(portType);
      jsPlumbPort.setParameter('portIndex', port.index);
    });
  }

  renderEdges(edges) {
    jsPlumb.detachEveryConnection();
    const outputPrefix = 'output';
    const inputPrefix = 'input';

    for (const id of Object.keys(edges)) {
      const edge = edges[id];
      const detachable = (edge.id === NEW_NODE_EDGE.id) ? false : this.isEditable;
      const connection = jsPlumb.connect({
        uuids: [
          `${outputPrefix}-${edge.startPortId}-${edge.startNodeId}`,
          `${inputPrefix}-${edge.endPortId}-${edge.endNodeId}`
        ],
        detachable: detachable
      });
      connection.setParameter('edgeId', edge.id);
    }
  }

  removeNodes(nodesIdsToRemove) {
    nodesIdsToRemove.forEach((nodeId) => {
      let node = this.GraphStyleService.getNodeElementById(nodeId);
      jsPlumb.remove(node);
    });
  }

  removeActivePortClasses() {
    jsPlumb.selectEndpoints().each((endpoint) => {
      if (endpoint.id !== this.selectedPortId) {
        endpoint.removeClass('port-active');
      }
    });
  }

}

export default AdapterService;
