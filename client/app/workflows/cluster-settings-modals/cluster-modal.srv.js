'use strict';

import clusterModalTpl from './choose-cluster-modal.html';
import presetModalTpl from './preset-modal/preset-modal.html';

const presetTypesMap = {
  standalone: 'Stand-alone',
  yarn: 'YARN',
  mesos: 'Mesos',
  local: 'Local'
};

/* @ngInject */
function ClusterModalService($uibModal) {
  const service = this;

  service.formatPresetType = formatPresetType;
  service.openClusterSelectionModal = openClusterSelectionModal;
  service.openCurrentClusterModal = openCurrentClusterModal;

  function formatPresetType(type) {
    return presetTypesMap[type];
  }

  function openClusterSelectionModal() {
    return $uibModal.open({
      animation: false,
      templateUrl: clusterModalTpl,
      controller: 'ChooseClusterModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      size: 'lg',
      keyboard: true
    });
  }

  function openCurrentClusterModal(type, preset, isSnapshot) {
    return $uibModal.open({
      animation: false,
      templateUrl: presetModalTpl,
      controller: 'PresetModalCtrl',
      controllerAs: 'controller',
      backdrop: 'static',
      size: 'lg',
      keyboard: true,
      resolve: {
        preset: () => {
          return preset;
        },
        type: () => {
          return type;
        },
        isSnapshot: () => {
          return isSnapshot;
        }
      }
    });
  }

}

exports.inject = function (module) {
  module.service('ClusterModalService', ClusterModalService);
};
