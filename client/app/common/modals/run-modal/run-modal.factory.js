'use strict';

/* @ngInject */
function RunModalFactory ($modal, $q, BaseApiClient) {
  // const URL = 'NO_URL';

  class RunModal extends BaseApiClient {

    constructor() {
      super();
    }

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
      // return this.makeRequest(this.METHOD_GET, URL);
      let def = $q.defer();

      setTimeout(() => def.reject(), 10000);

      return def.promise;
    }
  }

  return new RunModal();
}

exports.inject = function (module) {
  module.factory('RunModalFactory', RunModalFactory);
};
