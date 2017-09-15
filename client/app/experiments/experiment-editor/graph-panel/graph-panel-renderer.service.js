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
function GraphPanelRendererService($rootScope) {

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

  that.setExperiment = function setExperiment(experiment) {
    internal.experiment = experiment;
  };

  that.clearExperiment = function clearExperiment() {
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

exports.function = GraphPanelRendererService;

exports.inject = function (module) {
  module.service('GraphPanelRendererService', GraphPanelRendererService);
};
