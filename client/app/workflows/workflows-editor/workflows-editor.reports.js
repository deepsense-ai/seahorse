'use strict';

class WorkflowsEditorReports {
  constructor($scope, Report, PageService, Operations,
    GraphPanelRendererService, WorkflowService) {
    this.$scope = $scope;
    this.Report = Report;
    this.PageService = PageService;
    this.Operations = Operations;
    this.GraphPanelRendererService = GraphPanelRendererService;
    this.WorkflowService = WorkflowService;
  }

  init(report) {
    this.WorkflowService.getWorkflow().setPortTypesFromReport(report);
    this.Report.createReportEntities(report.id, report);
  }

  initListeners() {
    if (this.inited) {
      return false;
    }

    this.$scope.$on('OutputPort.LEFT_CLICK', (event, data) => {
      let node = this.WorkflowService.getWorkflow()
        .getNodeById(data.portObject.nodeId);

      this.$scope.$applyAsync(() => {
        let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));

        if (this.Report.hasReportEntity(reportEntityId)) {
          this.Report.getReport(reportEntityId).then(report => {
            this.report = report;
          });
        }
      });
    });

    this.inited = true;
  }
}

export default WorkflowsEditorReports;
