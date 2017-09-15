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

import tpl from './workflow-clone-modal.html';

/* @ngInject */
function WorkflowCloneService($uibModal, WorkflowsApiClient, NotificationService) {

  this.openModal = (callback, workflow) => {
    const modal = $uibModal.open({
      animation: true,
      templateUrl: tpl,
      controller: 'WorkflowCloneModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        originalWorkflow: () => workflow
      }
    });

    modal.result.then((workflowToClone) => {
      WorkflowsApiClient.cloneWorkflow(workflowToClone).then(callback, () => {
        NotificationService.showWithParams({
          message: 'There was an error during copying workflow.',
          title: 'Workflow copy',
          settings: {timeOut: 10000},
          notificationType: 'error'
        });
      });
    });
  };
}

exports.inject = function (module) {
  module.service('WorkflowCloneService', WorkflowCloneService);
};
