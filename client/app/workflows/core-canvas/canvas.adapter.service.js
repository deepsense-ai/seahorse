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
    for (const nodeId of Object.keys(nodes)) {
      const element = this.container.querySelector(`#node-${nodeId}`);
      const node = nodes[nodeId];
      this.renderOutputPorts(element, node.output, nodes[nodeId]);
      this.renderInputPorts(element, node.input);
    }
  }

  renderOutputPorts (element, ports, node) {
    ports.forEach((port) => {
      const style = Object.create(OUTPUT_STYLE);

      if (!this.isEditable) {
        style.isSource = false;
      }

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

    for (const id of Object.keys(edges)) {
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

export default AdapterService;
