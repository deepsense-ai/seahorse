'use strict';

class ExportModalService {
  /* @ngInject */
  constructor($modal) {
    this.$modal = $modal;
  }

  showModal() {
    let modal =  this.$modal.open({
      animation: true,
      templateUrl: 'app/common/modals/export-modal/export-modal.html',
      controller: 'ExportModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    return modal.result;
  }
}

exports.inject = function (module) {
  module.service('ExportModalService', ExportModalService);
};
