/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

const DI = new WeakMap();

function ShowModal(templateUrl, controller, resolveData) {
  let modal = DI.get(this).modal.open({
    animation: true,
    templateUrl: templateUrl,
    controller: controller,
    backdrop: 'static',
    keyboard: false,
    resolve: resolveData
  });

  modal.result.
    then(() => {
      DI.get(this).state.reload();
    }).
    catch((errorMessage) => {
      if (errorMessage) {
        DI.get(this).rootScope.transitToErrorState(errorMessage);
      }
    });
}

class ExperimentCrudController {
  constructor($scope, $modal, $state, $rootScope) {
    DI.set(this, {
      'modal': $modal,
      'state': $state,
      'rootScope': $rootScope
    });

    $scope.$on('Experiments.ADD_EXPERIMENT', (event) => {
      event.stopPropagation();
      this.addExperiment();
    });
  }

  addExperiment() {
    ShowModal.call(
      this,
      'app/experiments/experiment-browser/experiment-crud-modal/experiment-crud-add-modify-modal/experiment-crud-add-modify-modal.tmpl.html',
      'AddModifyExperimentModalController as addModifyExprModal',
      {
        experimentItem: null
      }
    );
  }

  modifyExperiment(experimentItem) {
    ShowModal.call(
      this,
      'app/experiments/experiment-browser/experiment-crud-modal/experiment-crud-add-modify-modal/experiment-crud-add-modify-modal.tmpl.html',
      'AddModifyExperimentModalController as addModifyExprModal',
      {
        experimentItem: () => experimentItem
      }
    );
  }

  removeExperiment(experimentItem) {
    ShowModal.call(
      this,
      'app/experiments/experiment-browser/experiment-crud-modal/experiment-crud-delete-modal/experiment-crud-delete-modal.tmpl.html',
      'DeleteExperimentModalController as deleteExprModal',
      {
        experimentItem: () => experimentItem
      }
    );
  }
}

exports.inject = function (module) {
  module.controller('ExperimentCrudController', ExperimentCrudController);
};
