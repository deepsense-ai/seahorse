/* globals d3, nv */

'use strict';

import tpl from './plot.html';

/* @ngInject */
function PiePlot($filter) {
  const chart = nv.models.pieChart();

  const directive = {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      'data': '='
    },
    link: function(scope, element) {
      scope.$watch('data', function(data) {
        displayChart(data, element);
      });

      scope.$on('$destroy', function() {
        chart.tooltip.hidden(true);
      });

      function displayChart(data, element) {
        const chartValues = _.map(data.counts, function (val, idx) {
          val = $filter('precision')(val);
          return {
            x: data.buckets[idx],
            y: val
          };
        });

        chart
            .duration(500)
            .noData('There is no Data to display')
            .labelThreshold(0)
            .labelType('percent');

        chart.tooltip.hideDelay(0);

        d3.select(element[0].querySelector('.svg-plot'))
            .datum(chartValues)
            .call(chart);

        nv.utils.windowResize(chart.update);
      }

    }
  };
  return directive;
}

exports.inject = function(module) {
  module.directive('piePlot', PiePlot);
};
