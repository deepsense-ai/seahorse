const ESTIMATOR = ['io.deepsense.deeplang.doperables.Estimator'];
const TRANSFORMER = ['io.deepsense.deeplang.doperables.Transformer'];
const EVALUATOR = ['io.deepsense.deeplang.doperables.Evaluator'];

// output types
const DOT_ENDPOINT = 'Dot';
const RECTANGLE_ENDPOINT = 'Rectangle';

// colors
const SEAHORSE_SEA_GREEN = '#34b5ba';
const SEAHORSE_BLUE = '#00b1eb';
const SEAHORSE_MADISON = '#2f4050';
const SEAHORSE_ALLPORTS = '#1e6c71';

// paint styles
const DEFAULT_PAINT_STYLE = {
  fillStyle: SEAHORSE_BLUE
};

const STYLES_MAP = {
  'estimator' : {
    fillStyle: SEAHORSE_SEA_GREEN
  },
  'transformer' : {
    fillStyle: SEAHORSE_MADISON
  },
  'evaluator' : {
    fillStyle: SEAHORSE_ALLPORTS
  },
  'default': {
    fillStyle: SEAHORSE_BLUE
  }
};

const CONNECTOR_STYLE_DEFAULT = {
  lineWidth: 2,
  outlineWidth: 2,
  strokeStyle: SEAHORSE_BLUE,
};

const CONNECTOR_HOVER_STYLE = {
  outlineColor: 'white',
  strokeStyle: SEAHORSE_BLUE
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

      const color = this.getPortPaintStyleForQualifier(port.typeQualifier[0]).fillStyle;
      const connectorStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        strokeStyle: color
      });

      const connectorHoverStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        strokeStyle: color
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
    const isDataFrame = typeQualifier.substr(
        typeQualifier.lastIndexOf('.') + 1, typeQualifier.length
      ).toLowerCase() === 'dataframe';

    if (isDataFrame) {
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

}

export default GraphStyleService;



