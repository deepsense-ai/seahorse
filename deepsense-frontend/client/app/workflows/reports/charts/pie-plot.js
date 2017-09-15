'use strict';

function PiePlot() {
  const LABEL_PRECISION = 4;
  const directive = {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/plot.html',
    replace: true,
    scope: {
      'data': '='
    },
    link: function(scope, element) {
      scope.$watch('data', function(data) {
        displayChart(data, element);
      });
    }
  };
  return directive;

  function displayChart(data, element) {
    const chart = nv.models.pieChart();

    const chartValues = _.map(data.counts, function (val, idx) {
      const shortVal = parseFloat(val).toPrecision(LABEL_PRECISION);
      return {
        x: `${data.buckets[idx]}: ${shortVal}`,
        y: val
      };
    });

    chart
        .duration(500)
        .noData('There is no Data to display')
        .labelThreshold(0)
        .labelType('percent');

    d3.select(element[0].querySelector('.svg-plot'))
        .datum(chartValues)
        .call(chart);

    nv.utils.windowResize(chart.update);
  }
}

exports.inject = function(module) {
  module.directive('piePlot', PiePlot);
};
