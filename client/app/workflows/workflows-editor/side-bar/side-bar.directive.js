'use strict';

import tpl from './side-bar.html';

class SideBar {
  constructor() {
    this.restrict = 'E';
    this.templateUrl = tpl;
    this.controller = 'SideBarController';
    this.controllerAs = 'sbCtrl';
    this.replace = true;
  }
}

exports.inject = function(module) {
  module.directive('sideBar', () => new SideBar());
};
