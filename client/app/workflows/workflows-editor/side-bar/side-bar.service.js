'use strict';

class SideBarService {
  constructor() {
    this.data = {
      operationCatalogue: true
    };
  }

  activatePanel(panelName) {
    this.data[panelName] = !this.data[panelName];
  }

}

exports.inject = function(module) {
  module.service('SideBarService', SideBarService);
};
