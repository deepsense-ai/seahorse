'use strict';

function ReportTableHeader() {
  return {
    templateUrl: 'app/workflows/reports/report-table/report-table-header/report-table-header.html',
    replace: 'true',
    scope: {
      'tableData': '=',
      'tableSizes': '='
    },
    controller: () => {},
    controllerAs: 'reportTableHeader',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportTableHeader', ReportTableHeader);
};
