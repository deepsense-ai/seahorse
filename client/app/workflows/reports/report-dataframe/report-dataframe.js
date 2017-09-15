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
      this.tableData = this.data['Data Sample'];
      this.tableSizes = this.data['DataFrame Size'];
      this.tableColumnsData = {};
      _.forEach(this.distributionsTypes, function(distType, colName) {
        let icon = (distType === 'categorical') ? 'fa-pie-chart' : 'fa-bar-chart-o';
        this.tableColumnsData[colName] = {
          'icon': icon
        };
      }.bind(this));
    },
    controllerAs: 'reportDataframe',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDataframe', ReportDataframe);
};
