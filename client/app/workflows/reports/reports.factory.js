/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

/* @ngInject */
function ReportsFactory() {
  let reportsStorage = new Map();

  let createReportEntities = (reportId, resultEntities) => {
    for (let reportEntityId in resultEntities) {
      let resultEntity = resultEntities[reportEntityId];
      if (resultEntity.report) {
        resultEntity.report.reportId = reportId;
        reportsStorage.set(reportEntityId, resultEntity);
      }
    }
  };

  let getReportEntity = (reportEntityId) => {
    return reportsStorage.get(reportEntityId);
  };

  let hasReportEntity = (reportEntityId) => {
    return !!getReportEntity(reportEntityId);
  };

  return {
    createReportEntities: createReportEntities,
    getReportEntity: getReportEntity,
    hasReportEntity: hasReportEntity
  };
}

exports.function = ReportsFactory;

exports.inject = function (module) {
  module.factory('Report', ReportsFactory);
};
