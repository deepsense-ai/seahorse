'use strict';

import PortStatusTooltipController from './port-status-tooltip.controller.js';
import PortStatusTooltipTemplate from './port-status-tooltip.html';
import './port-status-tooltip.less';

const PortStatusTooltipComponent = {
  bindings: {
    portObject: '<'
  },
  controller: PortStatusTooltipController,
  templateUrl: PortStatusTooltipTemplate
};

export default PortStatusTooltipComponent;
