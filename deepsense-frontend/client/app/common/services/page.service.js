'use strict';

/* @ngInject */
function PageService() {
  var that = this;
  var internal = {};

  that.setTitle = function setTitle(name) {
    internal.title = name;
  };

  that.getTitle = function getTitle() {
    return internal.title || '';
  };

  return that;
}

exports.function = PageService;

exports.inject = function(module) {
  module.service('PageService', PageService);
};
