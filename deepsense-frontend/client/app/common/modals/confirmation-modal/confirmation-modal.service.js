'use strict';

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
      templateUrl: 'app/common/modals/confirmation-modal/confirmation-modal.html',
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
