'use strict';

/* @ngInject */
function WorkflowsConfig($stateProvider) {
  $stateProvider.state('workflows', {
    url: '/workflows',
    abstract: true,
    templateUrl: 'app/workflows/workflows.html'
  });
}

exports.function = WorkflowsConfig;

exports.inject = function(module) {
  module.config(WorkflowsConfig);
};
