'use strict';

function DistributionContinuousChart() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/distribution-continuous-chart.html',
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
        }]
      });

      /*
       * The box plot can be displayed on the given data if and only if
       * there have been provided such values like median and quartiles.
       */
      if (this.data && this.data.statistics && this.data.statistics.median) {
        this.plots.push({
          name: 'Box plot',
          type: 'box'
        });
      }

      // default setting
      this.chosenPlot.value = this.plots[0].type;
    },
    controllerAs: 'distributionContinuousChart',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('distributionContinuousChart', DistributionContinuousChart);
};
