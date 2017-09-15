/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

const DI = new WeakMap();

class AddModifyExperimentModalController {
  constructor(experimentItem, $modalInstance, ExperimentApiClient) {
    DI.set(this, {
      'experimentItem': experimentItem,
      'modalInstance': $modalInstance,
      'ExperimentApiClient': ExperimentApiClient
    });

    this.loading = false;

    if (experimentItem) {
      _.assign(this, {
        modifyAction: true,
        experimentName: experimentItem.name,
        experimentDescription: experimentItem.description
      });
    } else {
      this.modifyAction = false;
    }
  }

  proceed() {
    this.loading = true;

    if (!this.modifyAction) {
      DI.get(this).ExperimentApiClient.
        createExperiment({
          'name': this.experimentName || 'Draft experiment',
          'description': this.experimentDescription || ''
        }).
        then(DI.get(this).modalInstance.close).
        catch(() => {
          DI.get(this).modalInstance.dismiss(`Could not create the experiment`);
        });
    } else {
      DI.get(this).ExperimentApiClient.
        modifyExperiment(DI.get(this).experimentItem.id, {
          'name': this.experimentName || 'Draft experiment',
          'description': this.experimentDescription || '',
          'graph': DI.get(this).experimentItem.graph
        }).
        then(DI.get(this).modalInstance.close).
        catch(() => {
          DI.get(this).modalInstance.dismiss(`Could not modify the experiment with id: ${DI.get(this).experimentItem.id}`);
        });
    }
  }

  cancel() {
    DI.get(this).modalInstance.dismiss();
  }
}

exports.inject = function (module) {
  module.controller('AddModifyExperimentModalController', AddModifyExperimentModalController);
};
