'use strict';

function ReportEvaluateScoring() {
  return {
    scope: {
      'data': '='
    },
    templateUrl: 'app/workflows/reports/report-evaluate-scoring/report-evaluate-scoring.html',
    replace: 'true',
    controller: function() {
      this.tableData = this.data['Evaluate Regression Report'];
    },
    controllerAs: 'reportEvaluateScoring',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('reportEvaluateScoring', ReportEvaluateScoring);
};
