'use strict';

function ColumnPlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/plot.html',
    replace: true,
    scope: {
      'data': '='
    },
    link: function(scope, element) {
      function displayChart(data) {

        let categories = _.map(_.sliding(data.buckets, 2), ([start, end]) =>
          start + " - " + end
        );

        $(element)
          .highcharts({
            chart: {
              type: 'column'
            },
            title: null,
            subtitle: null,
            xAxis: {
              categories: categories,
              labels: {
                rotation: -45,
                style: {
                  fontSize: '13px',
                  fontFamily: 'Verdana, sans-serif'
                }
              }
            },
            plotOptions: {
              column: {
                groupPadding: 0,
                pointPadding: 0,
                borderWidth: 1
              }
            },
            legend: {
              enabled: false
            },
            tooltip: {
              formatter: function() {
                return `
                <p>Value range:
                  <b>${this.x}</b>
                </p>
                <br />
                <p>Occurrence count: <b>${this.point.y}</b></p>
              `;
              }
            },
            series: [{
              data: data.counts
            }]
          });
      }

      scope.$applyAsync(() => {
        scope.$watch('data', displayChart);
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('columnPlot', ColumnPlot);
};
