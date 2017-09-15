'use strict';

// TODO Remove jshint ignore and refactor code.
/* jshint ignore:start */

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

/* beautify preserve:start */
import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';
/* beautify preserve:end */

/* @ngInject */
function GraphPanelRendererService($rootScope, $document, Edge, $timeout, Report,
  DeepsenseCycleAnalyser, NotificationService, ConnectionHinterService, GraphPanelStyler) {

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

  jsPlumb.registerEndpointTypes(GraphPanelStyler.getTypes());

  let that = this;
  let internal = {
    currentZoomRatio: 1.0,
    renderMode: null,

    edgesAreDetachable() {
      return internal.renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE;
    },

    reset() {
      jsPlumb.deleteEveryEndpoint();
      jsPlumb.unbind('connection');
      jsPlumb.unbind('connectionDetached');
      jsPlumb.unbind('connectionMoved');
      jsPlumb.unbind('connectionDrag');
      jsPlumb.setZoom(internal.currentZoomRatio, true);
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
    internal.renderMode = null;
    internal.reset();
  };

  that.removeNode = function removeNode(nodeId) {
    let node = internal.getNodeById(nodeId);
    jsPlumb.remove(node);
  };

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

    if (selectedPort) {
      let previouslySelected = jsPlumb.getEndpoint(selectedPort);
      // This might be null after opening inner workflow.
      if (previouslySelected) {
        previouslySelected.addType('selected');
      }
    }
  };

  that.renderPorts = function renderPorts(workflow, selectedPort) {
    let nodes = workflow.getNodes();
    for (let nodeId in nodes) {
      if (nodes.hasOwnProperty(nodeId)) {
        let node = internal.getNodeById(nodeId);
        that._addOutputPoints(workflow, node, nodes[nodeId].output, nodes[nodeId], selectedPort);
        that._addInputPoints(node, nodes[nodeId].input);
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
          detachable: internal.edgesAreDetachable()
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

  that._addOutputPoints = function _addOutputPoints(workflow, nodeElement, ports, nodeObj, selectedPort) {
    let anchors = (ports.length === 1) ? ['BottomCenter'] : ['BottomLeft', 'BottomCenter', 'BottomRight'];

    for (let i = 0; i < ports.length; i++) {
      let reportEntityId = nodeObj.getResult(i);
      let hasReport = Report.hasReportEntity(reportEntityId);

      let outputStyle = angular.copy(OUTPUT_STYLE);

      if (hasReport) {
        // HACK We would like to use jsPlumb types for all styling. Unfortunately it seems like jsPlumb
        // ignores CSS classes from type in endpoints when dragging connection. Explicit cssClass works though.
        outputStyle = _.assign(outputStyle, {
          cssClass: GraphPanelStyler.getTypes().outputWithReport.cssClass
        });
      }

      if (internal.renderMode === GraphPanelRendererBase.RUNNING_RENDER_MODE) {
        outputStyle.isSource = false;
      }

      let jsPlumbPort = jsPlumb.addEndpoint(nodeElement, outputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });

      jsPlumbPort.setParameter('portIndex', i);
      jsPlumbPort.setParameter('nodeId', nodeObj.id);

      GraphPanelStyler.styleOutputEndpointDefault(jsPlumbPort, hasReport);

      if(ports[i] === selectedPort) {
        GraphPanelStyler.styleSelectedOutputEndpoint(jsPlumbPort);
      }

      // FIXME Quickfix to make reports browseable in read-only mode.
      // There is a conflict between multiselection and output port click when isSource = false.
      let eventForLeftClick = internal.renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? 'click' : 'mousedown';
      jsPlumbPort.bind(eventForLeftClick, (port, event) => {
        if (hasReport) {
          let thisPortObject = ports[i];
          $rootScope.$broadcast('OutputPort.LEFT_CLICK', {
            reference: port,
            portObject: thisPortObject,
            event: event
          });
        }
      });

      jsPlumbPort.bind('mouseover', (endpoint) => {
        internal.emitMouseOverEvent('OutputPoint.MOUSEOVER', endpoint.canvas, ports[i]);
      });

      jsPlumbPort.bind('mouseout', (endpoint) => {
        $rootScope.$emit('OutputPoint.MOUSEOUT');
      });
    }
  };

  that._addInputPoints = function _addInputPoints(node, ports) {
    let anchors = (ports.length === 1) ? ['TopCenter'] : ['TopLeft', 'TopCenter', 'TopRight'];

    for (let i = 0; i < ports.length; i++) {
      let port = jsPlumb.addEndpoint(node, inputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });

      port.setParameter('portIndex', i);

      GraphPanelStyler.styleInputEndpointDefault(port);

      port.bind('click', () => {
        $rootScope.$broadcast('InputPoint.CLICK');
      });

      port.bind('mouseover', (endpoint) => {
        internal.emitMouseOverEvent('InputPoint.MOUSEOVER', endpoint.canvas, ports[i]);
      });

      port.bind('mouseout', (endpoint) => {
        $rootScope.$broadcast('InputPoint.MOUSEOUT');
      });
    }
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

    jsPlumb.bind('connectionDrag', (connection) => {
      $rootScope.$broadcast(Edge.DRAG);

      /* During detaching an edge, hints should be displayed as well */
      if (internal.renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE &&
        _.isArray(connection.endpoints)
      ) {
        let port = connection.endpoints[0];
        ConnectionHinterService.highlightMatchedAndDismatchedPorts(workflow, port);
        ConnectionHinterService.highlightOperations(workflow, port);
      }
    });

  };

  that.setRenderMode = function setRenderMode(renderMode) {
    if (renderMode !== GraphPanelRendererBase.EDITOR_RENDER_MODE &&
      renderMode !== GraphPanelRendererBase.RUNNING_RENDER_MODE) {
      throw `render mode should be either 'editor' or 'report'`;
    }

    internal.renderMode = renderMode;
  };

  that.disablePortHighlightings = function disablePortHighlightings(workflow) {
    ConnectionHinterService.disablePortHighlighting(workflow);
    ConnectionHinterService.disableOperationsHighlighting();
  };

  return that;
}

exports.inject = function(module) {
  module.service('GraphPanelRendererService', GraphPanelRendererService);
};

/* jshint ignore:end */
