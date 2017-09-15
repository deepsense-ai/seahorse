/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var dagDemo = require('./dag-demo.js');

/* @ngInject */
function Experiment($stateParams) {
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

  this.experimentLabel = 'experiment:';
  this.data = tempExperimentsData[$stateParams.id];

  dagDemo();
}
exports.function = Experiment;

exports.inject = function (module) {
  module.controller('Experiment', Experiment);
};
