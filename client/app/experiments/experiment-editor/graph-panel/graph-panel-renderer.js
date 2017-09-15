/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var jsPlumb = require('jsPlumb'),
  Edge = require('../../common-objects/common-edge.js');

var connectorPaintStyle = {
  lineWidth: 2,
  strokeStyle: '#61B7CF',
  outlineColor: 'white',
  outlineWidth: 2
};

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
  connectorStyle: connectorPaintStyle,
  hoverPaintStyle: endpointHoverStyle,
  connectorHoverStyle: connectorHoverStyle
};

var inputStyle = {
  endpoint: 'Rectangle',
  paintStyle: {
    fillStyle: '#7AB02C'
  },
  hoverPaintStyle: endpointHoverStyle,
  maxConnections: -1,
  dropOptions: {
    hoverClass: 'hover',
    activeClass: 'active'
  },
  isTarget: true
};

/* @ngInject */
function DrawingService($rootScope) {

  const nodeIdPrefix = 'node-';
  const nodeIdPrefixLength = nodeIdPrefix.length;

  var that = this;
  var internal = {};

  that.init = function init() {
    jsPlumb.reset();
    jsPlumb.setContainer('flowchart-box');
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

  that.renderExperiment = function renderExperiment(experiment) {
    internal.experiment = experiment;
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
        that.addOutputPoint(node, nodes[nodeId].output);
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
      var edge = edges[id];
      var connection = jsPlumb.connect({
          uuids: [
            outputPrefix + '-' + edge.startPortId + '-' + edge.startNodeId,
            inputPrefix + '-' + edge.endPortId + '-' + edge.endNodeId
          ]
        });
      connection.setParameter('edgeId', edge.id);
    }
  };

  that.contextMenuHandler = function contextMenuHandler(endPoint, event) {
    $rootScope.$broadcast('OutputPoint.CONTEXTMENU', {
      event: event,
      thisPoint: {
        'type': 'endPoint',
        'reference': endPoint
      }
    });
  };

  that.outputClickHandler = function clickHandler () {
    $rootScope.$broadcast('OutputPoint.CLICK');
  };

  that.addOutputPoint = function addOutputPoint(node, ports) {
    var anchors = ['BottomCenter', 'BottomLeft', 'BottomRight'];
    for (let i = 0; i < ports.length; i++) {
      let port = jsPlumb.addEndpoint(node, outputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });
      port.setParameter('portIndex', i);

      port.bind('contextmenu', that.contextMenuHandler);
      port.bind('click', that.outputClickHandler);
    }
  };

  that.inputClickHandler = function inputClickHandler () {
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
    }
  };

  that.addEndpoint = function addEndpoint(node, place) {
    var inputStyle = {
      endpoint: 'Dot',
      paintStyle: {
        fillStyle: '#7AB02C',
        radius: 10,
        lineWidth: 2
      },
      isSource: true,
      connector: ['Bezier'],
      connectorStyle: connectorPaintStyle,
      hoverPaintStyle: endpointHoverStyle,
      connectorHoverStyle: connectorHoverStyle
    };

    jsPlumb.addEndpoint(node, inputStyle, {
      anchor: place
    });
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
        },
        edge = internal.experiment.createEdge(data);
      info.connection.setParameter('edgeId', edge.id);
      $rootScope.$broadcast(Edge.CREATE, {edge: edge});
    });

    jsPlumb.bind('connectionDetached', (info, originalEvent) => {
      var edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      if (info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
        $rootScope.$broadcast(Edge.REMOVE, {edge: edge});
      }
    });

    jsPlumb.bind('connectionMoved', function (info) {
      var edge = internal.experiment.getEdgeById(info.connection.getParameter('edgeId'));
      $rootScope.$broadcast(Edge.REMOVE, {edge: edge});
    });

    jsPlumb.bind('connectionDrag', () => {
      $rootScope.$broadcast(Edge.DRAG);
    });
  };

  return that;

}

exports.function = DrawingService;

exports.inject = function (module) {
  module.service('DrawingService', DrawingService);
};
