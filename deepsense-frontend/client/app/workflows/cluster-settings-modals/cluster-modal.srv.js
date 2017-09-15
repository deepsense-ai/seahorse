'use strict';

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
      templateUrl: 'app/workflows/cluster-settings-modals/choose-cluster-modal.html',
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
      templateUrl: `app/workflows/cluster-settings-modals/preset-modal/preset-modal.html`,
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
