'use strict';

/* @ngInject */
function RunModalController($modalInstance, message, RunModalFactory) {
  const LOADING = 'loading';
  const LOADED = 'loaded';
  const FAILED = 'failed';

  _.assign(this, {
    loading: false,
    message: message,
    close: () => {
      $modalInstance.dismiss();
    },
    execute: function () {
      this.status = LOADING;

      RunModalFactory.execute().
        then((response) => {
          this.status = LOADED;
          return $modalInstance.close(response.id);
        }).
        catch((reason = { data: 'Server is not responding' }) => {
          this.status = FAILED;
          this.errorMessage = reason.data;
        });
    }
  });
}

exports.inject = function (module) {
  module.controller('RunModalController', RunModalController);
};
