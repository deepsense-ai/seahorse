'use strict';

class SideBar {
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/workflows/workflows-editor/side-bar/side-bar.html';
    this.controller = 'SideBarController';
    this.controllerAs = 'sbCtrl';
    this.replace = true;
  }
}

exports.inject = function(module) {
  module.directive('sideBar', () => new SideBar());
};
