'use strict';

/* @ngInject */
function NavigationController(config) {

  const vm = this;

  vm.getAPIVersion = getAPIVersion;

  function getAPIVersion() {
    return config.editorVersion;
  }

}

exports.inject = function(module) {
  module.controller('NavigationController', NavigationController);
};
