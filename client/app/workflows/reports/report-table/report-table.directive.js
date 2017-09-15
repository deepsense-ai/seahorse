'use strict';

import tpl from './report-table.html';


function ReportTable() {
  return {
    templateUrl: tpl,
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
