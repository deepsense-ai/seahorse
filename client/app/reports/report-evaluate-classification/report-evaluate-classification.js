/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

function ReportEvaluateClassification() {
  return {
    scope: {
      'data': '='
    },
    templateUrl: 'app/reports/report-evaluate-classification/report-evaluate-classification.html',
    replace: 'true',
    controller: function() {},
    controllerAs: 'reportEvaluateClassification',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('reportEvaluateClassification', ReportEvaluateClassification);
};
