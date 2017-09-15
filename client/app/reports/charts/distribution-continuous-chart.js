/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function DistributionContinuousChart() {
  return {
    restrict: 'E',
    templateUrl: 'app/reports/charts/distribution-continuous-chart.html',
    replace: true,
    scope: false,
    controller: function($scope) {
      _.assign($scope, {
        chosenPlot: {
          value: null
        },
        plots: [{
          name: 'Column plot',
          type: 'column'
        }, {
          name: 'Box plot',
          type: 'box'
        }]
      });

      // default setting
      $scope.chosenPlot.value = $scope.plots[0].type;
    },
    controllerAs: 'distributionContinuousChart'
  };
}

exports.inject = function (module) {
  module.directive('distributionContinuousChart', DistributionContinuousChart);
};
