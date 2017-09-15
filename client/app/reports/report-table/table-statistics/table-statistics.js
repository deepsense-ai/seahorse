/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 06.05.15.
 */

'use strict';

function TableStatistics() {
  return {
    templateUrl: 'app/reports/report-table/table-statistics/table-statistics.html',
    replace: 'true'
  };
}

exports.inject = function (module) {
  module.directive('tableStatistics', TableStatistics);
};
