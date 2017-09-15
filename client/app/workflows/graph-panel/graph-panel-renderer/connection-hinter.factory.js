'use strict';

import {
  GraphPanelRendererBase
}
from './graph-panel-renderer-base.js';
import {
  GraphPanelStyler
}
from './graph-panel-styler.js';

class ConnectionHinterService extends GraphPanelRendererBase {

  constructor($rootScope, WorkflowService, OperationsHierarchyService, Operations, Report) {
    super();
    this.$rootScope = $rootScope;
    this.WorkflowService = WorkflowService;
    this.Report = Report;
    this.OperationsHierarchyService = OperationsHierarchyService;
    this.Operations = Operations;
  }

  /*
   * Highlights such ports that match to the given port
   * and colours ports that don't match.
   */
  showHints(sourceEndpoint, renderMode, hasReport) {
    const workflow = this.WorkflowService.getWorkflow();
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
        GraphPanelStyler.styleInputEndpointTypeMatch(endpoint, renderMode, hasReport);
      } else {
        GraphPanelStyler.styleInputEndpointTypeDismatch(endpoint, renderMode, hasReport);
      }
    };

    _.forEach(nodes, (node) => {
      const nodeEl = this.getNodeById(node.id);
      const endpoints = jsPlumb.getEndpoints(nodeEl);

      _.forEach(endpoints, (endpoint, i) => {
        let reportEntityId = node.getResult(i);
        let hasReport = this.Report.hasReportEntity(reportEntityId);

        if (endpoint.isTarget) { // is an input port
          highlightInputPort(endpoint, node);
        }
        if (endpoint.isSource) { // is an output port
          GraphPanelStyler.styleOutputEndpointDefault(endpoint, renderMode, hasReport);
        }
      });
    });

    GraphPanelStyler.styleSelectedOutputEndpoint(sourceEndpoint);
  }

  /*
   * Remove any port highlighting.
   */
  setDefaultPortColors(renderMode) {
    const nodes = this.WorkflowService.getWorkflow()
      .getNodes();

    _.forEach(nodes, (node) => {
      const nodeEl = this.getNodeById(node.id);
      const endpoints = jsPlumb.getEndpoints(nodeEl);
      _.forEach(endpoints, (endpoint, i) => {
        let reportEntityId = node.getResult(i);
        let hasReport = this.Report.hasReportEntity(reportEntityId);

        if (endpoint.isSource && !hasReport) {
          GraphPanelStyler.styleInputEndpointDefault(endpoint, renderMode);
        }
        if (endpoint.isTarget) {
          GraphPanelStyler.styleOutputEndpointDefault(endpoint, renderMode);
        }
      });
    });
  }

  /*
   * Highlight the operation on the left side panel.
   */
  highlightOperations(sourceEndpoint, hasReport) {
    const workflow = this.WorkflowService.getWorkflow();
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

  disableHighlightingOoperations() {
    this.$rootScope.$broadcast('ConnectionHinter.DISABLE_HIGHLIGHTINGS');
  }
}

exports.inject = function(module) {
  module.factory('ConnectionHinterService', /* @ngInject */ ($rootScope, WorkflowService, OperationsHierarchyService, Operations, Report) => {
    return new ConnectionHinterService($rootScope, WorkflowService, OperationsHierarchyService, Operations, Report);
  });
};
