/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function DatasetsConfig($stateProvider) {
  $stateProvider.state('lab.datasets', {
      url: '/datasets',
      templateUrl: 'app/datasets/dataset-list.html',
      controller: 'DatasetList as datasetList'
  });
}
exports.function = DatasetsConfig;

exports.inject = function (module) {
  module.config(DatasetsConfig);
};
