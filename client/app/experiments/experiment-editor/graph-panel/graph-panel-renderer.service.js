/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

let Edge = require('./../../common-objects/common-edge.js');

let connectorPaintStyleDefault = {
  lineWidth: 2,
  outlineColor: 'white',
  outlineWidth: 2
};

/**
 * Maps edge's state to its style object
 *
 * @type {object}
 */
let connectorPaintStyles = {};
connectorPaintStyles[Edge.STATE_TYPE.ALWAYS] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#61B7CF' });
connectorPaintStyles[Edge.STATE_TYPE.MAYBE] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#F8AC59' });
connectorPaintStyles[Edge.STATE_TYPE.NEVER] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#ED5565' });
connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: 'gray' });

let connectorHoverStyle = {
  strokeStyle: '#216477'
};

let endpointHoverStyle = {
  fillStyle: '#216477',
  strokeStyle: '#216477'
};

let outputStyle = {
  endpoint: 'Dot',
  paintStyle: {
    fillStyle: '#1AB394',
    radius: 10,
    lineWidth: 2
  },
  isSource: true,
  connector: ['Bezier', { curviness: 75 }],
  connectorStyle: connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN],
  hoverPaintStyle: endpointHoverStyle,
  connectorHoverStyle: connectorHoverStyle,
  maxConnections: -1
};

let inputStyle = {
  endpoint: 'Rectangle',
  paintStyle: {
    fillStyle: '#1AB394'
  },
  hoverPaintStyle: endpointHoverStyle,
  dropOptions: {
    hoverClass: 'hover',
    activeClass: 'active'
  },
  isTarget: true,
  maxConnections: 1
};

/* @ngInject */
function GraphPanelRendererService($rootScope, $document) {

  const nodeIdPrefix = 'node-';
  const nodeIdPrefixLength = nodeIdPrefix.length;

  let that = this;
  let internal = {};

  internal.currentZoomRatio = 1.0;

  internal.getAllInternalElementsPosition = function getAllInternalElementsPosition () {
    let elementsToFit = jsPlumb.getContainer().children;
    let elementsToFitPositions = _.map(elementsToFit, (el) => {
      let elementDimensions = el.getBoundingClientRect();
      return {
        top:    el.offsetTop,
        left:   el.offsetLeft,
        right:  el.offsetLeft  + elementDimensions.width,
        bottom: el.offsetTop   + elementDimensions.height
      };
    });

    return elementsToFitPositions;
  };

  that.getPseudoContainerPosition = function getPseudoContainerPosition () {
    let elementsToFitPositions = internal.getAllInternalElementsPosition();
    return {
      topMost: Math.min.apply(Math, _.map(elementsToFitPositions, (elPos) => elPos.top )),
      leftMost: Math.min.apply(Math, _.map(elementsToFitPositions, (elPos) => elPos.left )),
      rightMost: Math.max.apply(Math, _.map(elementsToFitPositions, (elPos) => elPos.right )),
      bottomMost: Math.max.apply(Math, _.map(elementsToFitPositions, (elPos) => elPos.bottom ))
    };
  };

  that.getPseudoContainerCenter = function getPseudoContainerCenter () {
    let pseudoContainerPosition = that.getPseudoContainerPosition();
    return {
      y: pseudoContainerPosition.topMost  + ((pseudoContainerPosition.bottomMost - pseudoContainerPosition.topMost) / 2),
      x: pseudoContainerPosition.leftMost + ((pseudoContainerPosition.rightMost - pseudoContainerPosition.leftMost) / 2)
    };
  };

  that.getZoomRatio = function getZoomRatio () {
    return jsPlumb.getZoom();
  };

  that.setZoom = function setZoom (zoomRatio) {
    let instance = jsPlumb;
    internal.currentZoomRatio = zoomRatio;
    instance.setZoom(zoomRatio);
    instance.repaintEverything();
  };

  internal.reset = function reset() {
    jsPlumb.deleteEveryEndpoint();
    jsPlumb.unbind('connection');
    jsPlumb.unbind('connectionDetached');
    jsPlumb.unbind('connectionMoved');
    jsPlumb.unbind('connectionDrag');
    jsPlumb.setZoom(internal.currentZoomRatio, true);
  };

  that.init = function init() {
    internal.reset();
    jsPlumb.setContainer($document[0].querySelector('.flowchart-paint-area'));
    jsPlumb.importDefaults({
      DragOptions: {
        cursor: 'pointer',
        zIndex: 2000
      }
    });
    that.bindEdgeEvent();
  };

  internal.getNodeById = function getNodeById(id) {
    return document.querySelector('#' + nodeIdPrefix + id);
  };

  that.repaintEverything = function redrawEverything() {
    jsPlumb.repaintEverything();
  };

  that.setExperiment = function setExperiment(experiment) {
    internal.experiment = experiment;
  };

  that.clearExperiment = function clearExperiment() {
    internal.reset();
    internal.experiment = null;
  };

  that.removeNode = function removeNode(nodeId) {
    let node = internal.getNodeById(nodeId);
    jsPlumb.remove(node);
  };

  that.renderPorts = function renderPorts() {
    let nodes = internal.experiment.getNodes();
    for (let nodeId in nodes) {
      if (nodes.hasOwnProperty(nodeId)) {
        let node = internal.getNodeById(nodeId);
        that.addOutputPoint(node, nodes[nodeId].output, nodes[nodeId]);
        that.addInputPoint(node, nodes[nodeId].input);
      }
    }
  };

  that.renderEdges = function renderEdges() {
    jsPlumb.detachEveryConnection();
    let edges = internal.experiment.getEdges();
    let outputPrefix = 'output';
    let inputPrefix = 'input';
    for (let id in edges) {
      if (edges.hasOwnProperty(id)) {
        let edge = edges[id];
        let connection = jsPlumb.connect({
          uuids: [
            outputPrefix + '-' + edge.startPortId + '-' + edge.startNodeId,
            inputPrefix + '-' + edge.endPortId + '-' + edge.endNodeId
          ]
        });
        connection.setParameter('edgeId', edge.id);
      }
    }
    that.changeEdgesPaintStyles();
  };

  that.changeEdgesPaintStyles = function changeEdgesStates() {
    let connections = jsPlumb.getConnections();
    let edges = internal.experiment.getEdges();
    for (let id in edges) {
      let edge = edges[id];
      let connection = _.find(connections, (connection) => connection.getParameter('edgeId') === edge.id );

      if (!_.isUndefined(connection)) {
        connection.setPaintStyle(connectorPaintStyles[edge.state]);
      }
    }
  };

  that.portContextMenuHandler = function portContextMenuHandler(port, event) {
    $rootScope.$broadcast('OutputPort.RIGHT_CLICK', {
      reference: port,
      event: event
    });
  };

  that.outputClickHandler = function outputClickHandler() {
    $rootScope.$broadcast('OutputPort.LEFT_CLICK');
  };

  internal.broadcastHoverEvent = function (eventName, portElement, portObject) {
    $rootScope.$broadcast(eventName, {
      portElement: portElement,
      portObject: portObject
    });
  };

  that.addOutputPoint = function addOutputPoint(nodeElement, ports, nodeObj) {
    let anchors = (ports.length === 1) ?
      ['BottomCenter'] :
      ['BottomLeft', 'BottomCenter', 'BottomRight'];

    for (let i = 0; i < ports.length; i++) {
      let port = jsPlumb.addEndpoint(nodeElement, outputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });

      port.setParameter('portIndex', i);
      port.setParameter('nodeId', nodeObj.id);

      port.bind('contextmenu', that.portContextMenuHandler);
      port.bind('click', that.outputClickHandler);

      port.bind('mouseover', (endpoint) => {
        internal.broadcastHoverEvent('OutputPoint.MOUSEOVER', endpoint.canvas, ports[i]);
      });

      port.bind('mouseout', (endpoint) => {
        internal.broadcastHoverEvent('OutputPoint.MOUSEOUT', endpoint.canvas, ports[i]);
      });
    }
  };

  that.inputClickHandler = function inputClickHandler() {
    $rootScope.$broadcast('InputPoint.CLICK');
  };

  that.addInputPoint = function addInputPoint(node, ports) {
    let anchors = (ports.length === 1) ?
      ['TopCenter'] :
      ['TopLeft', 'TopCenter', 'TopRight'];

    for (let i = 0; i < ports.length; i++) {
      let port = jsPlumb.addEndpoint(node, inputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });

      port.setParameter('portIndex', i);

      port.bind('click', that.inputClickHandler);

      port.bind('mouseover', (endpoint) => {
        internal.broadcastHoverEvent('InputPoint.MOUSEOVER', endpoint.canvas, ports[i]);
      });

      port.bind('mouseout', (endpoint) => {
        internal.broadcastHoverEvent('InputPoint.MOUSEOUT', endpoint.canvas, ports[i]);
      });
    }
  };

  that.bindEdgeEvent = function bindEdgeEvents() {
    jsPlumb.bind('connection', (info, originalEvent) => {
      if (!originalEvent) {
        return;
      }

      let data = {
          'from': {
            'nodeId': info.sourceId.slice(nodeIdPrefixLength),
            'portIndex': info.sourceEndpoint.getParameter('portIndex')
          },
          'to': {
            'nodeId': info.targetId.slice(nodeIdPrefixLength),
            'portIndex': info.targetEndpoint.getParameter('portIndex')
          }
        };
      let edge = internal.experiment.createEdge(data);

      info.connection.setParameter('edgeId', edge.id);

      $rootScope.$broadcast(Edge.CREATE, {edge: edge});
    });

    jsPlumb.bind('connectionDetached', (info, originalEvent) => {
      let edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge && info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
        $rootScope.$broadcast(Edge.REMOVE, {
          edge: edge
        });
      }
    });

    jsPlumb.bind('connectionMoved', (info) => {
      let edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge) {
        $rootScope.$broadcast(Edge.REMOVE, {
          edge: edge
        });
      }
    });

    jsPlumb.bind('connectionDrag', () => {
      $rootScope.$broadcast(Edge.DRAG);
    });
  };

  return that;
}

exports.function = GraphPanelRendererService;

exports.inject = function (module) {
  module.service('GraphPanelRendererService', GraphPanelRendererService);
};
