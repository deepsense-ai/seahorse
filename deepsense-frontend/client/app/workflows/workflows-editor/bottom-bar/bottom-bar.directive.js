'use strict';

class BottomBar {
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/workflows/workflows-editor/bottom-bar/bottom-bar.html';
    this.controller = 'BottomBarController';
    this.controllerAs = 'bbCtrl';
    this.replace = true;
  }
}

exports.inject = function(module) {
  module.directive('bottomBar', () => new BottomBar());
};
