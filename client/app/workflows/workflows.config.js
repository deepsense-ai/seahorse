'use strict';

import tpl from './workflows.html';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.state('workflows', {
    url: '/workflows',
    abstract: true,
    templateUrl: tpl
  });
}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
