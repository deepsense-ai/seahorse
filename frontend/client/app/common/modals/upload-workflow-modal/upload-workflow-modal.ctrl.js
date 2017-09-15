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
function UploadWorkflowModalController($uibModalInstance, Upload, WorkflowsApiClient) {
  const STATUS_PREPARING = 'preparing';
  const STATUS_LOADING = 'loading';
  const STATUS_SUCCESS = 'success';
  const STATUS_FAILURE = 'failure';

  _.assign(this, {
    status: STATUS_PREPARING,
    errorMessage: '',
    progress: '',
    close: () => {
      $uibModalInstance.dismiss();
    },
    upload: function(file) {
      this.status = STATUS_FAILURE;
      Upload
        .upload({
          url: WorkflowsApiClient.getUploadWorkflowMethodUrl(),
          method: 'POST',
          file: file,
          fileFormDataName: 'workflowFile'
        })
        .progress((evt) => {
          this.status = STATUS_LOADING;
          this.progress = parseInt(100.0 * evt.loaded / evt.total, 10);
        })
        .then((response) => {
          this.status = STATUS_SUCCESS;
          $uibModalInstance.close(response.data.workflowId);
        })
        .catch(({
          data
        } = {}) => {
          let {
            message
          } = (data || {});
          this.status = STATUS_FAILURE;
          this.errorMessage = message || 'Server error';
        });
    },
    ok: function() {
      $uibModalInstance.close();
    }
  });
}

exports.inject = function(module) {
  module.controller('UploadWorkflowModalController', UploadWorkflowModalController);
};
