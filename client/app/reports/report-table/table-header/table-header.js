/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 06.05.15.
 */

'use strict';

function TableHeader() {
  return {
    templateUrl: 'app/reports/report-table/table-header/table-header.html',
    replace: 'true'
  };
}

exports.inject = function (module) {
  module.directive('tableHeader', TableHeader);
};
