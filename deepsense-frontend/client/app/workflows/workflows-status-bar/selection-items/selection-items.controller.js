'use strict';

/* @ngInject */
function SelectionItemsController($scope, $rootScope, MultiSelectionService, WorkflowService, DeleteModalService,
                                  UserService, EventsService) {

  const COOKIE_NAME = 'SEAHORSE_NODE_DELETE_NO_CONFIMRATION';
  const vm = this;

  vm.selection = MultiSelectionService.getSelectedNodeIds();
  vm.currentWorkflow = WorkflowService.getCurrentWorkflow();
  vm.hasElements = hasElements;
  vm.isOwner = isOwner;
  vm.delete = deleteSelectedNodes;
  vm.canDelete = canDelete;

  initialize();
  return vm;

  function initialize() {
    //track selections from MultiSelectionService
    //$rootScope over $scope for performance
    let selectionListener = $rootScope.$watch(() => MultiSelectionService.getSelectedNodeIds(), (newSelection) => {
      vm.selection = newSelection;
    });

    //clear selections when switching workspaces, as MultiSelection service is not aware of Workflow change
    //$rootScope over $scope for performance
    let workflowListener = $rootScope.$watch(() => WorkflowService.getCurrentWorkflow(), () => {
        vm.selection = [];
    });

    //unbind all listeners on destroy
    $scope.$on('$destroy', () => {
      selectionListener();
      workflowListener();
    });
  }

  function canDelete() {
    return (isOwner() &&
      vm.currentWorkflow.workflowStatus === 'editor' &&
      vm.currentWorkflow.sessionStatus === 'running');
  }

  function isOwner() {
    return vm.currentWorkflow.owner.id === UserService.getSeahorseUser().id;
  }

  function deleteSelection() {
    EventsService.publish(EventsService.EVENTS.WORKFLOW_DELETE_SELECTED_ELEMENT);
  }

  function deleteSelectedNodes() {
    DeleteModalService.handleDelete(deleteSelection, COOKIE_NAME);
  }

  function hasElements() {
    return vm.selection.length > 0;
  }
}

exports.inject = function (module) {
  module.controller('SelectionItemsController', SelectionItemsController);
};
