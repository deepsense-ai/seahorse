'use strict';

require('../config.js');

/* @ngInject */
function VersionFactory(config) {
  let service = {};
  service.getDocsVersion = function() {
    return config.apiVersion.split('.').slice(0, 2).join('.');
  };
  service.editorVersion = config.editorVersion;
  return service;
}

exports.function = VersionFactory;

exports.inject = function(module) {
  module.factory('version', VersionFactory);
};
