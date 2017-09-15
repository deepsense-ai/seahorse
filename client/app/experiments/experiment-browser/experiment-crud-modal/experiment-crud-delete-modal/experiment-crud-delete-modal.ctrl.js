/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

const DI = new WeakMap();

class DeleteExperimentModalController {
  constructor(experimentItem, $modalInstance, ExperimentApiClient) {
    DI.set(this, {
      'experimentItem': experimentItem,
      'modalInstance': $modalInstance,
      'ExperimentApiClient': ExperimentApiClient
    });

    this.loading = false;
  }

  delete() {
    this.loading = true;

    DI.get(this).ExperimentApiClient.
      deleteExperiment(DI.get(this).experimentItem.id).
      then(DI.get(this).modalInstance.close).
      catch(() => {
        DI.get(this).modalInstance.dismiss(`Could not delete the experiment with id: ${DI.get(this).experimentItem.id}`);
      });
  }

  cancel() {
    DI.get(this).modalInstance.dismiss();
  }
}

exports.inject = function (module) {
  module.controller('DeleteExperimentModalController', DeleteExperimentModalController);
};
