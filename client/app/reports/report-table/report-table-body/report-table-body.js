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

function ReportTableBodyeController($scope, $rootScope, $element) {
  let that = this;
  let internals = {};

  internals.CELL_HIGHLIGHT_CLASS = 'info';
  _.assign(internals, {
    CELL_HIGHLIGHT_CLASS: 'info',
    clearSelection: function() {
      let tableEl = $element[0];
      let allCellsToRemove = tableEl.querySelectorAll(`.${internals.CELL_HIGHLIGHT_CLASS}`);

      _.forEach(allCellsToRemove, (cell) => {
        cell.classList.remove(internals.CELL_HIGHLIGHT_CLASS);
      });
    },
    selectColumn: function (index) {
      let tableEl = $element[0];
      let allCellsToSelect = tableEl.querySelectorAll(`td:nth-child(${index + 1}), th:nth-child(${index + 1})`);

      _.forEach(allCellsToSelect, (cell) => {
        cell.classList.add(internals.CELL_HIGHLIGHT_CLASS);
      });
    }
  });

  _.assign(that, {
    extendSidePanel: function extendSidePanel () {
      let tableEl = $element[0];
      let highlightedCell = tableEl.querySelector('td.info');

      if (highlightedCell) {
        let index = highlightedCell.cellIndex + 1;
        let colName = tableEl.querySelector(`th:nth-child(${ index }) span.col-name`).innerHTML;

        $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
          colName: colName
        });

        $scope.$on(REPORT_EVENTS.DESELECT_COLUMN, internals.clearSelection);
      }
    },
    selectColumn: function selectColumn (event) {
      // get exactly the cell, not a <span> or <a> or something else
      let cell = $(event.target).closest('td, th');

      if (!cell.length) {
        return false;
      } else {
        internals.clearSelection();
        internals.selectColumn(cell[0].cellIndex);
      }
    }
  });
}

exports.inject = function (module) {
  module.directive('reportTableBody', ReportTableBody);
};
