'use strict';

let internal = {};
class SideBarController {
  constructor(SideBarService) {
    internal = {
      SideBarService
    };
    this.data = SideBarService.data;
  }

  activatePanel(panelName) {
    internal.SideBarService.activatePanel(panelName);
  }

}

exports.inject = function(module) {
  module.controller('SideBarController', SideBarController);
};
