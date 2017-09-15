'use strict';

import {
  GraphPanelRendererBase
}
from './../graph-panel/graph-panel-renderer/graph-panel-renderer-base.js';

/* @ngInject */
function WorkflowsReportController(
  $state, $scope, report, Report,
  GraphNode, PageService, Operations, GraphPanelRendererService, WorkflowService, DeepsenseNodeParameters
) {
  let that = this;
  let internal = {};

  _.assign(internal, {
    selectedNode: null,
    unselectNode: () => {
      internal.selectedNode = null;
    }
  });

  _.assign(that, {
    getWorkflow: WorkflowService.getWorkflow,
    getSelectedNode: () => internal.selectedNode,
    get GUIData() {
      return report.thirdPartyData.gui;
    },
    get reportData() {
      return report.executionReport;
    }
  });

  internal.init = function init() {
    PageService.setTitle('Workflow execution report');

    let workflow = WorkflowService.createWorkflow(report, Operations.getData());
    workflow.updateState(report.executionReport);
    workflow.setPortTypesFromReport(report.executionReport.resultEntities);

    GraphPanelRendererService.setRenderMode(GraphPanelRendererBase.REPORT_RENDER_MODE);
    GraphPanelRendererService.setZoom(1.0);

    WorkflowService.updateEdgesStates();
    GraphPanelRendererService.changeEdgesPaintStyles();

    Report.createReportEntities(report.executionReport.id, report.executionReport.resultEntities);
  };

  $scope.$on(GraphNode.CLICK, (event, data) => {
    let node = data.selectedNode;

    internal.selectedNode = node;

    if (node.hasParameters()) {
      $scope.$digest();
    } else {
      Operations.getWithParams(node.operationId)
        .then((operationData) => {
          $scope.$applyAsync(() => {
            node.setParameters(operationData.parameters, DeepsenseNodeParameters);
          });
        }, (error) => {
          console.error('operation fetch error', error);
        });
    }
  });

  $scope.$on('AttributePanel.UNSELECT_NODE', () => {
    internal.unselectNode();
    $scope.$digest();
  });

  $scope.$on('StatusBar.HOME_CLICK', () => {
    let url = $state.href('home');
    window.open(url, '_blank');
  });

  $scope.$on('OutputPort.LEFT_CLICK', (event, data) => {
    let node = WorkflowService.getWorkflow()
      .getNodeById(data.portObject.nodeId);
    $scope.$applyAsync(() => {
      let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));
      if (Report.hasReportEntity(reportEntityId)) {
        $state.go('workflows.reportEntity', {
          reportEntityId: reportEntityId
        });
      }
    });
  });

  $scope.$on('$destroy', () => {
    WorkflowService.clearWorkflow();
    GraphPanelRendererService.clearWorkflow();
  });

  internal.init();

  return that;
}

exports.function = WorkflowsReportController;

exports.inject = function(module) {
  module.controller('WorkflowsReportController', WorkflowsReportController);
};
