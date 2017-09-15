'use strict';

class WorkflowsEditorReports {
  constructor($scope, $rootScope, Report, PageService, Operations,
    GraphPanelRendererService, WorkflowService) {
    this.$scope = $scope;
    this.$rootScope = $rootScope;
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
      let node = this.WorkflowService.getWorkflow().getNodeById(data.portObject.nodeId);
      let reportEntityId = node.getResult(data.reference.getParameter('portIndex'));

      if (this.Report.hasReportEntity(reportEntityId)) {
        this.Report.getReport(reportEntityId).then(report => {
          this.reportName = '';
          this.report = report;
          this.reportName = report.name;
          this.Report.openReport();
        });
      }
    });

    this.inited = true;
  }
}

export default WorkflowsEditorReports;
