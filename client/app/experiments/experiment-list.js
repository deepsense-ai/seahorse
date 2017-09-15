/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentList($rootScope) {
  var tempExperimentsData = {
    'a01': {
      'id': 'a01',
      'name': 'test experiment'
    },
    'a02': {
      'id': 'a02',
      'name': 'some important stuff'
    }
  };

  this.experiments = tempExperimentsData;
  $rootScope.headerTitle = 'My experiments';
}
exports.function = ExperimentList;

exports.inject = function (module) {
  module.controller('ExperimentList', ExperimentList);
};
