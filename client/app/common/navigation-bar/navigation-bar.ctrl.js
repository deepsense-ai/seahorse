/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function NavigationController(PageService) {
  var that = this;
  that.getTitle = function getTitle() {
    return PageService.getTitle();
  };
  return this;
}

exports.function = NavigationController;

exports.inject = function (module) {
  module.controller('NavigationController', NavigationController);
};
