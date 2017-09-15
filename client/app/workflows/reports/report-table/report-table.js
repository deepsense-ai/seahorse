'use strict';

function ReportTable() {
  return {
    templateUrl: 'app/workflows/reports/report-table/report-table.html',
    controller: 'ReportTableController',
    bindToController: true,
    controllerAs: 'reportTable',
    replace: 'true',
    scope: {
      'tableData': '=',
      'tableColumnsData': '=',
      // TODO What is it for? Can we remove it?
      'selectionColumnEnabled': '=',
      'tableSizes': '='
    },
    link: function(scope, element, args, controller) {
      if (scope.reportTable.selectionColumnEnabled) {
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
  module.directive('reportTable', ReportTable);
};
