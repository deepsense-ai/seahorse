'use strict';

class ExportModalService {
  /* @ngInject */
  constructor($uibModal) {
    this.$uibModal = $uibModal;
  }

  showModal() {
    let modal = this.$uibModal.open({
      animation: true,
      templateUrl: 'app/common/modals/export-modal/export-modal.html',
      controller: 'ExportModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    return modal.result;
  }
}

exports.inject = function(module) {
  module.service('ExportModalService', ExportModalService);
};
