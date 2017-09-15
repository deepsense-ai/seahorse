'use strict';


function ReportTable() {
  return {
    templateUrl: 'app/workflows/reports/report-table/report-table.html',
    scope: {
      'table': '=',
      // Even though distributions are strictly connected to tables at the moment, we have them
      // at root level next to tables for now.
      'distributions': '=',
      'datatypesVisible': '=?'
    },
    controller: 'reportTableController',
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportTable', ReportTable);
};
