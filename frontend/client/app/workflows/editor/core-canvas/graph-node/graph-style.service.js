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

const ESTIMATOR = ['ai.deepsense.deeplang.doperables.Estimator'];
const TRANSFORMER = ['ai.deepsense.deeplang.doperables.Transformer'];
const EVALUATOR = ['ai.deepsense.deeplang.doperables.Evaluator'];
const DATAFRAME = ['ai.deepsense.deeplang.doperables.dataframe.DataFrame'];

// output types
const DOT_ENDPOINT = 'Dot';
const RECTANGLE_ENDPOINT = 'Rectangle';

// colors
const ESTIMATOR_COLOR = '#62b77a';
const DEFAULT_COLOR = '#0197c8';
const TRANSFORMER_COLOR = '#021f4e';
const EVALUATOR_COLOR = '#afaf3c';
const READ_ONLY_COLOR = '#076575';

// paint styles
const DEFAULT_PAINT_STYLE = {
  fill: DEFAULT_COLOR
};

const STYLES_MAP = {
  'estimator': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: ESTIMATOR_COLOR
  },
  'transformer': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: TRANSFORMER_COLOR
  },
  'evaluator': {
    stroke: 'transparent',
    strokeWidth: 5,
    fill: EVALUATOR_COLOR
  },
  'dataframe': {
    stroke: 'transparent',
    strokeWidth: 3,
    fill: DEFAULT_COLOR
  },
  'readonly': {
    stroke: 'transparent',
    strokeWidth: 3,
    fill: READ_ONLY_COLOR
  }
};

const CONNECTOR_STYLE_DEFAULT = {
  strokeWidth: 2
};

const CONNECTOR_HOVER_STYLE = {
  endpoint: 'Dot',
  stroke: DEFAULT_COLOR
};

// port basic settings
const OUTPUT_STYLE = {
  endpoint: DOT_ENDPOINT,
  isSource: true,
  connector: ['Bezier', {
    curviness: 75
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
  constructor(OperationsHierarchyService, Operations) {
    'ngInject';

    this.OperationsHierarchyService = OperationsHierarchyService;
    this.Operations = Operations;
  }

  getStyleForPort(port) {
    let portStyle = {};

    if (port.type === 'input') {
      portStyle = Object.create(INPUT_STYLE);
    } else {
      portStyle = Object.create(OUTPUT_STYLE);
      const isSource = this.canPortBeSource(port);

      const color = isSource ? this.getPortPaintStyleForQualifier(port.typeQualifier[0]).fill : READ_ONLY_COLOR;
      const connectorStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        stroke: color
      });

      const connectorHoverStyle = Object.assign({}, CONNECTOR_STYLE_DEFAULT, {
        stroke: color
      });

      portStyle.isSource = isSource;
      portStyle.connectorStyle = connectorStyle;
      portStyle.connectorHoverStyle = connectorHoverStyle;
    }

    if (port.typeQualifier.length === 1) {
      portStyle.endpoint = this.getPortEndingTypeForQualifier(port.typeQualifier[0]);
      portStyle.paintStyle = this.getPortPaintStyleForQualifier(port.typeQualifier[0]);
    }

    return portStyle;
  }

  canPortBeSource(port) {
    const catalog = this.Operations.getCatalog();
    const filter = this.Operations.getFilterForTypeQualifier(port.typeQualifier);
    const categories = this.Operations.filterCatalog(catalog, filter);

    return categories.length > 0;
  }

  getPortEndingTypeForQualifier(typeQualifier) {
    const type = this.getOutputTypeFromQualifier(typeQualifier);

    if (type === 'dataframe' || type === 'readonly') {
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
    } else if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, DATAFRAME)) {
      return 'dataframe';
    } else {
      return 'readonly';
    }
  }

  // TODO this method will need refactor because some of its logic is repeated in controller
  enablePortHighlighting(nodes, sourceEndpoint) {
    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = nodes[sourceNodeId].output[sourcePortIndex];
    const outputType = this.getOutputTypeFromQualifier(sourcePort.typeQualifier[0]);

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
      endpoint.addClass('matching-port');
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
      endpoint.removeClass('matching-port');
    });
  }

  getNodeElementById(id) {
    return document.querySelector(`#node-${id}`);
  }

}

export default GraphStyleService;

