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

/* @ngInject */
function WorkflowsApiClientFactory(BaseApiClient, ServerCommunication, config, $q) {

  const API_TYPE = 'batch';
  const PATH_WORKFLOWS = '/workflows';

  class WorkflowsApiClient extends BaseApiClient {

    constructor() {
      super();
      this.ServerCommunication = ServerCommunication;
    }

    getAllWorkflows() {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_WORKFLOWS}`, null, 10000);
    }

    getWorkflow(workflowId) {
      return this.makeRequest(this.METHOD_GET, `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}`);
    }

    createWorkflow(params) {
      let data = {
        metadata: {
          type: API_TYPE,
          apiVersion: config.apiVersion
        },
        workflow: {
          nodes: [],
          connections: []
        },
        thirdPartyData: {
          gui: {
            name: params.name,
            description: params.description
          }
        }
      };
      return this.makeRequest(this.METHOD_POST, this.API_URL + PATH_WORKFLOWS, data);
    }

    cloneWorkflow(workflowToClone) {
      return this.makeRequest(this.METHOD_POST, `${this.API_URL}${PATH_WORKFLOWS}/${workflowToClone.id}/clone`, {
        name: workflowToClone.name,
        description: workflowToClone.description
      });
    }

    deleteWorkflow(workflowId) {
      return this.makeRequest(this.METHOD_DELETE, `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}`);
    }

    updateWorkflow(serializedWorkflow) {
      let data = {
        workflowId: serializedWorkflow.id,
        workflow: _.clone(serializedWorkflow)
      };

      data.workflow.metadata = {
        type: API_TYPE,
        apiVersion: config.apiVersion
      };

      this.ServerCommunication.sendUpdateWorkflowToWorkflowExchange(data);
    }

    getDownloadWorkflowMethodUrl(workflowId, includeDatasources) {
      return `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/download?format=json&export-datasources=${!!includeDatasources}`;
    }

    getUploadWorkflowMethodUrl() {
      return `${this.API_URL}${PATH_WORKFLOWS}/upload`;
    }

    cloneNotebookNode(workflowId, sourceNodeId, destinationNodeId) {
      return this.makeRequest(
        this.METHOD_POST,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/notebook/${sourceNodeId}/copy/${destinationNodeId}`
      );
    }

    getPresetByWorkflowId(workflowId) {
      return this.makeRequest(
        this.METHOD_GET,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/preset`
      );
    }

    bindPresetToWorkflow(presetId, workflowId) {
      return this.makeRequest(
        this.METHOD_POST,
        `${this.API_URL}${PATH_WORKFLOWS}/${workflowId}/preset`,
        {
          'id': workflowId,
          'presetId': presetId
        }
      );
    }

  }

  return new WorkflowsApiClient();
}

exports.inject = function (module) {
  module.factory('WorkflowsApiClient', WorkflowsApiClientFactory);
};
