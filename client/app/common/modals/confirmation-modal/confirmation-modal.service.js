'use strict';

class ConfirmationModalService {
  /* @ngInject */
  constructor($modal) {
    this.$modal = $modal;
  }

  showModal(options = { message: '' }) {
    let modal =  this.$modal.open({
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

exports.inject = function (module) {
  module.service('ConfirmationModalService', ConfirmationModalService);
};
