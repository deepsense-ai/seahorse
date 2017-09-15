'use strict';

/* @ngInject */
function VersionFactory(config) {
  let service = {};
  service.getDocsVersion = function() {
    // TODO Workaround for lacking of documentation 1.2
    return "1.1";
    //return config.apiVersion.split('.').slice(0, 2).join('.');
  };
  return service;
}

exports.function = VersionFactory;

exports.inject = function(module) {
  module.factory('version', VersionFactory);
};
