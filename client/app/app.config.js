/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function AppConfig($stateProvider, $urlRouterProvider) {
    // TODO: enable html5mode

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('lab', {
        abstract: true,
        template: '<div ui-view/>'
    });
}
exports.function = AppConfig;

exports.inject = function (module) {
  module.config(AppConfig);
};
