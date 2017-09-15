'use strict';

function ReportDataframe() {
  return {
    scope: {
      'data': '=',
      'distributionsTypes': '='
    },
    templateUrl: 'app/workflows/reports/report-dataframe/report-dataframe.html',
    replace: 'true',
    controller: function() {
      // TODO Use some enum instead of name here.
      this.tableData = this.data['Data Sample'];
      this.tableData = this.tableData ? this.tableData : this.data['Column Names and Types'];
      this.tableSizes = this.data['DataFrame Size'];

      let columnTypeByName = _.object(_.zip(this.tableData.columnNames, this.tableData.columnTypes));

      this.tableColumnsData = _.map(this.tableData.columnNames, (columnName) => {
        let distributionType = this.distributionsTypes[columnName];
        return {
          'columnName': columnName,
          'type': columnTypeByName[columnName],
          'distributionType': distributionType
        };
      });
    },
    controllerAs: 'reportDataframe',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDataframe', ReportDataframe);
};
