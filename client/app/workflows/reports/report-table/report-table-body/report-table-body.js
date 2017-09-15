'use strict';

function ReportTableBody() {
  return {
    templateUrl: 'app/workflows/reports/report-table/report-table-body/report-table-body.html',
    controller: 'ReportTableBodyController',
    bindToController: true,
    controllerAs: 'reportTableBody',
    replace: 'true',
    scope: {
      'tableData': '=',
      'tableColumnsData': '=',
      'selectionColumnEnabled': '='
    },
    link: function(scope, element, args, controller) {
      if (scope.reportTableBody.selectionColumnEnabled) {
        element.on('click', function(event) {
          scope.$apply(() => {
            controller.selectColumn(event);
            controller.extendSidePanel();
          });
        });
      }
    }
  };
}

exports.inject = function(module) {
  module.directive('reportTableBody', ReportTableBody);
};
