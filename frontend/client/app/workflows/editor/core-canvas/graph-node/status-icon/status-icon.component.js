'use strict';

import StatusIconTemplate from './status-icon.html';
import './status-icon.less';

import {specialOperations} from 'APP/enums/special-operations.js';

const CSS_CLASSES_MAP = {
  'status_completed': {
    status: 'completed',
    icon: 'fa-check'
  },
  'status_running': {
    status: 'running',
    icon: 'fa-cog fa-spin'
  },
  'status_queued': {
    status: 'queued',
    icon: 'fa-clock-o'
  },
  'status_aborted': {
    status: 'aborted',
    icon: 'fa-exclamation'
  },
  'status_failed': {
    status: 'failed',
    icon: 'fa-ban'
  },
  'error': {
    status: 'failed',
    icon: 'fa-exclamation'
  },
  unknown: {
    status: 'unknown',
    icon: 'fa-question'
  }
};

const StatusIconComponent = {
  bindings: {
    node: '<'
  },
  templateUrl: StatusIconTemplate,
  controller: class StatusIconController {
    constructor($scope) {
      'ngInject';

      if (this.nodeType === 'unknown') {
        this.tooltipMessage = this.node.description;
      }

      $scope.$watch(() => this.node.knowledgeErrors, (newValue) => {
        if (this.nodeType !== 'unknown') {
          const errors = this.node.getFancyKnowledgeErrors();
          this.tooltipMessage = errors || '';
        }
      });
    }

    getCssClasses() {
      if (this.node.operationId === specialOperations.UNKNOWN_OPERATION) {
        return CSS_CLASSES_MAP.unknown;
      }

      if (this.node.state && this.node.state.status && this.node.state.status !== 'status_draft') {
        return CSS_CLASSES_MAP[this.node.state.status];
      } else if (this.node.knowledgeErrors.length > 0) {
        return CSS_CLASSES_MAP.error;
      } else {
        return null;
      }
    }

  }
};

export default StatusIconComponent;
