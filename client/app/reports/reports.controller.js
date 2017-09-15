/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var EVENTS = {
  'EXTEND_SIDE_PANEL': 'extend-side-panel',
  'SHRINK_SIDE_PANEL': 'shrink-side-panel',
  'CHOSEN_COLUMN': 'chosen-column',
  'HIDE_DETAILS': 'hide-details'
};

/* @ngInject */
function Report(PageService, $scope, $stateParams, EntitiesAPIClient) {
  let that = this;
  let internal = {};
  let entityId = $stateParams.id;

  that.messageError = '';

  EntitiesAPIClient.getReport(entityId).then((data) => {
    internal.tables = data.tables;
    internal.distributions = data.distributions;
    internal.distributionsNames = _.reduce(
      internal.distributions,
      function (acc, distObj, name) {
        let re = /[a-zA-Z0-9]+/.exec(name);
        if (re) {
          acc[re[0]] = distObj.subtype;
        }
        return acc;
      },
      {}
    );

    PageService.setTitle(`Report: ${data.name}`);
  }, () => {
    that.messageError = `The report with id equals to ${entityId} does not exist!`;
  });

  that.getTable = function getTable() {
    return internal.tables && internal.tables.DataSample;
  };

  that.getDistributionObject = function getDistributionObject(colName) {
    if (internal.distributions) {
      return internal.distributions[colName + '_Distribution'];
    }
  };

  that.getDistributionsTypes = function getDistributionsTypes() {
    return internal.distributionsNames;
  };

  $scope.$on(EVENTS.CHOSEN_COLUMN, function (event, data) {
    let distObject = that.getDistributionObject(data.colName);
    let eventToBroadcast = _.isUndefined(distObject) ? EVENTS.SHRINK_SIDE_PANEL : EVENTS.EXTEND_SIDE_PANEL;

    $scope.$broadcast(eventToBroadcast, data);
  });

  $scope.$on(EVENTS.HIDE_DETAILS, () => {
    $scope.$broadcast(EVENTS.SHRINK_SIDE_PANEL);
  });

  return that;
}

exports.function = Report;
exports.EVENTS = EVENTS;

exports.inject = function (module) {
  module.controller('Report', Report);
};
