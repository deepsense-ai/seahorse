'use strict';

// TODO Remove jshint ignore and refactor code.
/* jshint ignore:start */

import jsPlumb from 'jsplumb';

const connectorPaintStyleDefault = {
  lineWidth: 2,
  outlineColor: 'white',
  outlineWidth: 2
};

const connectorHoverStyle = {
  strokeStyle: '#216477'
};

const inputStyle = {
  endpoint: 'Rectangle',
  dropOptions: {
    hoverClass: 'hover',
    activeClass: 'active'
  },
  isTarget: true,
  maxConnections: 1
};


const inputAnchorByPosition = {
  left: 'TopLeft',
  center: 'TopCenter',
  right: 'TopRight'
};

const outputAnchorByPosition = {
  left: 'BottomLeft',
  center: 'BottomCenter',
  right: 'BottomRight'
};

/* @ngInject */
function GraphPanelRendererService($rootScope, $document, Edge, $timeout, Report,
  DeepsenseCycleAnalyser, NotificationService, GraphPanelStyler) {

  //TODO remove this service when all functionalities will be moved to canvas.adapter.service

  const connectorPaintStyles = {
    [Edge.STATE_TYPE.ALWAYS]: _.defaults({}, connectorPaintStyleDefault, {
      strokeStyle: '#61B7CF'
    }), [Edge.STATE_TYPE.MAYBE]: _.defaults({}, connectorPaintStyleDefault, {
      strokeStyle: '#F8AC59'
    }), [Edge.STATE_TYPE.NEVER]: _.defaults({}, connectorPaintStyleDefault, {
      strokeStyle: '#ED5565'
    }), [Edge.STATE_TYPE.UNKNOWN]: _.defaults({}, connectorPaintStyleDefault, {
      strokeStyle: 'gray'
    })
  };

  const OUTPUT_STYLE = {
    endpoint: 'Dot',
    isSource: true,
    connector: ['Bezier', {
      curviness: 75
    }],
    connectorStyle: connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN],
    connectorHoverStyle: connectorHoverStyle,
    maxConnections: -1
  };

  const nodeIdPrefix = 'node-';
  const nodeIdPrefixLength = nodeIdPrefix.length;

  let that = this;
  let internal = {
    currentZoomRatio: 1.0,
    disabledMode: false,

    areEdgesDetachable() {
      return !internal.disabledMode;
    },

    reset() {
      jsPlumb.deleteEveryEndpoint();
      jsPlumb.unbind('connection');
      jsPlumb.unbind('connectionDetached');
      jsPlumb.unbind('connectionMoved');
      jsPlumb.unbind('connectionDrag');
      //jsPlumb.setZoom(internal.currentZoomRatio, true);
    },

    getNodeById(id) {
      return document.querySelector('#' + nodeIdPrefix + id);
    },

    emitMouseOverEvent(eventName, portElement, portObject) {
      $rootScope.$emit(eventName, {
        portElement: portElement,
        portObject: portObject
      });
    }
  };

  that.init = function init(workflow) {
    internal.reset();
    jsPlumb.setContainer($document[0].querySelector('.flowchart-paint-area'));
    jsPlumb.importDefaults({
      DragOptions: {
        cursor: 'pointer',
        zIndex: 2000
      }
    });
    that._bindEdgeEvent(workflow);
  };

  that.getZoomRatio = () => jsPlumb.getZoom();

  that.setZoom = function setZoom(zoomRatio) {
    let instance = jsPlumb;
    internal.currentZoomRatio = zoomRatio;
    instance.setZoom(zoomRatio);
    instance.repaintEverything();
  };

  that.repaintEverything = function repaintEverything() {
    jsPlumb.repaintEverything();
  };

  that.clearWorkflow = function clearWorkflow() {
    internal.reset();
  };

  that.removeNode = function removeNode(nodeId) {
    let node = internal.getNodeById(nodeId);
    jsPlumb.remove(node);
  };

  // TODO That could be removed. Just call rerender.
  that.removeNodes = (nodes) => {
    _.each(nodes, (node) => {
      that.removeNode(node);
    });
  };

  that.rerender = function rerender(workflow, selectedPort) {
    that.init(workflow);
    that.renderPorts(workflow, selectedPort);
    that.renderEdges(workflow);
    that.repaintEverything();
  };

  that.renderPorts = function renderPorts(workflow, selectedPort) {
    let nodes = workflow.getNodes();
    for (let nodeId in nodes) {
      if (nodes.hasOwnProperty(nodeId)) {
        const nodeElement = internal.getNodeById(nodeId);
        const nodeObject = nodes[nodeId];
        that._addOutputPoints(workflow, nodeElement, nodeObject.output, nodes[nodeId], selectedPort);
        that._addInputPoints(nodeElement, nodeObject.input);
      }
    }
  };

  that.renderEdges = function renderEdges(workflow) {
    jsPlumb.detachEveryConnection();
    let edges = workflow.getEdges();
    let outputPrefix = 'output';
    let inputPrefix = 'input';

    for (let id in edges) {
      if (edges.hasOwnProperty(id)) {
        let edge = edges[id];
        let connection = jsPlumb.connect({
          uuids: [
            outputPrefix + '-' + edge.startPortId + '-' + edge.startNodeId,
            inputPrefix + '-' + edge.endPortId + '-' + edge.endNodeId
          ],
          detachable: internal.areEdgesDetachable()
        });
        connection.setParameter('edgeId', edge.id);
      }
    }
    that.changeEdgesPaintStyles(workflow);
  };

  that.changeEdgesPaintStyles = function changeEdgesPaintStyles(workflow) {
    let connections = jsPlumb.getConnections();
    let edges = workflow.getEdges();
    for (let id in edges) {
      if (edges.hasOwnProperty(id)) {
        let edge = edges[id];
        let connection = _.find(connections, (connection) => connection.getParameter('edgeId') === edge.id);

        if (!_.isUndefined(connection)) {
          connection.setPaintStyle(connectorPaintStyles[edge.state]);
        }
      }
    }
  };

  that._addOutputPoints = function (workflow, nodeElement, ports, nodeObj, selectedPort) {
    ports.forEach((port) => that._addOutputPoint(workflow, nodeElement, port, nodeObj, selectedPort));
  };

  that._addOutputPoint = function (workflow, nodeElement, port, nodeObj, selectedPort) {
    const reportEntityId = nodeObj.getResult(port.index);
    const hasReport = Report.hasReportEntity(reportEntityId);

    let outputStyle = angular.copy(OUTPUT_STYLE);

    if (hasReport) {
      // HACK We would like to use jsPlumb types for all styling. Unfortunately it seems like jsPlumb
      // ignores CSS classes from type in endpoints when dragging connection. Explicit cssClass works though.
      outputStyle = _.assign(outputStyle, {
        cssClass: GraphPanelStyler.getTypes().outputWithReport.cssClass
      });
    }

    if (internal.disabledMode) {
      outputStyle.isSource = false;
    }

    const jsPlumbPort = jsPlumb.addEndpoint(nodeElement, outputStyle, {
      anchor: outputAnchorByPosition[port.portPosition],
      uuid: port.id
    });

    jsPlumbPort.setParameter('portIndex', port.index);
    jsPlumbPort.setParameter('nodeId', nodeObj.id);

    GraphPanelStyler.styleOutputEndpointDefault(jsPlumbPort, hasReport);

    if(port === selectedPort) {
      GraphPanelStyler.styleSelectedOutputEndpoint(jsPlumbPort);
    }

    const emitLeftClick = (clickedPort, event) => {
      if (hasReport) {
        $rootScope.$broadcast('OutputPort.LEFT_CLICK', {
          reference: clickedPort,
          portObject: port,
          event: event
        });
      }
    };
    // FIXME Quickfix to make reports browseable in disabled mode.
    // There is a conflict between multiselection and output port click when isSource = false.
    // For isSource=true works 'click' event
    // For isSource=false works 'mousedown' event
    jsPlumbPort.bind('click', emitLeftClick);
    jsPlumbPort.bind('mousedown', emitLeftClick);

    jsPlumbPort.bind('mouseover', (endpoint) => {
      internal.emitMouseOverEvent('OutputPoint.MOUSEOVER', endpoint.canvas, port);
    });

    jsPlumbPort.bind('mouseout', () => {
      $rootScope.$emit('OutputPoint.MOUSEOUT');
    });
  };

  that._addInputPoints = function(node, ports) {
    ports.forEach((port) => that._addInputPoint(node, port));
  };

  that._addInputPoint = function(node, port) {
    const jsPlumbPort = jsPlumb.addEndpoint(node, inputStyle, {
      anchor: inputAnchorByPosition[port.portPosition],
      uuid: port.id
    });

    jsPlumbPort.setParameter('portIndex', port.index);

    GraphPanelStyler.styleInputEndpointDefault(jsPlumbPort);

    jsPlumbPort.bind('click', () => {
      $rootScope.$broadcast('InputPoint.CLICK');
    });

    jsPlumbPort.bind('mouseover', (endpoint) => {
      internal.emitMouseOverEvent('InputPoint.MOUSEOVER', endpoint.canvas, port);
    });

    jsPlumbPort.bind('mouseout', () => {
      $rootScope.$broadcast('InputPoint.MOUSEOUT');
    });
  };

  that._bindEdgeEvent = function _bindEdgeEvent(workflow) {
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
      let edge = workflow.createEdge(data);

      info.connection.setParameter('edgeId', edge.id);

      $rootScope.$broadcast(Edge.CREATE, {
        edge: edge
      });

      if (DeepsenseCycleAnalyser.cycleExists(workflow)) {
        NotificationService.showError({
          title: 'Error',
          message: 'You cannot create a cycle in the graph!'
        }, 'A cycle in the graph has been detected!');

        $timeout(() => {
          $rootScope.$broadcast(Edge.REMOVE, {
            edge: workflow.getEdgeById(info.connection.getParameter('edgeId'))
          });

          jsPlumb.detach(info.connection);
        }, 0, false);
      }
    });

    jsPlumb.bind('connectionDetached', (info, originalEvent) => {
      if (workflow) {
        let edge = workflow.getEdgeById(info.connection.getParameter('edgeId'));
        if (edge && info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
          $rootScope.$broadcast(Edge.REMOVE, {
            edge: edge
          });
        }
      }
    });

    jsPlumb.bind('connectionMoved', (info) => {
      let edge = workflow.getEdgeById(info.connection.getParameter('edgeId'));
      if (edge) {
        $rootScope.$broadcast(Edge.REMOVE, {
          edge: edge
        });
      }
    });
  };

  that.setDisabledMode = (disabledMode) => {
    internal.disabledMode = disabledMode;
  };

  that.disablePortHighlightings = function disablePortHighlightings(workflow) {
    ConnectionHinterService.disablePortHighlighting(workflow);
    ConnectionHinterService.disableOperationsHighlighting();
  };

  that.internal = internal;

  return that;
}

exports.inject = function(module) {
  module.service('GraphPanelRendererService', GraphPanelRendererService);
};

/* jshint ignore:end */
