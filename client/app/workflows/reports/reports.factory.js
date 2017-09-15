/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function ReportsFactory() {
  let reportsStorage = new Map();

  let createReportEntities = (resultEntities) => {
    for (let reportEntityId in resultEntities) {
      reportsStorage.set(reportEntityId, resultEntities[reportEntityId]);
    }
  };

  let getReportEntity = (reportEntityId) => {
    return reportsStorage.get(reportEntityId);
  };

  return {
    createReportEntities: createReportEntities,
    getReportEntity: getReportEntity
  };
}

exports.function = ReportsFactory;

exports.inject = function (module) {
  module.factory('Report', ReportsFactory);
};
