/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 06.05.15.
 */

'use strict';

let REPORT_EVENTS = require('../../reports.controller.js').EVENTS;

function ReportTableBody() {
  return {
    templateUrl: 'app/reports/report-table/report-table-body/report-table-body.html',
    controller: ReportTableBodyeController,
    bindToController: true,
    controllerAs: 'reportTableBody',
    replace: 'true',
    scope: {
      'tableData': '=',
      'tableColumnsData': '=',
      'selectionColumnEnabled': '='
    },
    link: function (scope, element, args, controller) {
      if (scope.reportTableBody.selectionColumnEnabled) {
        element.on('click', function (event) {
          scope.$apply(() => {
            controller.selectColumn(event);
            controller.extendSidePanel();
          });
        });
      }
    }
  };
}

function ReportTableBodyeController($scope, $element, TopWalkerService) {
  let that = this;
  let internals = {};

  internals.CELL_HIGHLIGHT_CLASS = 'info';

  _.assign(that, {
    extendSidePanel: function extendSidePanel () {
      let tableEl = $element[0];
      let highlightedCell = tableEl.querySelector('td.info');

      if (highlightedCell) {
        let index = highlightedCell.cellIndex + 1;
        let colName = tableEl.querySelector(`th:nth-child(${ index }) span.col-name`).innerHTML;

        $scope.$emit(REPORT_EVENTS.CHOSEN_COLUMN, {
          colName: colName
        });
      }
    },
    selectColumn: function selectColumn (event) {
      let cell;
      let cellsIndexToSelect;
      let allCellsToSelect;
      let allCellsToRemove;
      let tableEl = $element[0];

      // get exactly the cell, not a <span> or <a> or something else
      cell = TopWalkerService.walk(event.target, function (node) {
        let tagName = node.tagName.toLowerCase();
        return tagName === 'td' || tagName === 'th';
      }, tableEl);

      // if we clicked somewhere above the td, th
      if (!cell) {
        return false;
      }

      cellsIndexToSelect = cell.cellIndex;

      allCellsToSelect =
        tableEl.querySelectorAll(`td:nth-child(${cellsIndexToSelect + 1}), th:nth-child(${cellsIndexToSelect + 1})`);
      allCellsToRemove =
        tableEl.querySelectorAll(`.${internals.CELL_HIGHLIGHT_CLASS}`);

      // remove added classes from all old cells
      _.forEach(allCellsToRemove, function (cell) {
        cell.classList.remove(internals.CELL_HIGHLIGHT_CLASS);
      });

      // add classes to each cell
      _.forEach(allCellsToSelect, function (cell) {
        cell.classList.add(internals.CELL_HIGHLIGHT_CLASS);
      });
    }
  });
}

exports.inject = function (module) {
  module.directive('reportTableBody', ReportTableBody);
};
