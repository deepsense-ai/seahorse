/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentList() {
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

  this.myExperiments = 'My experiments:';
  this.experiments = tempExperimentsData;
}
exports.function = ExperimentList;

exports.inject = function (module) {
  module.controller('ExperimentList', ExperimentList);
};
