require('imports?this=>window!script!jsplumb');

//TODO move externally?
const OUTPUT_STYLE = {
  endpoint: 'Dot',
  isSource: true,
  connector: ['Bezier', {
    curviness: 75
  }],
  maxConnections: -1
};

//TODO move externally?
const INPUT_STYLE = {
  endpoint: 'Rectangle',
  dropOptions: {
    hoverClass: 'hover',
    activeClass: 'active'
  },
  isTarget: true,
  maxConnections: 1
};

const POSITION_MAP = {
  OUTPUT: {
    left: 'BottomLeft',
    center: 'BottomCenter',
    right: 'BottomRight'
  },
  INPUT: {
    left: 'TopLeft',
    center: 'TopCenter',
    right: 'TopRight'
  }
};

class AdapterService {
  /*@ngInject*/
  constructor(WorkflowService, $rootScope, DeepsenseCycleAnalyser) {
    this.WorkflowService = WorkflowService;
    this.DeepsenseCycleAnalyser = DeepsenseCycleAnalyser;
    this.$rootScope = $rootScope;
  }

  initialize(container) {
    this.container = container;
  }

  bindEvents() {
    // Edge management
    jsPlumb.bind('connection', (info, originalEvent) => {
      if (!originalEvent) {
        return;
      }

      const data = {
        from: {
          nodeId: info.sourceId.slice('node-'.length),
          portIndex: info.sourceEndpoint.getParameter('portIndex')
        },
        to: {
          nodeId: info.targetId.slice('node-'.length),
          portIndex: info.targetEndpoint.getParameter('portIndex')
        }
      };
      const edge = this.workflow.createEdge(data);
      this.workflow.addEdge(edge);
      //TODO remove, as it shouldn't be here
      this.WorkflowService.updateEdgesStates();
      info.connection.setParameter('edgeId', edge.id);
      if (this.DeepsenseCycleAnalyser.cycleExists(this.workflow)) {
        this.workflow.removeEdge(edge);
        jsPlumb.detach(info.connection);
      }
    });

    jsPlumb.bind('connectionDetached', (info, originalEvent) => {
      if (this.workflow) {
        const edge = this.workflow.getEdgeById(info.connection.getParameter('edgeId'));
        if (edge && info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
          this.workflow.removeEdge(edge);
        }
      }
    });

    jsPlumb.bind('connectionMoved', (info) => {
      const edge = this.workflow.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge) {
        this.workflow.removeEdge(edge);
      }
    });

    jsPlumb.bind('connectionDrag', (connection) => {
      //TODO Add highlight 
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

  reset() {
    jsPlumb.setContainer(this.container);
    jsPlumb.deleteEveryEndpoint();
    jsPlumb.unbind('connection');
    jsPlumb.unbind('connectionDetached');
    jsPlumb.unbind('connectionMoved');
    jsPlumb.unbind('connectionDrag');
    this.bindEvents();
  }

  render() {
    this.reset();
    this.renderPorts(this.nodes);
    this.renderEdges(this.edges);
    jsPlumb.repaintEverything();
  }

  renderPorts(nodes) {
    for (const nodeId in nodes) {
      if (nodes.hasOwnProperty(nodeId)) {
        const nodeElement = this.container.querySelector(`#node-${nodeId}`);
        const nodeObject = nodes[nodeId];
        this.renderOutputPorts(nodeElement, nodeObject.output, nodes[nodeId]);
        this.renderInputPorts(nodeElement, nodeObject.input);
      }
    }
  }

  renderOutputPorts (nodeElement, ports, nodeObj) {
    ports.forEach((port) => {
      const style = Object.create(OUTPUT_STYLE);

      if (!this.isEditable) {
        style.isSource = false;
      }

      const jsPlumbPort = jsPlumb.addEndpoint(nodeElement, style, {
        anchor: POSITION_MAP.OUTPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.setParameter('portIndex', port.index);
      jsPlumbPort.setParameter('nodeId', nodeObj.id);
    });
  };

  renderInputPorts(node, ports) {
    ports.forEach((port) => {
      const jsPlumbPort = jsPlumb.addEndpoint(node, INPUT_STYLE, {
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

    for (const id in edges) {
      if (edges.hasOwnProperty(id)) {
        const edge = edges[id];
        const connection = jsPlumb.connect({
          uuids: [
            `${outputPrefix}-${edge.startPortId}-${edge.startNodeId}`,
            `${inputPrefix}-${edge.endPortId}-${edge.endNodeId}`
          ],
          detachable: this.isEditable
        });
        connection.setParameter('edgeId', edge.id);
      }
    }
  }
}

export default AdapterService;
