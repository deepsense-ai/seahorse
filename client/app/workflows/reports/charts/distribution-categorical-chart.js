'use strict';

function DistributionCategoricalChart() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/distribution-categorical-chart.html',
    replace: true,
    scope: {
      'data': '='
    },
    controller: () => {},
    controllerAs: 'distributionCategoricalChart',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('distributionCategoricalChart', DistributionCategoricalChart);
};
