'use strict';

import {
  GraphPanelRendererBase
}
from './graph-panel-renderer-base.js';

class ConnectionHinterService extends GraphPanelRendererBase {

  /* @ngInject */
  constructor($rootScope, WorkflowService, OperationsHierarchyService, Operations, Report, GraphPanelStyler) {
    super();
    this.$rootScope = $rootScope;
    this.WorkflowService = WorkflowService;
    this.Report = Report;
    this.OperationsHierarchyService = OperationsHierarchyService;
    this.Operations = Operations;
    this.GraphPanelStyler = GraphPanelStyler;
  }

  /*
   * Highlights such ports that match to the given port
   * and colours ports that don't match.
   */
  highlightMatchedAndDismatchedPorts(workflow, sourceEndpoint) {
    const nodes = workflow.getNodes();

    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourceNode = workflow.getNodeById(sourceNodeId);
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = sourceNode.output[sourcePortIndex];

    const highlightInputPort = (endpoint, node) => {
      const portIndex = endpoint.getParameter('portIndex');
      const port = node.input[portIndex];

      const typesMatch = _.every(_.map(
        sourcePort.typeQualifier,
        typeQualifier => this.OperationsHierarchyService.IsDescendantOf(typeQualifier, port.typeQualifier)
      ));

      if (typesMatch && // types match
        endpoint.connections.length === 0 && // there cannot be any edge attached
        port.nodeId !== sourceNodeId) // attaching an edge to the same node is forbidden
      {
        this.GraphPanelStyler.styleInputEndpointTypeMatch(endpoint);
      } else {
        this.GraphPanelStyler.styleInputEndpointTypeDismatch(endpoint);
      }
    };

    _.forEach(nodes, (node) => {
      const nodeEl = this.getNodeById(node.id);
      const endpoints = jsPlumb.getEndpoints(nodeEl);

      _.forEach(endpoints, (endpoint, i) => {
        if (endpoint.isTarget) { // is an input port
          highlightInputPort(endpoint, node);
        }
      });
    });
  }

  disablePortHighlighting(workflow) {
    _.forEach(workflow.getNodes(), (node) => {
      let nodeEl = this.getNodeById(node.id);
      let endpoints = jsPlumb.getEndpoints(nodeEl);
      _.forEach(endpoints, (endpoint) => {
        endpoint.removeType('matched');
        endpoint.removeType('dismatched');
      });
    });
  }

  /*
   * Highlight the operation on the left side panel.
   */
  highlightOperations(workflow, sourceEndpoint) {
    const sourceNodeId = sourceEndpoint.getParameter('nodeId');
    const sourceNode = workflow.getNodeById(sourceNodeId);
    const sourcePortIndex = sourceEndpoint.getParameter('portIndex');
    const sourcePort = sourceNode.output[sourcePortIndex];

    const operations = this.Operations.getData();
    let operationsMatch = {};
    _.forEach(operations, (operation) => {
      const inputMatches = _.reduce(operation.ports.input, (acc, input) => {
        const typesMatch = _.every(_.map(
          sourcePort.typeQualifier,
          typeQualifier => this.OperationsHierarchyService.IsDescendantOf(typeQualifier, input.typeQualifier)
        ));

        acc.push(typesMatch);

        return acc;
      }, []);

      operationsMatch[operation.id] = _.any(inputMatches);
    });

    this.$rootScope.$broadcast('ConnectionHinter.HIGHLIGHT_OPERATIONS', operationsMatch);
  }

  disableOperationsHighlighting() {
    this.$rootScope.$broadcast('ConnectionHinter.DISABLE_HIGHLIGHTINGS');
  }
}

exports.inject = function(module) {
  module.service('ConnectionHinterService', ConnectionHinterService);
};
