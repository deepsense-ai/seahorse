require('imports?this=>window!script!jsplumb');

const POSITION_MAP = {
  OUTPUT: {
    left: 'BottomLeft',
    center: 'BottomCenter',
    right: 'BottomRight'
  },
  INPUT: {
    left: [0.25, 0, -1, 0], // Top middle left part
    center: 'TopCenter',
    right: [0.75, 0, -1, 0] // Top middle right part
  }
};

const NEW_NODE_NODE = {
  id: 'new-node',
  input: [{
    id: 'input-0-new-node',
    portPosition: 'center',
    index: 0,
    typeQualifier: ['dataframe']
  }],
  output: []
};

const NEW_NODE_EDGE = {
  id: 'new-node-edge',
  endNodeId: NEW_NODE_NODE.id,
  endPortId: 0
};

class AdapterService {
  /*@ngInject*/
  constructor(WorkflowService, $rootScope, DeepsenseCycleAnalyser, GraphStyleService) {
    this.WorkflowService = WorkflowService;
    this.DeepsenseCycleAnalyser = DeepsenseCycleAnalyser;
    this.$rootScope = $rootScope;
    this.GraphStyleService = GraphStyleService;
  }

  initialize(container) {
    this.container = container;
  }

  bindEvents() {
    // Edge management
    jsPlumb.bind('connection', (jsPlumbEvent, originalEvent) => {
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
      if (this.DeepsenseCycleAnalyser.cycleExists(this.workflow)) {
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
      //console.warn(connection.endpoints[0]);
    });

    jsPlumb.bind('connectionAborted', (connection, originalEvent) => {
      this.onConnectionAbort({newNodeData: {
        x: (originalEvent.layerX - originalEvent.x) * -1,
        y: (originalEvent.layerY - originalEvent.y + 60) * -1,
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

  handleConnectionAbort(fn) {
    this.onConnectionAbort = fn;
  }

  reset() {
    jsPlumb.setContainer(this.container);
    jsPlumb.deleteEveryEndpoint();
    jsPlumb.unbind('connection');
    jsPlumb.unbind('connectionDetached');
    jsPlumb.unbind('connectionMoved');
    jsPlumb.unbind('connectionDrag');
    jsPlumb.unbind('connectionAborted');
    this.bindEvents();
  }

  render() {
    this.reset();
    this.renderPorts(this.getNodesToRender(this.nodes));
    this.renderEdges(this.getEdgesToRender(this.edges));
    jsPlumb.repaintEverything();
  }

  getNodesToRender(inputNodes) {
    const nodes = JSON.parse(JSON.stringify(inputNodes));
    if (this.newNodeData && this.newNodeData.endpoint) {
      nodes[NEW_NODE_NODE.id] = Object.assign({}, NEW_NODE_NODE);
    }
    return nodes;
  }

  getEdgesToRender(inputEdges) {
    const edges = JSON.parse(JSON.stringify(inputEdges));
    if (this.newNodeData && this.newNodeData.endpoint) {
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
      this.renderOutputPorts(element, node.output, nodes[nodeId]);
      this.renderInputPorts(element, node.input, nodes[nodeId]);
    }
  }

  renderOutputPorts(element, ports, node) {
    ports.forEach((port) => {
      const style = this.GraphStyleService.getStyleForPort(port);

      const jsPlumbPort = jsPlumb.addEndpoint(element, style, {
        anchor: POSITION_MAP.OUTPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.setParameter('portIndex', port.index);
      jsPlumbPort.setParameter('nodeId', node.id);
    });
  };

  renderInputPorts(node, ports) {
    ports.forEach((port) => {
      const style = this.GraphStyleService.getStyleForPort(port);

      const jsPlumbPort = jsPlumb.addEndpoint(node, style, {
        anchor: POSITION_MAP.INPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.setParameter('portIndex', port.index);
    });
  };

  renderEdges(edges) {
    jsPlumb.detachEveryConnection();
    const outputPrefix = 'output';
    const inputPrefix = 'input';

    for (const id of Object.keys(edges)) {
      const edge = edges[id];
      const detachable = (edge.id === NEW_NODE_EDGE.id) ? false: this.isEditable;
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
}

export default AdapterService;
