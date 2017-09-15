/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 06.05.15.
 */

'use strict';

function TableData() {
  return {
    templateUrl: 'app/reports/table-data/table-data.html',
    controller: TableController,
    replace: 'true',
    link: function (scope, element, args, controller) {
      element.on('click', function(event) {
        // this == element[0]
        controller.showDetails(this);
        controller.selectColumn(this, event);
      });
    }
  };
}

function TableController($rootScope, TopWalkerService) {
  let that = this;
  let internals = {};

  internals.tableDimensions = {
    DETAILS_ON: 6,
    DETAILS_OFF: 9
  };

  internals.CELL_HIGHLIGHT_CLASS = 'info';

  internals.toggleTable = function toggleTable (tableHolder, dimension, eventName) {
    let allClasses = tableHolder.classList;
    let indexOfDimensionClass = -1;
    let replacedClassName = '';

    indexOfDimensionClass = _.findIndex(allClasses, function (singleClass) {
      return singleClass.indexOf('col-') > -1;
    });

    if (indexOfDimensionClass === -1) {
      return false;
    }

    replacedClassName = allClasses[indexOfDimensionClass].replace(/\d+$/, dimension);

    allClasses.add(replacedClassName);

    // to not delete the same class
    if (replacedClassName !== allClasses[indexOfDimensionClass]) {
      allClasses.remove(allClasses[indexOfDimensionClass]);
    }

    $rootScope.$broadcast(eventName);
  };

  that.hideDetails = function hideDetails (tableHolder) {
    return internals.toggleTable(tableHolder, internals.tableDimensions.DETAILS_OFF, 'REPORT.HIDE_DETAILS');
  };

  that.showDetails = function showDetails (tableHolder) {
    return internals.toggleTable(tableHolder, internals.tableDimensions.DETAILS_ON, 'REPORT.SHOW_DETAILS');
  };

  that.selectColumn = function selectColumn (tableHolder, event) {
    let cell = null;
    let cellsIndexToSelect = -1;
    let allCellsToSelect = [];
    let allCellsToRemove = [];

    // get exactly the cell, not a <span> or <a> or something else
    cell = TopWalkerService.walk(event.target, function (node) {
      let tagName = node.tagName.toLowerCase();
      return tagName === 'td' || tagName === 'th';
    }, tableHolder);

    // if we clicked somehow above the td, th
    if (!cell) {
      return false;
    }

    cellsIndexToSelect = cell.cellIndex;

    allCellsToSelect =
      tableHolder.querySelectorAll(`td:nth-child(${cellsIndexToSelect + 1}), th:nth-child(${cellsIndexToSelect + 1})`);
    allCellsToRemove =
      tableHolder.querySelectorAll(`.${internals.CELL_HIGHLIGHT_CLASS}`);

    // remove added classes from all old cells
    _.forEach(allCellsToRemove, function(cell) {
      cell.classList.remove(internals.CELL_HIGHLIGHT_CLASS);
    });

    // add classes to each cell
    _.forEach(allCellsToSelect, function(cell) {
      cell.classList.add(internals.CELL_HIGHLIGHT_CLASS);
    });
  };

  $rootScope.$on('REPORT.HIDE_DETAILS', that.hideDetails);
}

exports.inject = function (module) {
  module.directive('tableData', TableData);
};
