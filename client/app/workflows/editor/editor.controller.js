'use strict';

import _ from 'lodash';

const ZOOM_STEP = 0.1;
const MAIN_HEADER_HEIGHT = 90;

class EditorController {
  constructor($rootScope, CanvasService, AdapterService, UUIDGenerator, Operations, OperationsHierarchyService, MouseEvent,  $element) {
    'ngInject';
    this.CanvasService = CanvasService;
    this.AdapterService = AdapterService;
    this.UUIDGenerator = UUIDGenerator; //TODO remove when services are refactored
    this.Operations = Operations;       //TODO remove when services are refactored
    this.OperationsHierarchyService = OperationsHierarchyService;       //TODO remove when services are refactored
    this.MouseEvent = MouseEvent;
    this.$element = $element;

    this.categories = Operations.getCatalog();

    $rootScope.$on('Keyboard.KEY_PRESSED_DEL', () => {
      this.hideTooltip();
    });
  }

  $postLink() {
    this.bindEvents();
    this.containment = $(this.$element[0].querySelector('.flowchart-box'));

    this.AdapterService.handleShowTooltip(this.showTooltip.bind(this));
    this.AdapterService.handleHideTooltip(this.hideTooltip.bind(this));
  }

  $onDestroy() {
    this.$canvas.off();
  }

  bindEvents() {
    this.$canvas = $(this.$element[0].querySelector('core-canvas'));
    this.$toolbar = $(this.$element[0].querySelector('canvas-toolbar'));
    //Wheel handling
    this.$canvas.bind('wheel', (e) => {
      let zoomDelta = ZOOM_STEP;
      if (e.originalEvent.deltaY < 0) {
        zoomDelta = -1 * ZOOM_STEP;
      }
      this.CanvasService.centerZoom(zoomDelta);
    });

    // Drag handling in JSPlumb
    const moveHandler = (event) => {
      if (this.MouseEvent.isModKeyDown(event)) {
        this.CanvasService.moveWindow(event.originalEvent.movementX, event.originalEvent.movementY);
      } else {
        this.$canvas.off('mousemove', moveHandler);
      }
    };

    this.$canvas.bind('mousedown', () => {
      if (this.MouseEvent.isModKeyDown(event)) {
        this.$canvas.bind('mousemove', moveHandler);
      }
    });

    this.$canvas.bind('mouseup', () => {
      this.$canvas.off('mousemove', moveHandler);
    });

    // Drag and Drop from toolbar handling
    this.$canvas.bind('drop', (event) => {
      const originalEvent = event.originalEvent;
      if (originalEvent.dataTransfer.getData('draggableExactType') === 'graphNode') {
        this.startWizard(originalEvent.offsetX, originalEvent.offsetY);
      }
    });

    this.$element.bind('mousedown', () => {
      if (this.newNodeData) {
        this.newNodeData = null;
      }
    });

    this.$toolbar.bind('mousedown', (event) => {
      event.stopPropagation();
    });

    // Drag and Drop from toolbar handling
    this.$canvas.bind('contextmenu', (event) => {
      const originalEvent = event.originalEvent;
      this.startWizard(originalEvent.offsetX, originalEvent.offsetY);
      return false;
    });
  }

  zoomIn() {
    this.CanvasService.centerZoom(ZOOM_STEP);
  }

  zoomOut() {
    this.CanvasService.centerZoom(-1 * ZOOM_STEP);
  }

  fit() {
    this.CanvasService.fit();
  }

  fullScreen() {
    //TODO: add after the design of scene changes
  }

  newNode($event) {
    const [x, y] = this.CanvasService.translatePosition(100, 100);
    this.startWizard(x, y);
  }

  onConnectionAbort(newNodeData) {
    this.startWizard(newNodeData.x, newNodeData.y, newNodeData.endpoint);
  }

  startWizard(x, y, endpoint = null) {
    if (this.isEditable) {
      const newNodeData = {
        x: x,
        y: y,
        nodeId: null,
        portIndex: null,
        typeQualifier: null
      };

      // After some time endpoint paramters are wiped by jsPlumb,
      // and we need those params to render the temporary edges or after wizard the final edges.
      if (endpoint) {
        newNodeData.nodeId = endpoint.getParameter('nodeId');
        newNodeData.portIndex = endpoint.getParameter('portIndex');
        newNodeData.typeQualifier = this.workflow.getNodeById(newNodeData.nodeId).output[newNodeData.portIndex].typeQualifier;
        this.categories = this.Operations.filterCatalog(this.Operations.getCatalog(), this.getFilterForCatalog(newNodeData.typeQualifier));
      } else {
        this.categories = this.Operations.getCatalog();
      }

      this.newNodeData = newNodeData;
    }
  }

  onDisplayRectChange(rect) {
    const {right, bottom} = this.containment[0].getBoundingClientRect();
    this.CanvasService.moveWindow(Math.min(right - rect.right, 0) * this.CanvasService.scale, Math.min(bottom - rect.bottom, 0) * this.CanvasService.scale, 0.5);
  }

  onSelect(operationId) {
    const newNodeElement = this.$element[0].querySelector('new-node');
    //TODO move it out of here to some service when they're refactored
    const params = {
      id: this.UUIDGenerator.generateUUID(),
      x: newNodeElement.offsetLeft,
      y: newNodeElement.offsetTop,
      operation: this.Operations.get(operationId)
    };
    const node = this.workflow.createNode(params);
    this.workflow.addNode(node);
    if (this.newNodeData.nodeId) {
      const newEdge = this.workflow.createEdge({
        from: {
          nodeId: this.newNodeData.nodeId,
          portIndex: this.newNodeData.portIndex
        },
        to: {
          nodeId: node.id,
          portIndex: this.getMatchingPortIndex(node, this.newNodeData.typeQualifier)
        }
      });
      this.workflow.addEdge(newEdge);
    }
    this.newNodeData = null;
  }

  //TODO move to service that is aware of OperationsHierarchy Service and Operation and GraphNode??
  getFilterForCatalog(inputQualifier) {
    return (item) => {
      return this.Operations.get(item.id).ports.input.filter((port) =>
        this.OperationsHierarchyService.IsDescendantOf(inputQualifier, port.typeQualifier)
      ).length;
    };
  }

  //TODO move to service that is aware of OperationsHierarchy Service and GraphNode
  getMatchingPortIndex(node, typeQualifier) {
    let portIndex = 0;
    node.input.forEach((port, index) => {
      if (this.OperationsHierarchyService.IsDescendantOf(typeQualifier, port.typeQualifier)) {
        portIndex = index;
      }
    });
    return portIndex;
  }

  //TODO move it to service which is aware of endpoints and nodes
  getTypeQualifierForEndpoint(endpoint) {
    const node = this.workflow.getNodeById(endpoint.getParameter('nodeId'));
    const port = node.originalOutput[endpoint.getParameter('portIndex')];

    return [...port.typeQualifier];
  }

  //TODO move it to service which is aware of endpoints and their params
  getAvailableOperations(workflow, sourceEndpoint) {
    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourceNode = workflow.getNodeById(sourceNodeId);
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = sourceNode.output[sourcePortIndex];


    const operations = this.Operations.getData();
    const operationsMatch = [];
    _.forEach(operations, (operation) => {
      const inputMatches = _.reduce(operation.ports.input, (acc, input) => {
        const typesMatch = _.every(_.map(
          sourcePort.typeQualifier,
          typeQualifier => this.OperationsHierarchyService.IsDescendantOf(typeQualifier, input.typeQualifier)
        ));

        acc.push(typesMatch);

        return acc;
      }, []);
      operationsMatch[operation.id] = _.some(inputMatches);
    });

    return operationsMatch;
  }

  showTooltip(portEl, portObject) {
    this.portElement = portEl;
    this.portObject = portObject;
    this.isTooltipVisible = true;

    this.tooltipX = this.getPositionX();
    this.tooltipY = this.getPositionY();
  }

  hideTooltip() {
    this.portElement = null;
    this.portObject = null;
    this.isTooltipVisible = false;
  }

  getPositionX() {
    return Math.round(
      this.portElement.getBoundingClientRect()
        .left +
      this.portElement.getBoundingClientRect()
        .width
    );
  }

  getPositionY() {
    const adjustment = (this.portObject.type === 'input') ? -5 : 5;

    return Math.round(
      this.portElement.getBoundingClientRect()
        .top - MAIN_HEADER_HEIGHT
      + adjustment
    );
  }

  //TODO move it to service which is aware of endpoints and nodes
  getTypeQualifierForEndpoint(endpoint) {
    const node = this.workflow.getNodeById(endpoint.getParameter('nodeId'));
    const port = node.originalOutput[endpoint.getParameter('portIndex')];

    return [...port.typeQualifier];
  }

  //TODO move it to service which is aware of endpoints and their params
  getAvailableOperations(workflow, sourceEndpoint) {
    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourceNode = workflow.getNodeById(sourceNodeId);
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = sourceNode.output[sourcePortIndex];


    const operations = this.Operations.getData();
    const operationsMatch = [];
    _.forEach(operations, (operation) => {
      const inputMatches = _.reduce(operation.ports.input, (acc, input) => {
        const typesMatch = _.every(_.map(
          sourcePort.typeQualifier,
          typeQualifier => this.OperationsHierarchyService.IsDescendantOf(typeQualifier, input.typeQualifier)
        ));

        acc.push(typesMatch);

        return acc;
      }, []);
      operationsMatch[operation.id] = _.some(inputMatches);
    });

    return operationsMatch;
  }

  showTooltip(portEl, portObject) {
    this.portElement = portEl;
    this.portObject = portObject;
    this.isTooltipVisible = true;

    [this.tooltipX, this.tooltipY] = this.getPosition();
  }

  hideTooltip() {
    this.portElement = null;
    this.portObject = null;
    this.isTooltipVisible = false;
  }

  getPosition() {
    // To show tooltip in the same position for both inputs and outputs
    const adjustment = (this.portObject.type === 'input') ? -5 : 5;

    return [
      Math.round(this.portElement.getBoundingClientRect().right),
      Math.round(this.portElement.getBoundingClientRect().top - this.$canvas[0].getBoundingClientRect().top + adjustment)
    ];
  }
}

export default EditorController;
