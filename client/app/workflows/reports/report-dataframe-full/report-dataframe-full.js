'use strict';

function ReportDataframeFull() {
  return {
    scope: {
      'report': '='
    },
    templateUrl: 'app/workflows/reports/report-dataframe-full/report-dataframe-full.html',
    replace: 'true',
    controller: function() {
      this.dataSample = this.report.tables['Data Sample'];
      let tableSizes = this.report.tables['DataFrame Size'];
      this.dataframeHeaderData = {
        columnCount: tableSizes.values[0][0],
        rowCount: tableSizes.values[0][1],
        previewRowCount: this.dataSample.values.length
      };
    },
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDataframeFull', ReportDataframeFull);
};
