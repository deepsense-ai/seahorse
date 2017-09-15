'use strict';

import GraphNodeController from './graph-node.controller.js';
import GraphPanelNodeTemplate from './graph-node.html';
import './graph-node.less';

const GraphPanelNodeComponent = {
  bindings: {
    isSelected: '<',
    node: '<'
  },
  controller: GraphNodeController,
  templateUrl: GraphPanelNodeTemplate
};

export default GraphPanelNodeComponent;
