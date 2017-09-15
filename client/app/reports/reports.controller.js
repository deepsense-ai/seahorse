/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Report($scope, $state, $stateParams, EntitiesAPIClient) {

  let that = this;
  let internal = {};
  let entityId = $stateParams.id;

  internal.closeReport = function closeReport () {
    history.back();
  };

  that.messageError = '';

  EntitiesAPIClient.getReport(entityId).then((data) => {
    internal.tables = data.tables;
    internal.distributions = data.distributions;
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

  that.dismiss = function dismiss () {
    internal.closeReport();
  };

  return that;
}

exports.function = Report;

exports.inject = function (module) {
  module.controller('Report', Report);
};
