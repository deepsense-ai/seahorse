'use strict';

import tpl from './export-modal.html';

class ExportModalService {
  /* @ngInject */
  constructor($uibModal) {
    this.$uibModal = $uibModal;
  }

  showModal() {
    let modal = this.$uibModal.open({
      animation: true,
      templateUrl: tpl,
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
