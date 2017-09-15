'use strict';

function ReportDataframeFull() {
  return {
    scope: {
      'report': '='
    },
    templateUrl: 'app/workflows/reports/report-dataframe-full/report-dataframe-full.html',
    replace: 'true',
    controller: function() {
      let tableByName = () => _.object(_.map(this.report.tables, (table) => [table.name, table]));
      this.dataSample = () => tableByName()['Data Sample'];
      let tableSizes = () => tableByName()['DataFrame Size'];
      this.dataframeHeaderData = () => {
        return {
          columnCount: tableSizes().values[0][0],
          rowCount: tableSizes().values[0][1],
          previewRowCount: this.dataSample().values.length
        };
      };
    },
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDataframeFull', ReportDataframeFull);
};
