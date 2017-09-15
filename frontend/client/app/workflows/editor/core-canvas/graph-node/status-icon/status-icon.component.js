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
