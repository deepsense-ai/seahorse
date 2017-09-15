import jsPlumb from 'jsplumb';

const ESTIMATOR = ['io.deepsense.deeplang.doperables.Estimator'];
const TRANSFORMER = ['io.deepsense.deeplang.doperables.Transformer'];
const EVALUATOR = ['io.deepsense.deeplang.doperables.Evaluator'];

// output types
const DOT_ENDPOINT = 'Dot';
const RECTANGLE_ENDPOINT = 'Rectangle';

// colors
const SEAHORSE_SEA_GREEN = '#34b5ba';
const SEAHORSE_BLUE = '#0197c8';
const SEAHORSE_MADISON = '#021f4e';
const SEAHORSE_ALLPORTS = '#1e6d71';

// paint styles
const DEFAULT_PAINT_STYLE = {
  fill: SEAHORSE_BLUE
};

const STYLES_MAP = {
  'estimator': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: SEAHORSE_SEA_GREEN
  },
  'transformer': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: SEAHORSE_MADISON
  },
  'evaluator': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: SEAHORSE_ALLPORTS
  },
  'default': {
    stroke: 'transparent',
    strokeWidth: 3,
    fill: SEAHORSE_BLUE
  }
};

const CONNECTOR_STYLE_DEFAULT = {
  strokeWidth: 2,
};

const CONNECTOR_HOVER_STYLE = {
  endpoint: 'Dot',
  stroke: SEAHORSE_BLUE
};

// port basic settings
const OUTPUT_STYLE = {
  endpoint: RECTANGLE_ENDPOINT,
  isSource: true,
  connector: ['Bezier', {
    curviness: 50
  }],
  connectorStyle: CONNECTOR_STYLE_DEFAULT,
  connectorHoverStyle: CONNECTOR_HOVER_STYLE,
  maxConnections: -1,
  paintStyle: DEFAULT_PAINT_STYLE
};

const INPUT_STYLE = {
  endpoint: RECTANGLE_ENDPOINT,
  dropOptions: {
    hoverClass: 'hover',
    activeClass: 'active'
  },
  isTarget: true,
  maxConnections: 1,
  paintStyle: DEFAULT_PAINT_STYLE
};

class GraphStyleService {
  constructor(OperationsHierarchyService) {
    'ngInject';
    this.OperationsHierarchyService = OperationsHierarchyService;
  }

  getStyleForPort(port) {
    let portStyle = {};

    if (port.type === 'input') {
      portStyle = Object.create(INPUT_STYLE);
    } else {
      portStyle = Object.create(OUTPUT_STYLE);

      const color = this.getPortPaintStyleForQualifier(port.typeQualifier[0]).fill;
      const connectorStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        stroke: color
      });

      const connectorHoverStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        stroke: color
      });

      portStyle.connectorStyle = connectorStyle;
      portStyle.connectorHoverStyle = connectorHoverStyle;
    }

    if (port.typeQualifier.length === 1) {
      portStyle.endpoint = this.getPortEndingTypeForQualifier(port.typeQualifier[0]);
      portStyle.paintStyle = this.getPortPaintStyleForQualifier(port.typeQualifier[0]);
    }

    return portStyle;
  }

  getPortEndingTypeForQualifier(typeQualifier) {
    const type = this.getOutputTypeFromQualifier(typeQualifier);

    if (type === 'default') {
      return DOT_ENDPOINT;
    } else {
      return RECTANGLE_ENDPOINT;
    }
  }

  getPortPaintStyleForQualifier(typeQualifier) {
    const outputType = this.getOutputTypeFromQualifier(typeQualifier);
    return STYLES_MAP[outputType];
  }

  getOutputTypeFromQualifier(typeQualifier) {
    if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, ESTIMATOR)) {
      return 'estimator';
    } else if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, TRANSFORMER)) {
      return 'transformer';
    } else if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, EVALUATOR)) {
      return 'evaluator';
    } else {
      return 'default';
    }
  }

  // TODO this method will need refactor because some of its logic is repeated in controller
  enablePortHighlighting(nodes, sourceEndpoint) {
    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = nodes[sourceNodeId].output[sourcePortIndex];

    const outputType = this.getOutputTypeFromQualifier(sourcePort.typeQualifier[0]);
    const cssClass = `matching-port-${outputType}`;

    const endpointTargets =
      _.chain(nodes)
        .values()
        .map((node) => {
          const nodeElement = this.getNodeElementById(node.id);

          return {
            nodeInput: node.input,
            nodeEndpoints: jsPlumb.getEndpoints(nodeElement).filter(endpoint => endpoint.isTarget)
          };
        })
        .map(nodesInfo => {
          const {nodeInput, nodeEndpoints} = nodesInfo;

          return nodeEndpoints.map((endpoint) => {
            const portIndex = endpoint.getParameter('portIndex');
            const port = nodeInput[portIndex];

            return {
              endpoint,
              port
            };
          });
        })
        .flatMap()
        .filter(endpointInfo => {
          const {port, endpoint} = endpointInfo;
          const typesMatch = sourcePort
            .typeQualifier
            .some(typeQualifier => this.OperationsHierarchyService.IsDescendantOf(typeQualifier, port.typeQualifier));

          // types match && there cannot be any edge attached && attaching an edge to the same node is forbidden
          return typesMatch && endpoint.connections.length === 0 && port.nodeId !== sourceNodeId;
        })
        .map(endpointInfo => endpointInfo.endpoint)
        .value();

    endpointTargets.forEach((endpoint) => {
      endpoint.addClass(cssClass);
    });
  }

  disablePortHighlighting(nodes) {
    const endpointTargets = _.chain(nodes)
      .map((node) => {
        let nodeEl = this.getNodeElementById(node.id);
        return jsPlumb.getEndpoints(nodeEl);
      })
      .values()
      .flatMap()
      .value();

    endpointTargets.forEach((endpoint) => {
      endpoint.removeClass('matching-port-default matching-port-estimator matching-port-evaluator matching-port-transformer');
    });
  }

  getNodeElementById(id) {
    return document.querySelector(`#node-${id}`);
  }

}

export default GraphStyleService;



