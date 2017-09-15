'use strict';

/* @ngInject */
function VersionFactory(config) {
  let service = {};
  service.getDocsVersion = function() {
    return config.apiVersion.split('.').slice(0, 2).join('.');
  };
  return service;
}

exports.function = VersionFactory;

exports.inject = function(module) {
  module.factory('version', VersionFactory);
};
