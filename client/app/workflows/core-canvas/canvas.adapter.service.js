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
  constructor() {
    this.container = undefined;
  }

  initialize(container) {
    this.container = container;
    jsPlumb.setContainer(container);
  }

  setZoom(zoom) {
    jsPlumb.setZoom(zoom);
  }

  setItems(items) {
    this.items = items;
  }

  setEdges(edges) {
    this.edges = edges;
  }

  render() {
    this.renderPorts(this.items);
    this.renderEdges(this.edges);
    jsPlumb.repaintEverything();
  }

  renderPorts(ports) {
    for (const nodeId in ports) {
      if (ports.hasOwnProperty(nodeId)) {
        const nodeElement = this.container.querySelector(`#node-${nodeId}`);
        const nodeObject = ports[nodeId];
        this.addOutputPoints(nodeElement, nodeObject.output, ports[nodeId]);
        this.addInputPoints(nodeElement, nodeObject.input);
      }
    }
  }

  //TODO rename
  addOutputPoints (nodeElement, ports, nodeObj) {
    ports.forEach((port) => {
      const jsPlumbPort = jsPlumb.addEndpoint(nodeElement, OUTPUT_STYLE, {
        anchor: POSITION_MAP.OUTPUT[port.portPosition],
        uuid: port.id
      });

      jsPlumbPort.setParameter('portIndex', port.index);
      jsPlumbPort.setParameter('nodeId', nodeObj.id);
    });
  };

  //TODO rename
  addInputPoints(node, ports) {
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
            outputPrefix + '-' + edge.startPortId + '-' + edge.startNodeId,
            inputPrefix + '-' + edge.endPortId + '-' + edge.endNodeId
          ],
          detachable: true
        });
        connection.setParameter('edgeId', edge.id);
      }
    }
  }
}

export default AdapterService;
