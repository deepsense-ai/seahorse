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

import tpl from './delete-modal.html';

/* @ngInject */
function DeleteModalService($uibModal, $cookies) {
  const srv = this;

  srv.handleDelete = handleDelete;

  function handleDelete(deleteHandler, cookieName) {
    if ($cookies.get(cookieName) !== 'true') {
      openDeleteModal()
        .then((cookieValue) => {
          return cookieValue ? $cookies.put(cookieName, 'true') : false;
        })
        .then(deleteHandler);
    } else {
      deleteHandler();
    }
  }

  function openDeleteModal() {
    return $uibModal.open({
      animation: false,
      templateUrl: tpl,
      controller: 'DeleteConfirmationModalController',
      controllerAs: 'controller',
      backdrop: 'static',
      keyboard: true
    }).result;
  }

}

exports.inject = function (module) {
  module.service('DeleteModalService', DeleteModalService);
};
