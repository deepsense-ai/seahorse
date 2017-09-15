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
          return (cookieValue) ? $cookies.put(cookieName, 'true') : false;
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
