'use strict';

/* @ngInject */
function ContextMenuWrapperController($scope, $element, $window,
                                      WorkflowService, ReportOptionsService, GraphNode, Edge) {
  let that = this;
  let internal = {};

  let $flowchartBox = $($element).closest('.flowchart-box');

  internal.contextMenuState = 'invisible';
  internal.contextMenuPosition = {};

  internal.rawCloseContextMenu = function rawCloseContextMenu () {
    $scope.$broadcast('ContextMenu.CLOSE');
    internal.contextMenuState = 'invisible';
  };

  internal.closeContextMenu = function closeContextMenu () {
    internal.rawCloseContextMenu();
    $scope.$digest();
  };

  internal.contextMenuOpener = function contextMenuOpener (event, data) {
    let portEl = data.reference.canvas;
    let dimensions = portEl.getBoundingClientRect();

    internal.contextMenuPosition.x = dimensions.left + dimensions.width / 1.5;
    internal.contextMenuPosition.y = dimensions.top + dimensions.height / 1.5;
    internal.contextMenuState = 'visible';

    $scope.$digest();
  };

  internal.isNotInternal = function isNotInternal (event) {
    return event.target && event.target.matches('.context-menu *') === false;
  };

  internal.checkClickAndClose = function checkClickAndClose (event) {
    if (internal.isNotInternal(event)) {
      internal.closeContextMenu();
    }
  };

  internal.handlePortRightClick = function handlePortRightClick (event, data) {
    let port = data.reference;
    let nodeId = port.getParameter('nodeId');
    let currentNode = WorkflowService.getWorkflow().getNodes()[nodeId];

    ReportOptionsService.setCurrentPort(port);
    ReportOptionsService.setCurrentNode(currentNode);
    ReportOptionsService.clearReportOptions();
    ReportOptionsService.updateReportOptions();

    internal.contextMenuOpener(...arguments);
  };

  _.assign(that, {
    getContextMenuState: () => internal.contextMenuState,
    getContextMenuPositionY: () => internal.contextMenuPosition.y,
    getContextMenuPositionX: () => internal.contextMenuPosition.x,
    getPositionY: () => $flowchartBox[0].offsetTop,
    getPositionX: () => $flowchartBox[0].getBoundingClientRect().left,
    getReportOptions: () => ReportOptionsService.getReportOptions()
  });

  let $flowchartBoxScope = $scope.$parent.$parent;
  $flowchartBoxScope.$on(GraphNode.MOUSEDOWN, internal.closeContextMenu);
  $flowchartBoxScope.$on(Edge.DRAG, internal.closeContextMenu);
  $flowchartBoxScope.$on('InputPoint.CLICK', internal.closeContextMenu);
  $flowchartBoxScope.$on('OutputPort.LEFT_CLICK', internal.closeContextMenu);
  $flowchartBoxScope.$on('OutputPort.RIGHT_CLICK', internal.handlePortRightClick);
  $flowchartBoxScope.$on('Keyboard.KEY_PRESSED_ESC', internal.closeContextMenu);
  $flowchartBoxScope.$on('ZOOM.ZOOM_PERFORMED', internal.rawCloseContextMenu);

  $window.addEventListener('mousedown', internal.checkClickAndClose);
  $window.addEventListener('blur', internal.closeContextMenu);

  $scope.$on('$destroy', () => {
    $window.removeEventListener('mousedown', internal.checkClickAndClose);
    $window.removeEventListener('blur', internal.closeContextMenu);
  });

  return that;
}

/* @ngInject */
function ContextMenuWrapperDirective() {
  return {
    restrict: 'E',
    controller: ContextMenuWrapperController,
    controllerAs: 'contextMenuWrapperCtrl',
    bindToController: true,
    scope: true,
    transclude: true,
    templateUrl: 'app/workflows/graph-panel/context-menu-wrapper/context-menu-wrapper.tmpl.html'
  };
}

exports.inject = function (module) {
  module.directive('contextMenuWrapper', ContextMenuWrapperDirective);
};

