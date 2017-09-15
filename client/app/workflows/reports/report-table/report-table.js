'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

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
    controller: function($rootScope) {
      let columnTypeByName = _.object(_.zip(this.table.columnNames, this.table.columnTypes));
      this.tableColumnsData = _.map(this.table.columnNames, (columnName) => {
        let distributionType = this.distributions && this.distributions[columnName];
        return {
          'columnName': columnName,
          'type': columnTypeByName[columnName],
          'distributionType': distributionType
        };
      });

      this.showDistribution = function(columnData) {
        if (columnData.distributionType) {
          $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
            colName: columnData.columnName
          });
        }

      };

    },
    controllerAs: 'controller',
    bindToController: true,
    replace: 'true'
  };
}

exports.inject = function(module) {
  module.directive('reportTable', ReportTable);
};
