'use strict';

import tpl from './confirmation-modal.html';

class ConfirmationModalService {
  /* @ngInject */
  constructor($uibModal) {
    this.$uibModal = $uibModal;
  }

  showModal(options = {
    message: ''
  }) {
    let modal = this.$uibModal.open({
      animation: true,
      templateUrl: tpl,
      controller: 'ConfirmationModalController as controller',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        message: () => options.message
      }
    });

    return modal.result;
  }
}

exports.inject = function(module) {
  module.service('ConfirmationModalService', ConfirmationModalService);
};
