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

import {specialOperations} from 'APP/enums/special-operations.js';

class GraphNodesService {
  /* @ngInject */
  constructor($q, $rootScope, $log, DeepsenseNodeParameters, Operations, UUIDGenerator, WorkflowsApiClient) {
    _.assign(this, {
      $q,
      $rootScope,
      $log,
      DeepsenseNodeParameters,
      Operations,
      UUIDGenerator,
      WorkflowsApiClient
    });
  }

  getNodeParameters(node) {
    let deferred = this.$q.defer();

    if (node.hasParameters()) {
      node.refreshParameters(this.DeepsenseNodeParameters);
      deferred.resolve(node, 'sync');
    } else {
      this.Operations.getWithParams(node.operationId)
        .then(operationData => {
          this.$rootScope.$applyAsync(() => {
            node.setParameters(operationData.parameters, this.DeepsenseNodeParameters);
            deferred.resolve(node, 'async');
          });
        }, (error) => {
          this.$log.error('operation fetch error', error);
          deferred.reject(error);
        });
    }
    return deferred.promise;
  }

  createNodeAndAdd(workflow, params) {
    params.id = this.UUIDGenerator.generateUUID();
    let node = workflow.createNode(params);
    workflow.addNode(node);
    return node;
  }

  cloneNodes(workflow, nodes) {
    let cloningNodeIds = nodes.map(node => node.id);
    let clones = _.map(nodes, node => this._cloneNode(workflow, node));

    // clone connections with saving hierarchy
    _.forEach(nodes, (node, index) => {
      this._setEdgeConnectionFromClone(node, clones, cloningNodeIds);
      workflow.cloneEdges(node, clones[index]);
    });
    return clones;
  }

  // TODO This (and cloneNodes) should probably be part of workflow class and added to project deepsense-graph-model
  _cloneNode(workflow, node) {
    let operation = this.Operations.get(node.operationId);
    let offset = {
      x: 255,
      y: 0
    };
    let nodeClone = _.cloneDeep(node);
    let newNodeId = this.UUIDGenerator.generateUUID();
    let nodeParams = angular.merge(
      nodeClone, {
        'id': newNodeId,
        'operation': operation,
        'x': node.x - offset.x >= 0 ? node.x - offset.x : node.x,
        'y': node.y - offset.y >= 0 ? node.y - offset.y : node.y,
        'uiName': nodeClone.uiName ? nodeClone.uiName += ' copy' : ''
      }
    );

    const createdNode = workflow.createNode(nodeParams);
    createdNode.parametersValues = angular.copy(node.parameters.serialize());

    if (Object.values(specialOperations.NOTEBOOKS).includes(node.operationId)) {
      this.WorkflowsApiClient.cloneNotebookNode(workflow.id, node.id, newNodeId).then(() => {
        return workflow.addNode(createdNode);
      }, () => {
        this.NotificationService.showError({
          title: 'Error',
          message: 'There was an error during copying content of the notebook! Content of the notebook is empty.'
        });
      });
    }

    if (node.hasInnerWorkflow()) {
      const oldInnerWorkflow = createdNode.getInnerWorkflow();
      const oldInnerThirdPartyData = createdNode.getInnerThirdPartyData();
      const map = this._mapOldIdsWithNewOnes(oldInnerWorkflow);
      const newInnerThirdPartyData = this._assingNewIdsThirdPartyData(map, oldInnerThirdPartyData);
      const newInnerWorkflow = this._assignNewIds(map, oldInnerWorkflow);
      createdNode.setInnerWorkflow(newInnerWorkflow);
      createdNode.setInnerThirdPartyData(newInnerThirdPartyData);
    }

    return workflow.addNode(createdNode);
  }

  _mapOldIdsWithNewOnes(innerWorkflow) {
    let map = {};
    innerWorkflow.nodes.forEach((node) => {
      if (node.operation.id === specialOperations.CUSTOM_TRANSFORMER.NODE) {
        let mapFromNestedNode = this._mapOldIdsWithNewOnes(node.parameters['inner workflow'].workflow);
        map = angular.extend(map, mapFromNestedNode);
      }
      map[node.id] = this.UUIDGenerator.generateUUID();
    });
    return map;
  }

  _assignNewIds(map, innerWorkflow) {
    let newInnerWorkflow = angular.copy(innerWorkflow);
    newInnerWorkflow.connections.forEach((connection) => {
      connection.from.nodeId = map[connection.from.nodeId];
      connection.to.nodeId = map[connection.to.nodeId];
    });
    newInnerWorkflow.nodes.forEach((node) => {
      const nestedInnerWorkflow = node.parameters['inner workflow'];
      if (nestedInnerWorkflow) {
        nestedInnerWorkflow.workflow = this._assignNewIds(map, nestedInnerWorkflow.workflow);
        nestedInnerWorkflow.thirdPartyData = this._assingNewIdsThirdPartyData(map, nestedInnerWorkflow.thirdPartyData);

      }
      node.id = map[node.id];
    });
    return newInnerWorkflow;
  }

  _assingNewIdsThirdPartyData(map, thirdPartyData) {
    let thirdPartyDataCopy = angular.copy(thirdPartyData);
    Object.keys(thirdPartyDataCopy.gui.nodes).forEach((oldId) => {
      thirdPartyDataCopy.gui.nodes[map[oldId]] = thirdPartyDataCopy.gui.nodes[oldId];
      delete thirdPartyDataCopy.gui.nodes[oldId];
    });
    return thirdPartyDataCopy;
  }

  _setEdgeConnectionFromClone(node, clones, cloningNodeIds) {
    _.filter(node.edges, edge => edge.startNodeId !== node.id)
      .forEach(edge => {
        let cloningNodeIndex = cloningNodeIds.indexOf(edge.startNodeId);

        if (cloningNodeIndex > -1) {
          edge.__connectFromClone = clones[cloningNodeIndex].id;
        }
      });
  }

  isSinkOrSource(node) {
    return node.operationId === specialOperations.CUSTOM_TRANSFORMER.SINK ||
      node.operationId === specialOperations.CUSTOM_TRANSFORMER.SOURCE;
  }

}

exports.inject = function (module) {
  module.service('GraphNodesService', GraphNodesService);
};
