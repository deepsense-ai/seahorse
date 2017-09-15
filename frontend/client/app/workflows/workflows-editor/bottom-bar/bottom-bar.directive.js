'use strict';

import tpl from './bottom-bar.html';

class BottomBar {
  constructor() {
    this.restrict = 'E';
    this.templateUrl = tpl;
    this.controller = 'BottomBarController';
    this.controllerAs = 'bbCtrl';
    this.replace = true;
  }
}

exports.inject = function(module) {
  module.directive('bottomBar', () => new BottomBar());
};
