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
    scope: {
      'data': '='
    },
    controller: function() {
      _.assign(this, {
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
      this.chosenPlot.value = this.plots[0].type;
    },
    controllerAs: 'distributionContinuousChart',
    bindToController: true
  };
}

exports.inject = function (module) {
  module.directive('distributionContinuousChart', DistributionContinuousChart);
};
