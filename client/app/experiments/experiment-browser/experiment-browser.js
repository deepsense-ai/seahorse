/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


/* @ngInject */
function ExperimentListController(PageService, ExperimentAPIClient) {
  ExperimentAPIClient.getList().then((data) => {
    this.experiments = data.experiments;
  });

  PageService.setTitle('My experiments');
}

exports.inject = function (module) {
  module.controller('ExperimentListController', ExperimentListController);
};
