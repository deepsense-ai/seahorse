'use strict';

/* @ngInject */
function RunModalFactory ($modal, $q, $timeout) {
  class RunModal {

    constructor() {}

    showModal(options = {}) {
      options.message = options.message || '';

      let modal =  $modal.open({
        animation: true,
        templateUrl: 'app/common/modals/run-modal/run-modal.html',
        controller: 'RunModalController as controller',
        resolve: {
          message: () => options.message
        }
      });

      return modal.result;
    }

    execute () {
      let def = $q.defer();
      $timeout(def.resolve, 5000, false);
      return def.promise;
    }
  }

  return new RunModal();
}

exports.inject = function (module) {
  module.factory('RunModalFactory', RunModalFactory);
};
