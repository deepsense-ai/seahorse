/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

'use strict';

function ReportTable() {
  return {
    scope: {
      'data':'='
    },
    templateUrl: 'app/reports/report-table/report-table.html',
    replace: 'true'
  };
}

exports.inject = function (module) {
  module.directive('reportTable', ReportTable);
};
