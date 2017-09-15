'use strict';

/* @ngInject */
function ReportsFactory($q, $rootScope, BottomBarService) {
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

  let getReport = reportEntityId => {
    let deferred = $q.defer();

    try {
      let reportEntity = getReportEntity(reportEntityId);
      $rootScope.stateData.dataIsLoaded = true;
      deferred.resolve(reportEntity.report);
    } catch (e) {
      $rootScope.stateData.errorMessage = 'Could not load the report';
      deferred.reject();
    }
    return deferred.promise;
  };

  let openReport = () => {
    BottomBarService.activatePanel('reportTab');
  };

  return {
    createReportEntities, getReportEntity, hasReportEntity, getReport, openReport
  };
}

exports.function = ReportsFactory;

exports.inject = function(module) {
  module.factory('Report', ReportsFactory);
};
