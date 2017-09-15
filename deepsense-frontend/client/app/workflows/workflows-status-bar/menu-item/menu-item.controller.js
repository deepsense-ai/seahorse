'use strict';

/* @ngInject */
class MenuItemController {
  constructor(WorkflowService, UserService) {
    this.WorkflowService = WorkflowService;
    this.UserService = UserService;
  }

  isOwner() {
    return this.WorkflowService.getCurrentWorkflow().owner.id === this.UserService.getSeahorseUser().id;
  }

}

exports.inject = function(module) {
  module.controller('MenuItemController', MenuItemController);
};
