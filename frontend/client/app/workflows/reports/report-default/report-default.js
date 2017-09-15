'use strict';

import tpl from './report-default.html';

function ReportDefault() {
  return {
    scope: {
      'report': '='
    },
    templateUrl: tpl,
    replace: 'true',
    controller: function() {},
    controllerAs: 'controller'
  };
}

exports.inject = function(module) {
  module.directive('reportDefault', ReportDefault);
};
