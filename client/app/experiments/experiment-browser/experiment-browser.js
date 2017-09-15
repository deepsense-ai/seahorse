/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


/* @ngInject */
function ExperimentListController(experiments, PageService) {
  this.experiments = experiments.experiments;

  PageService.setTitle('My experiments');
}

exports.inject = function (module) {
  module.controller('ExperimentListController', ExperimentListController);
};
