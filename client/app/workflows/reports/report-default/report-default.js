'use strict';

function ReportDefault() {
  return {
    scope: {
      'report': '='
    },
    templateUrl: 'app/workflows/reports/report-default/report-default.html',
    replace: 'true',
    controller: function() {},
    controllerAs: 'controller',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('reportDefault', ReportDefault);
};
