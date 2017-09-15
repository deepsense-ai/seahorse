'use strict';

let REPORT_EVENTS = require('../reports.controller.js').EVENTS;

/* @ngInject */
function ReportTableController($scope, $rootScope, $element) {
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
    selectColumn: function(index) {
      let tableEl = $element[0];
      let allCellsToSelect = tableEl.querySelectorAll(`td:nth-child(${index + 1}), th:nth-child(${index + 1})`);

      _.forEach(allCellsToSelect, (cell) => {
        cell.classList.add(internals.CELL_HIGHLIGHT_CLASS);
      });
    }
  });

  _.assign(that, {
    extendSidePanel: function extendSidePanel() {
      let tableEl = $element[0];
      let highlightedCell = tableEl.querySelector('td.info');

      if (highlightedCell) {
        // TODO There should be no HTML in controller.
        let index = highlightedCell.cellIndex + 1;
        let colName = tableEl.querySelector(`th:nth-child(${ index }) span.col-name`)
          .dataset.columnName;

        $rootScope.$broadcast(REPORT_EVENTS.SELECT_COLUMN, {
          colName: colName
        });

        $scope.$on(REPORT_EVENTS.DESELECT_COLUMN, internals.clearSelection);
      }
    },
    selectColumn: function selectColumn(event) {
      // get exactly the cell, not a <span> or <a> or something else
      let cell = $(event.target)
        .closest('td, th');

      if (!cell.length) {
        return false;
      } else {
        internals.clearSelection();
        internals.selectColumn(cell[0].cellIndex);
      }
    }
  });
}

ReportTableController.$inject = ['$scope', '$rootScope', '$element'];

exports.inject = function(module) {
  module.controller('ReportTableController', ReportTableController);
};
