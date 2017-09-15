/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var jsPlumb = require('jsPlumb');

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


function DrawingService() {

  var that = this;
  var internal = {};

  internal.init = function init() {
    jsPlumb.getInstance({
      DragOptions: {
        cursor: 'pointer',
        zIndex: 2000
      },
      Container: 'flowchart-box'
    });
  };

  internal.getNodeById = function getNodeById(id) {
    var idPrefix = '#node-';
    return document.querySelector(idPrefix + id);
  };

  that.renderExperiment = function renderExperiment(experiment) {
    internal.experiment = experiment;
  };

  that.renderPorts = function renderPorts() {
    var nodes = internal.experiment.getNodes();
    for (var i = 0; i < nodes.length; i++) {
      var node = internal.getNodeById(nodes[i].id);
      that.addOutputPoint(node, nodes[i].output);
      that.addInputPoint(node, nodes[i].input);
    }
  };

  that.renderConnections = function renderConnections() {
    var edges = internal.experiment.getEdges();
    var outputPrefix = 'output';
    var inputPrefix = 'input';

    for (var i = 0; i < edges.length; i++) {
      var edge = edges[i];
      jsPlumb.connect({
        uuids: [outputPrefix + '-' + edge.startPortId + '-' + edge.startNodeId,
          inputPrefix + '-' + edge.endPortId + '-' + edge.endNodeId
        ]
      });
      console.log(edges[i]);
    }
  };

  that.addOutputPoint = function addOutputPoint(node, ports) {
    var anchors = ['BottomCenter', 'BottomLeft', 'BottomRight'];
    for (var i = 0; i < ports.length; i++) {
      jsPlumb.addEndpoint(node, outputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });
    }
  };

  that.addInputPoint = function addInputPoint(node, ports) {
    var anchors = ['TopCenter', 'TopLeft', 'TopRight'];
    for (var i = 0; i < ports.length; i++) {
      jsPlumb.addEndpoint(node, inputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });
      console.log(ports[i].id);
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

  internal.init();

  return that;

}

exports.function = DrawingService;

exports.inject = function (module) {
  module.service('DrawingService', DrawingService);
};

