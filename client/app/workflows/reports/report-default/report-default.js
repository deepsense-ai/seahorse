'use strict';

function ReportDefault() {
  return {
    scope: {
      'data': '='
    },
    templateUrl: 'app/workflows/reports/report-default/report-default.html',
    replace: 'true',
    controller: () => {},
    controllerAs: 'reportDefault',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDefault', ReportDefault);
};
