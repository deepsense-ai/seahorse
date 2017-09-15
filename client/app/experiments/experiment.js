/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var dagDemo = require('./dag-demo.js');

/* @ngInject */
function Experiment($stateParams, $rootScope) {
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

  $rootScope.headerTitle = 'Experiment: ' + tempExperimentsData[$stateParams.id].name;

  dagDemo();
}
exports.function = Experiment;

exports.inject = function (module) {
  module.controller('Experiment', Experiment);
};
