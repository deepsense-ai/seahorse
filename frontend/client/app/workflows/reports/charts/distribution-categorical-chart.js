'use strict';

import tpl from './distribution-categorical-chart.html';

function DistributionCategoricalChart() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      'data': '='
    },
    controller: () => {},
    controllerAs: 'distributionCategoricalChart',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('distributionCategoricalChart', DistributionCategoricalChart);
};
