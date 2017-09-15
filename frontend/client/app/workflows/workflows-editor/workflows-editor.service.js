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

const COOKIE_NAME = 'SEAHORSE_NODE_DELETE_NO_CONFIRMATION';

/* ngInject */
function WorkflowsEditorService(DeleteModalService, EventsService, WorkflowService) {

  const service = this;

  service.handleDelete = handleDelete;

  function handleDelete() {
    if (WorkflowService.isWorkflowEditable()) {
      DeleteModalService.handleDelete(deleteSelection, COOKIE_NAME);
    }
  }

  function deleteSelection() {
    EventsService.publish(EventsService.EVENTS.WORKFLOW_DELETE_SELECTED_ELEMENT);
  }
}

exports.inject = function (module) {
  module.service('WorkflowsEditorService', WorkflowsEditorService);
};
