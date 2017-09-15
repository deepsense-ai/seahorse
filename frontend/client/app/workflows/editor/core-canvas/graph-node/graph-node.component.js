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

import './graph-node.less';
import './datasource-node/datasource-node.less';

import {specialOperations} from 'APP/enums/special-operations.js';
import {actionIcons} from './action-icons.js';
import {datasourceNode} from './datasource-node/datasource-node.js';
import standardNodeTpl from './graph-node.html';

const nodeTypes = {
  ACTION: 'action',
  SOURCE_OR_SINK: 'source-or-sink',
  DATASOURCE: 'datasource',
  UNKNOWN: 'unknown',
  STANDARD: 'standard'
};

const GraphPanelNodeComponent = {
  bindings: {
    isSelected: '<',
    node: '<'
  },
  template: `
    <div ng-include="$ctrl.templateUrl"></div>
  `,
  controller: class GraphNodeController {
    constructor($rootScope, $element, WorkflowService, GraphStyleService, datasourcesService, DatasourcesPanelService) {
      'ngInject';

      this.$rootScope = $rootScope;
      this.$element = $element;
      this.WorkflowService = WorkflowService;
      this.GraphStyleService = GraphStyleService;
      this.datasourcesService = datasourcesService;
      this.DatasourcesPanelService = DatasourcesPanelService;

      this.actionIcon = actionIcons[this.node.operationId];
      this.nodeType = this.getNodeType();
      this.templateUrl = standardNodeTpl;

      if (this.nodeType === nodeTypes.DATASOURCE) {
        Object.assign(this, datasourceNode);
      }
    }

    $postLink() {
      this.$element.on('click', ($event) => this.broadcastEvent('GraphNode.CLICK', $event));
      this.$element.on('mousedown', ($event) => this.broadcastEvent('GraphNode.MOUSEDOWN', $event));
      this.$element.on('mouseup', ($event) => this.broadcastEvent('GraphNode.MOUSEUP', $event));

      this.postLink();
    }

    $onDestroy() {
      this.$element.off('click', this.broadcastEvent);
      this.$element.off('mousedown', this.broadcastEvent);
      this.$element.off('mouseup', this.broadcastEvent);

      this.onDestroy();
    }

    postLink() {
      // TO OVERRIDE
    }

    onDestroy() {
      // TO OVERRIDE
    }

    onConstructor() {
      // TO OVERRIDE
    }

    broadcastEvent(eventType, $event) {
      this.$rootScope.$broadcast(eventType, {
        originalEvent: $event,
        selectedNode: this.node
      });
    }

    getNodeType() {
      const operationId = this.node.operationId;
      if (Object.values(specialOperations.ACTIONS).includes(operationId)) {
        return nodeTypes.ACTION;
      } else if (operationId === specialOperations.CUSTOM_TRANSFORMER.SINK ||
        operationId === specialOperations.CUSTOM_TRANSFORMER.SOURCE) {
        return nodeTypes.SOURCE_OR_SINK;
      } else if (Object.values(specialOperations.DATASOURCE).includes(operationId)) {
        return nodeTypes.DATASOURCE;
      } else if (operationId === specialOperations.UNKNOWN_OPERATION) {
        return nodeTypes.UNKNOWN;
      } else {
        return nodeTypes.STANDARD;
      }
    }

    isEditable() {
      return this.WorkflowService.isWorkflowEditable();
    }

    isOwner() {
      return this.WorkflowService.isCurrentUserOwnerOfCurrentWorkflow();
    }

    getBorderCssClass() {
      let typeQualifier;

      if (this.node.operationId === specialOperations.UNKNOWN_OPERATION) {
        return 'border-unknown';
      }

      if (this.node.input && this.node.input.length === 1) {
        typeQualifier = this.node.input[0].typeQualifier[0];
      } else if (this.node.originalOutput && this.node.originalOutput.length === 1) {
        typeQualifier = this.node.originalOutput[0].typeQualifier[0];
      }
      const type = this.GraphStyleService.getOutputTypeFromQualifier(typeQualifier);

      return `border-${type}`;
    }

  }
};

export default GraphPanelNodeComponent;
