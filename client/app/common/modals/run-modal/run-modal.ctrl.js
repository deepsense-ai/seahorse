'use strict';

/* @ngInject */
function RunModalController($state, $stateParams, $modalInstance, message, RunModalFactory) {
  const LOADING = 'loading';
  const SUCCESS = 'success';
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
        then(() => {
          this.status = SUCCESS;
        }).
        catch(({ data } = { data: 'Server is not responding' }) => {
          this.status = FAILED;
          this.errorMessage = data;
        });
    },
    goToReport : function () {
      $state.go('workflows.latest_report', {
        'id': $stateParams.id
      });
      $modalInstance.dismiss();
    }
  });
}

exports.inject = function (module) {
  module.controller('RunModalController', RunModalController);
};
