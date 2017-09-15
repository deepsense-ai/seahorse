'use strict';

function BoxPlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/plot.html',
    replace: true,
    scope: {
      'data': '='
    },
    link: function (scope, element) {
      function displayChart(data) {
        $(element)
          .highcharts({
            chart: {
              type: 'boxplot'
            },
            title: null,
            subtitle: null,
            legend: {
              enabled: false
            },
            xAxis: {
              categories: ['']
            },
            yAxis: {
              title: null
            },
            series: [{
              name: data.name,
              data: [
                [
                  parseFloat(data.statistics.min),
                  parseFloat(data.statistics.firstQuartile),
                  parseFloat(data.statistics.median),
                  parseFloat(data.statistics.thirdQuartile),
                  parseFloat(data.statistics.max)
                ]
              ],
              tooltip: {
                headerFormat: ''
              }
            }
            ]
          });
      }

      scope.$applyAsync(() => {
        scope.$watch('data', displayChart);
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('boxPlot', BoxPlot);
};
