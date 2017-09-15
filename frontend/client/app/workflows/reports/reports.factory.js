/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
