'use strict';

let internal = {};
class BottomBarController {
  constructor(BottomBarService) {
    internal = {
      BottomBarService
    };
    this.tabsState = BottomBarService.tabsState;
  }

  activatePanel(panelName) {
    if (internal.BottomBarService.tabsState[panelName]) {
      internal.BottomBarService.deactivatePanel(panelName);
    } else {
      internal.BottomBarService.activatePanel(panelName);
    }
  }

}

exports.inject = function(module) {
  module.controller('BottomBarController', BottomBarController);
};
