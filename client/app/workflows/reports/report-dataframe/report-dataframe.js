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
      this.tableData = this.tableData ? this.tableData : this.data['Column Names and Types']
      this.tableSizes = this.data['DataFrame Size'];
      this.tableColumnsData = {};
      _.forEach(this.distributionsTypes, function(distType, colName) {
        let icon = undefined;
        switch (distType) {
          case 'discrete':
            icon = 'fa-pie-chart';
            break;
          case 'continuous':
            icon = 'fa-bar-chart-o';
            break
          // TODO Handle 'no_distribution'
          default:
            console.log('Unknown distType: ' + distType + ". Falling back to bar chart");
            icon = 'fa-bar-chart-o';
        }
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
