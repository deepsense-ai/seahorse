/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var jsPlumb = require('jsPlumb'),
  Edge = require('../../common-objects/common-edge.js');

var connectorPaintStyleDefault = {
  lineWidth: 2,
  outlineColor: 'white',
  outlineWidth: 2
};

/**
 * Maps edge's state to its style object
 *
 * @type {object}
 */
var connectorPaintStyles = {};
connectorPaintStyles[Edge.STATE_TYPE.ALWAYS] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#61B7CF' });
connectorPaintStyles[Edge.STATE_TYPE.MAYBE] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#F8AC59' });
connectorPaintStyles[Edge.STATE_TYPE.NEVER] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#ED5565' });
connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN] = _.defaults({}, connectorPaintStyleDefault, { strokeStyle: 'gray' });

var connectorHoverStyle = {
  strokeStyle: '#216477'
};

var endpointHoverStyle = {
  fillStyle: '#216477',
  strokeStyle: '#216477'
};

var outputStyle = {
  endpoint: 'Dot',
  paintStyle: {
    fillStyle: '#7AB02C',
    radius: 10,
    lineWidth: 2
  },
  isSource: true,
  connector: ['Bezier'],
  connectorStyle: connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN],
  hoverPaintStyle: endpointHoverStyle,
  connectorHoverStyle: connectorHoverStyle,
  maxConnections: -1
};

var inputStyle = {
  endpoint: 'Rectangle',
  paintStyle: {
    fillStyle: '#7AB02C'
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

  var that = this;
  var internal = {};

  that.getAllInternalElementsPosition = function getAllInternalElementsPosition () {
    var elementsToFit = jsPlumb.getContainer().children;
    var elementsToFitPositions = _.map(elementsToFit, function (el) {
      var elementDimensions = el.getBoundingClientRect();

      return {
        top:    el.offsetTop,
        left:   el.offsetLeft,
        right:  el.offsetLeft  + elementDimensions.width,
        bottom: el.offsetTop   + elementDimensions.height
      };
    });

    return elementsToFitPositions;
  };

  that.getPseudoPosition = function getPseudoPosition () {
    var elementsToFitPositions = that.getAllInternalElementsPosition();

    return {
      topmost: Math.min.apply(Math, _.map(elementsToFitPositions, (elPos) => { return elPos.top;     })),
      leftmost: Math.min.apply(Math, _.map(elementsToFitPositions, (elPos) => { return elPos.left;    })),
      rightmost: Math.max.apply(Math, _.map(elementsToFitPositions, (elPos) => { return elPos.right;   })),
      bottommost: Math.max.apply(Math, _.map(elementsToFitPositions, (elPos) => { return elPos.bottom;  }))
    };
  };

  that.getPseudoContainerCenter = function getPseudoContainerCenter () {
    var pseudoContainerPosition = that.getPseudoPosition();

    console.log('CENTER OF PSEUDO: ', {
      y: pseudoContainerPosition.topmost  + ((pseudoContainerPosition.bottommost - pseudoContainerPosition.topmost) / 2),
      x: pseudoContainerPosition.leftmost + ((pseudoContainerPosition.rightmost - pseudoContainerPosition.leftmost) / 2)
    });

    return {
      y: pseudoContainerPosition.topmost  + ((pseudoContainerPosition.bottommost - pseudoContainerPosition.topmost) / 2),
      x: pseudoContainerPosition.leftmost + ((pseudoContainerPosition.rightmost - pseudoContainerPosition.leftmost) / 2)
    };
  };

  that.getZoom = function getZoom () {
    return jsPlumb.getZoom();
  };

  that.getNewCenterOf = function getNewCenterOf (ratio, centerFrom, centerTo) {
    return {
      y: ratio * centerTo.y + (1 - ratio) * centerFrom.y,
      x: ratio * centerTo.x + (1 - ratio) * centerFrom.x
    };
  };

  that.setZoom = function setZoom (zoom, transformOrigin) {
    transformOrigin = transformOrigin || [0.5, 0.5];
    var instance = jsPlumb;
    var el = instance.getContainer();
    if (!el) {return false;}
    var p = ['webkit', 'moz', 'ms', 'o'],
      s = 'scale(' + zoom + ')',
      oString = (transformOrigin[0] * 100) + '% ' + (transformOrigin[1] * 100) + '%';

    for (var i = 0; i < p.length; i++) {
      el.style[p[i] + 'Transform'] = s;
      el.style[p[i] + 'TransformOrigin'] = oString;
    }

    el.style.transform = s;
    el.style.transformOrigin = oString;

    instance.setZoom(zoom, true);
    instance.repaintEverything();

    $rootScope.$broadcast('Zoom');
  };

  that.getDifferenceAfterZoom = function getDifferenceAfterZoom (container, property, previous) {
    return (
      container.getBoundingClientRect()[property] -
      previous || container['client' + (property.slice(0, 1).toUpperCase() + property.slice(1))]
    ) / 2;
  };

  // TODO do not move beyond borders
  internal.moveElement = function moveElement (element, movement) {
    console.log(movement);
    $(element).animate({
      top: movement.y,
      left: movement.x
    }, 0);
  };

  that.setZero = function setZero (direction) {
    var container = jsPlumb.getContainer();

    container.style[direction] = 0;

    if (that.getZoom() !== 1) {
      let directionToDimension = direction === 'left' ? 'width' : 'height';
      container.style[direction] = that.getDifferenceAfterZoom(container, directionToDimension) + 'px';
    }

    $rootScope.$broadcast('GraphPanel.ZERO');
  };

  that.setCenter = function setCenter (centerTo) {
    var container                 = jsPlumb.getContainer();
    var containerParent           = container.parentNode;

    var centerOfMask              = {
      y: containerParent.clientHeight / 2,
      x: containerParent.clientWidth / 2
    };

    /*var centerOfPseudoContainer   = {
      y: pseudoContainer.topmost  + ((pseudoContainer.bottommost - pseudoContainer.topmost) / 2),
      x: pseudoContainer.leftmost + ((pseudoContainer.rightmost - pseudoContainer.leftmost) / 2)
    };*/

    var movement = {
      y: centerOfMask.y - centerTo.y,
      x: centerOfMask.x - centerTo.x
    };

    internal.moveElement(container, movement);

    $rootScope.$broadcast('GraphPanel.CENTERED');
  };

  that.init = function init() {
    // jsPlumb.reset();
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
    jsPlumb.deleteEveryEndpoint();
    jsPlumb.unbind('connection');
    jsPlumb.unbind('connectionDetached');
    jsPlumb.unbind('connectionMoved');
    jsPlumb.unbind('connectionDrag');
    jsPlumb.setZoom(1, true);
    internal.experiment = null;
  };

  that.removeNode = function removeNode(nodeId) {
    var node = internal.getNodeById(nodeId);
    jsPlumb.remove(node);
  };

  that.renderPorts = function renderPorts() {
    var nodes = internal.experiment.getNodes();
    for (var nodeId in nodes) {
      if (nodes.hasOwnProperty(nodeId)) {
        var node = internal.getNodeById(nodeId);
        that.addOutputPoint(node, nodes[nodeId].output, nodes[nodeId]);
        that.addInputPoint(node, nodes[nodeId].input);
      }
    }
  };

  that.renderEdges = function renderEdges() {
    jsPlumb.detachEveryConnection();
    var edges = internal.experiment.getEdges();
    var outputPrefix = 'output';
    var inputPrefix = 'input';
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

  internal.broadcastHoverEvent = (eventName, portElement, portObject) => {
    $rootScope.$broadcast(eventName, {
      portElement: portElement,
      portObject: portObject
    });
  };

  that.addOutputPoint = function addOutputPoint(nodeElement, ports, nodeObj) {
    var anchors = ['BottomCenter', 'BottomLeft', 'BottomRight'];
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
    var anchors = ['TopCenter', 'TopLeft', 'TopRight'];
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
      var edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge && info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
        $rootScope.$broadcast(Edge.REMOVE, {edge: edge});
      }
    });

    jsPlumb.bind('connectionMoved', function (info) {
      var edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge) {
        $rootScope.$broadcast(Edge.REMOVE, {edge: edge});
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
