'use strict';

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

import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';
import { GraphPanelStyler } from './graph-panel-styler.js';

/* @ngInject */
function GraphPanelRendererService($rootScope, $document, Edge, $timeout,
                                   DeepsenseCycleAnalyser, NotificationService, ConnectionHinterService, WorkflowService, GraphNode)
{
  const connectorPaintStyles = {
    [Edge.STATE_TYPE.ALWAYS]: _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#61B7CF' }),
    [Edge.STATE_TYPE.MAYBE]: _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#F8AC59' }),
    [Edge.STATE_TYPE.NEVER]: _.defaults({}, connectorPaintStyleDefault, { strokeStyle: '#ED5565' }),
    [Edge.STATE_TYPE.UNKNOWN]: _.defaults({}, connectorPaintStyleDefault, { strokeStyle: 'gray' })
  };

  const OUTPUT_STYLE = {
    endpoint: 'Dot',
    isSource: true,
    connector: ['Bezier', { curviness: 75 }],
    connectorStyle: connectorPaintStyles[Edge.STATE_TYPE.UNKNOWN],
    connectorHoverStyle: connectorHoverStyle,
    maxConnections: -1
  };

  const nodeIdPrefix = 'node-';
  const nodeIdPrefixLength = nodeIdPrefix.length;

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

    broadcastHoverEvent(eventName, portElement, portObject) {
      $rootScope.$broadcast(eventName, {
        portElement: portElement,
        portObject: portObject
      });
    }
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

  that.getZoomRatio = () => jsPlumb.getZoom();

  that.setZoom = function setZoom (zoomRatio) {
    let instance = jsPlumb;
    internal.currentZoomRatio = zoomRatio;
    instance.setZoom(zoomRatio);
    instance.repaintEverything();
  };

  that.repaintEverything = function redrawEverything() {
    jsPlumb.repaintEverything();
  };

  that.clearWorkflow = function clearWorkflow() {
    internal.reset();
  };

  that.removeNode = function removeNode(nodeId) {
    let node = internal.getNodeById(nodeId);
    jsPlumb.remove(node);
  };

  that.renderPorts = function renderPorts() {
    let nodes = WorkflowService.getWorkflow().getNodes();
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
    let edges = WorkflowService.getWorkflow().getEdges();
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
    that.changeEdgesPaintStyles();
  };

  that.changeEdgesPaintStyles = function changeEdgesStates() {
    let connections = jsPlumb.getConnections();
    let edges = WorkflowService.getWorkflow().getEdges();
    for (let id in edges) {
      if (edges.hasOwnProperty(id)) {
        let edge = edges[id];
        let connection = _.find(connections, (connection) => connection.getParameter('edgeId') === edge.id );

        if (!_.isUndefined(connection)) {
          connection.setPaintStyle(connectorPaintStyles[edge.state]);
        }
      }
    }
  };

  that.addOutputPoint = function addOutputPoint(nodeElement, ports, nodeObj) {
    let anchors = (ports.length === 1) ?
      ['BottomCenter'] :
      ['BottomLeft', 'BottomCenter', 'BottomRight'];

    let outputStyle = _.assign({}, OUTPUT_STYLE, {
      cssClass: GraphPanelStyler.getOutputEndpointDefaultCssClass(internal.renderMode),
      hoverClass: GraphPanelStyler.getOutputEndpointDefaultHoverCssClass(internal.renderMode)
    });

    for (let i = 0; i < ports.length; i++) {
      let port = jsPlumb.addEndpoint(nodeElement, outputStyle, {
        anchor: anchors[i],
        uuid: ports[i].id
      });

      port.setParameter('portIndex', i);
      port.setParameter('nodeId', nodeObj.id);

      GraphPanelStyler.styleOutputEndpointDefault(port, internal.renderMode);

      port.bind('click', (port, event) => {
        $rootScope.$broadcast('OutputPort.LEFT_CLICK', {
          reference: port,
          portObject: ports[i],
          event: event
        });

        event.stopPropagation();
      });

      port.canvas.addEventListener('mousedown', (event) => {
        if (internal.renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
          ConnectionHinterService.showHints(port, internal.renderMode);
          ConnectionHinterService.highlightOperations(port);

          event.stopPropagation();
        }
      });

      port.bind('mouseover', (endpoint) => {
        internal.broadcastHoverEvent('OutputPoint.MOUSEOVER', endpoint.canvas, ports[i]);
      });

      port.bind('mouseout', (endpoint) => {
        internal.broadcastHoverEvent('OutputPoint.MOUSEOUT', endpoint.canvas, ports[i]);
      });
    }
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

      GraphPanelStyler.styleInputEndpointDefault(port, internal.renderMode);

      port.bind('click', () => {
        $rootScope.$broadcast('InputPoint.CLICK');
      });

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
      let edge = WorkflowService.getWorkflow().createEdge(data);

      info.connection.setParameter('edgeId', edge.id);

      $rootScope.$broadcast(Edge.CREATE, {edge: edge});

      if (DeepsenseCycleAnalyser.cycleExists(WorkflowService.getWorkflow())) {
        NotificationService.showError({
          title: 'Error',
          message: 'You cannot create a cycle in the graph!'
        }, 'A cycle in the graph has been detected!');

        $timeout(() => {
          $rootScope.$broadcast(Edge.REMOVE, {
            edge: WorkflowService.getWorkflow().getEdgeById(info.connection.getParameter('edgeId'))
          });

          jsPlumb.detach(info.connection);
        }, 0, false);
      } else {
        WorkflowService.saveWorkflow();
      }
    });

    jsPlumb.bind('connectionDetached', (info, originalEvent) => {
      let workflow = WorkflowService.getWorkflow();
      if (workflow) {
        let edge = workflow.getEdgeById(info.connection.getParameter('edgeId'));
        if (edge && info.targetEndpoint.isTarget && info.sourceEndpoint.isSource && originalEvent) {
          $rootScope.$broadcast(Edge.REMOVE, { edge: edge });
          WorkflowService.saveWorkflow();
        }
      }
    });

    jsPlumb.bind('connectionMoved', (info) => {
      let edge = WorkflowService.getWorkflow().getEdgeById(info.connection.getParameter('edgeId'));
      if (edge) {
        $rootScope.$broadcast(Edge.REMOVE, { edge: edge });
        WorkflowService.saveWorkflow();
      }
    });

    jsPlumb.bind('connectionDrag', () => {
      $rootScope.$broadcast(Edge.DRAG);
    });
  };

  that.setRenderMode = function setRenderMode(renderMode) {
    if (renderMode !== GraphPanelRendererBase.EDITOR_RENDER_MODE &&
      renderMode !== GraphPanelRendererBase.REPORT_RENDER_MODE)
    {
      throw `render mode should be either 'editor' or 'report'`;
    }

    internal.renderMode = renderMode;
  };

  that.rerender = function rerender() {
    that.init();
    that.renderPorts();
    that.renderEdges();
    that.repaintEverything();
  };

  that.bindDisablePortHighlightingsEvents = function bindDisablePortHighlightingsEvents() {
    const disablePortHighlightings = () => {
      $timeout(() => {
        if (internal.renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
          ConnectionHinterService.setDefaultPortColors(internal.renderMode);
          ConnectionHinterService.disableHighlightingOoperations();
        }
      }, 0, false);
    };

    $document.on('mousedown', disablePortHighlightings);
    $rootScope.$on('FlowChartBox.ELEMENT_DROPPED', disablePortHighlightings);
    $rootScope.$on('Keyboard.KEY_PRESSED_DEL', disablePortHighlightings);
    $rootScope.$on(Edge.CREATE, disablePortHighlightings);
    $rootScope.$on(Edge.REMOVE, disablePortHighlightings);
    $rootScope.$on(GraphNode.MOUSEDOWN, disablePortHighlightings);
    jsPlumb.bind('connectionDragStop', disablePortHighlightings);
  };

  that.bindDisablePortHighlightingsEvents();

  return that;
}

exports.inject = function (module) {
  module.service('GraphPanelRendererService', GraphPanelRendererService);
};
