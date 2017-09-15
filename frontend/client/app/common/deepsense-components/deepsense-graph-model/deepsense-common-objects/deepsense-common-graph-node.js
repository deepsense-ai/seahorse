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

angular
  .module('deepsense.graph-model')
  .factory('GraphNode', /* @ngInject */function(Port) {

    function GraphNode(options) {
      this.id = options.id;
      this.description = options.description;
      this.name = options.name;
      this.uiName = options.uiName || '';
      this.color = options.color || '#00B1EB';
      this.operationId = options.operationId;
      this.version = options.version;
      this.icon = options.icon;
      this.type = options.type;
      this.input = this.fetchPorts('input', options.input);
      this.output = this.fetchPorts('output', options.output);
      this.originalOutput = _.cloneDeep(this.output, true);
      this.edges = {};
      this.x = options.x;
      this.y = options.y;
      this.nodeGetter = options.nodeGetter;

      if (options.parametersValues) {
        this.parametersValues = options.parametersValues;
      } else {
        this.parameters = options.parameters;
      }

      this.state = null; // report state
      this.knowledgeErrors = []; // inference errors
    }

    GraphNode.prototype.STATUS = {
      'DRAFT': 'status_draft',
      'QUEUED': 'status_queued',
      'RUNNING': 'status_running',
      'COMPLETED': 'status_completed',
      'FAILED': 'status_failed',
      'ABORTED': 'status_aborted'
    };

    GraphNode.prototype.STATUS_DEFAULT = GraphNode.prototype.STATUS.DRAFT;

    GraphNode.prototype.fetchPorts = function fetchPorts(type, ports) {
      return _.map(ports, (portFromApi) => new Port({
          nodeId: this.id,
          type: type,
          portIndex: portFromApi.portIndex,
          portPosition: portFromApi.portPosition,
          required: portFromApi.required,
          typeQualifier: portFromApi.typeQualifier
        }));
    };

    GraphNode.prototype.serialize = function serialize() {
      let data = {
        'id': this.id,
        'operation': {
          'id': this.operationId,
          'name': this.name,
          'version': this.version
        },
        'parameters': this.parametersValues ? this.parametersValues : this.parameters.serialize()
      };

      return data;
    };

    GraphNode.prototype.updateState = function updateState(state) {
      if (!state) {
        this.state = null;
      } else {
        let status = state && state.status && Object.keys(this.STATUS).indexOf(state.status) > -1 ?
          this.STATUS[state.status] :
          this.STATUS_DEFAULT;

        this.state = {
          'started': state.started,
          'ended': state.ended,
          'results': state.results ? state.results : [],
          'error': state.error,
          'status': status
        };
      }
    };

    GraphNode.prototype.getResult = function getResult(portIndex) {
      return this.state ?
        this.state.results[portIndex] :
        null;
    };

    GraphNode.prototype.setParameters = function setParameters(parametersSchema, DeepsenseNodeParameters) {
      if (this.parametersValues) {
        this.parameters = DeepsenseNodeParameters.factory.createParametersList(this.parametersValues, parametersSchema, this);
        this.parametersValues = null;
      }
    };

    GraphNode.prototype.refreshParameters = function refreshParameters() {
      if(this.parameters) {
        this.parameters.refresh(this);
      }
    };

    GraphNode.prototype.hasParameters = function hasParameters() {
      return !this.parametersValues && this.parameters;
    };

    GraphNode.prototype.hasInnerWorkflow = function hasInnerWorkflow() {
      return this.hasParameters() && _.find(this.parameters.parameters, (param) => param.value && param.value.workflow);
    };

    GraphNode.prototype.getInnerWorkflow = function getInnerWorkflow() {
      return this.parametersValues['inner workflow'].workflow;
    };

    GraphNode.prototype.getInnerThirdPartyData = function getInnerThirdPartyData() {
      return this.parametersValues['inner workflow'].thirdPartyData;
    };

    GraphNode.prototype.setInnerWorkflow = function setInnerWorkflow(innerWorkflow) {
      this.parametersValues['inner workflow'].workflow = innerWorkflow;
    };

    GraphNode.prototype.setInnerThirdPartyData = function setInnerThirdPartyData(innerThirdPartyData) {
      this.parametersValues['inner workflow'].thirdPartyData = innerThirdPartyData;
    };

    GraphNode.prototype.getFancyKnowledgeErrors = function getFancyKnowledgeErrors() {
      return this.knowledgeErrors
        .map((error, ix) => `<strong>#${ix + 1}</strong>: ${error}`)
        .join('<br/>');
    };

    GraphNode.prototype.getIncomingEdge = function getIncomingEdge(portIndex) {
      let edges = _.values(this.edges);
      return _.find(edges, (edge) => (edge.endNodeId === this.id && edge.endPortId === portIndex));
    };

    /**
     * Knowledge incoming to this node in given portIndex
     * @param {Number} portIndex Index of node's port to which knowledge incomes
     * @returns {Object|undefined} Knowledge that incomes to [[portIndex]], or undefined if no edge is connected
     */
    GraphNode.prototype.getIncomingKnowledge = function getIncomingKnowledge(portIndex) {
      let incomingEdge = this.getIncomingEdge(portIndex);
      if (incomingEdge) {
        let {startPortId, startNodeId} = incomingEdge;
        let parentNode = this.nodeGetter(startNodeId);
        return parentNode.output[startPortId];
      }
      return undefined;
    };

    return GraphNode;
  });
